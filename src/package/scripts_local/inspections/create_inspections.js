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
var WM = require('WM');

JavaLink.useDefaultDataSource();

JavaLink.process = function() {
	 var data = this.thawData([
	 	'PROGRAM',
        'ORDER_KEY',
        'DEPARTMENT', // not used
        'CREW',
        {
            name: 'SITES', fields: [
                'SOURCE',
                'ID',
                'X',
                'Y'
            ]
        },
      ]);

	 this.db.autoCommit = false;

	 let order_key = data.ORDER_KEY;
	 var order = this.db.queryRow('SELECT ordersubkey, ordersubtype, svcrtacct FROM WM_ORDER where order_key = ?', order_key);
	 Lang.assert(order, "Invalid work order #: " + order_key);
	 let orderSubKey = order[0];
	 let orderSubType = order[1];
	 let locationNumber = order[2];

	 let remoteUser = this.environment['REMOTE_USER'];

	 Lang.assert(data.PROGRAM == orderSubType, `Invalid order type: ${orderSubType}`);

	 var wmResult = WM.createServiceOrder(this, orderSubKey, orderSubType, locationNumber, data.CREW, remoteUser);
	 if(!wmResult.ok) {
		 this.quit("Unable to create service order: " + wmResult.message);
	 }
	 let serviceOrderNumber = wmResult.data.orderNumber;
	 Lang.assert(serviceOrderNumber, "No orderNumber provided from createServiceOrder call");
	 Logger.info(`serviceOrderNumber = ${serviceOrderNumber}`);
	 var woListKey = this.db.insertRowReturnKey('WM_INSPECTION_LIST', {
	 	SERVICE_ORDNBR: serviceOrderNumber,
	 	PROGRAM_ORDER_KEY: order_key,
	 	PROGRAM_TYPE: data.PROGRAM,
	 	CREATE_USER: remoteUser
	 }, 'ID', {
		LOCKVERSION: '1',
	 	CREATE_DATE: this.db.databaseType.localtimeSql
	 });
	 Lang.assert(woListKey, "No ID for WM_INSPECTION_LIST returned");

	 var addressSth = this.db.prepare(`
	 	select top 1 premiseaddress
	 	  from mwm_electric_meter
	 	 where premiseaddress is not null
	 	   and lat between (? - 0.003) and (? + 0.003)
	 	   and lon between (? - 0.003) and (? + 0.003)
	 	   and premiseaddress LIKE '[0-9]%'
	 	 order by power(abs(lat - ?), 2) + power(abs(lon - ?),2)
	 `);
	 var insertSth = this.db.prepare(`insert into wm_inspection (LIST_ID, ASSET_SOURCE, ASSET_ID, X, Y, ADDRESS) VALUES ('${woListKey}',?,?,?,?,?)`);
	 try {
	 	data.SITES.forEach((site) => {
	 		let lon = +site.X;
	 		let lat = +site.Y;
	 		addressSth.executeQuery(lat, lat, lon, lon, lat, lon);
	 		let row = addressSth.fetch();
	 		let address = row ? row[0] : null;
	 		insertSth.execute(site.SOURCE, site.ID, lon, lat, address);
	 	});
	 }
	 finally {
	 	addressSth.close();
	 	insertSth.close();
	 }

	 this.db.autoCommit = true;

	 this.outputData([woListKey]);

};

JavaLink.run();
