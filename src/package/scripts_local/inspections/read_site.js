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

JavaLink.useDefaultDataSource();

JavaLink.queryByCriteria = function() {
	this.outputQueryResults('SELECT ID, 0 as LOCKVERSION FROM INSPECTION_SITE WHERE FEEDERID=?', this.param['CIRCUIT']);
}

JavaLink.queryByKey = function() {
	let pk = this.param.PK.split(',');
		this.outputQueryResults(`SELECT 
			ID, 
			0 as LOCKVERSION,
			MAP_KEY,
			X,
			Y,
			X,
			Y,
			type,
			MAP_KEY as report_key
		FROM INSPECTION_SITE WHERE ${Sql.whereIn('ID', pk)}`);
}


JavaLink.process = function() {
	if(this.param.ND) {
		this.queryByCriteria();
	}
	else if(this.param.PK) {
		this.queryByKey();
	}
	else {
		this.quit('Invalid query parameters');
	}



};

JavaLink.run();
