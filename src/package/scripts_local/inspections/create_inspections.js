/*
 * NOTICES
 * -------
 * 
 * Copyright 2026 by Gatekeeper Systems All Rights Reserved.
 * 
 * Unpublished Work -- Protected under the copyright laws of the United States.
 * 
 * Restricted Rights Legend: Use, duplication or disclosure of the software
 * contained hereon is governed by the terms of a license agreement.  In
 * the absence of an agreement, use, duplication or disclosure by the United
 * States Government is subject to restrictions stated in subparagraph
 * (c)(1) of the Commercial Computer Software -- Restricted Rights clause
 * at FAR 52.227-9 or subparagraph (c)(1)(ii) of the Rights in Technical
 * Data and Computer Software clause at DFARS 252.227-7013, as applicable.
 * 
 * Contractor/Manufacturer:
 * 
 *     Gatekeeper Systems
 *     1010 E. Union St.
 *     Pasadena, CA 91106
 * 
 *     Tel: (626) 449-3070 or (800) 424-3070
 *     Fax: (626) 440-1742
 * 
 *     E-Mail: info@gatekeeper.com
 *     URL:    http://www.gatekeeper.com/
 *
 */

 
var JavaLink = require('JavaLink');
var Config = require('Config');
var Sql = require('Sql');
var Logger = require('Logger');
var Lang = require('Lang');

JavaLink.useDefaultDataSource();

JavaLink.process = function() {
	 var data = this.thawData([
	 	'PROGRAM',
        'ORDER_KEY',
        'CREW',
        {
            name: 'SITES', fields: [
                'TYPE',
                'ID',
            ]
        },
      ]);

	 let order_key = data.ORDER_KEY;
	 var order = this.db.queryRow('SELECT ordersubkey, ordertype FROM WM_ORDER where order_key = ?', order_key);
	 Lang.assert(order, "Invalid work order #: " + order_key);
	 let orderSubKey = order.ORDERSUBKEY;
	 let orderType = order.ORDERTYPE;
	 Lang.assert(['PFRS', 'PTRE', 'PCRS'].indexOf(orderType), "Invalid order type");

	 Logger.dump(data, 'info');
	 var woListKey = this.db.insertRowReturnKey('WM_INSPECTION_LIST', {
	 	WORK_ORDER: orderSubKey,
	 	ORDER_KEY: order_key,
	 	PROGRAM: data.PROGRAM
	 });
	 Lang.assert(woListKey, "No ID for WM_INSPECTION_LIST returned");

	 var sth = this.db.prepare(`
	 insert into wm_inspection (LIST_ID, ASSET_TYPE, ASSET_ID, X,Y)
	  select ${woListKey} as LIST_ID, ASSET_TYPE, ASSET_ID,(MIN_X + MAX_X / 2) as X, (MIN_Y + MAX_Y) / 2 as Y 
	 	from INSPECTION_SITE where ASSET_TYPE= ? and ASSET_ID = ?`);
	 data.SITES.forEach((site) => {
	 	sth.execute(site.TYPE, site.ID);
	 });
	 sth.close();

	 this.outputData([woListKey]);

};

JavaLink.run();
