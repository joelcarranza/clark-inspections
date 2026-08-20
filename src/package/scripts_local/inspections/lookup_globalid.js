/*
 * NOTICES
 * -------
 * 
 * Copyright 2013 by Gatekeeper Systems All Rights Reserved.
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
 *     99 East C Street Ste. 209
 *     Upland, Ca. 91786
 * 
 *     Tel: (626) 449-8135
 *     Fax: (626) 440-1742
 * 
 *     E-Mail: info@gatekeeper.com
 *     URL:    http://www.gatekeeper.com/
 *
 */

var JavaLink = require('JavaLink');
var Lang = require('Lang');
var Sql = require('Sql');

/*
 Outputs the circuits associated a set of map features
 Map features are passed as query parameter, with the layer name
 as the parameter name and the value as a comma separated list of keys

 Output is circuit ID + name
 */

JavaLink.useDefaultDataSource();

let LINE_QUERY = `SELECT l.GLOBALID 
	FROM EDGE e 
	JOIN ELECTRIC_LINE l ON e.VIA_OBJECT_ID = l.OBJECTID 
	WHERE VIA_FEAT_CODE IN ('PRIM', 'SEC','BUSBAR') AND VIA_ELEMENT_ID = ?`;

let DEVICE_QUERY = 'SELECT GLOBALID FROM ELECTRIC_DEVICE WHERE OBJECTID = ?';

let STRUCTURE_JUNCTION_QUERY = 'SELECT GLOBALID FROM ELECTRIC_STRUCTURE_JUNCTION WHERE OBJECTID = ?';


JavaLink.process = function() {

	var layersToTables = {
		"Primary Conductor":LINE_QUERY,
		"Primary Conductor by Phase":LINE_QUERY,
		"Secondary Conductor":LINE_QUERY,
		"Secondary Conductor by Phase":LINE_QUERY,
		"Secondary Conductor - Unthemed":LINE_QUERY,
		"Busbar":LINE_QUERY,
		"Capacitor Bank": DEVICE_QUERY,
		"Fault Indicator": DEVICE_QUERY,
		"Fuse": DEVICE_QUERY,
		"Open Point": DEVICE_QUERY,
		"Primary Meter": DEVICE_QUERY,
		"Recloser": DEVICE_QUERY,
		"Sectionalizer": DEVICE_QUERY,
		"Service Point": DEVICE_QUERY,
		"Street Light": DEVICE_QUERY,
		"Substation Breaker": DEVICE_QUERY,
		"Switch": DEVICE_QUERY,
		"Transformer": DEVICE_QUERY,
		"Voltage Regulators": DEVICE_QUERY,
		"Pole": STRUCTURE_JUNCTION_QUERY
	};

	var globalIds = {};

	Lang.keys(this.param).forEach(p => {
		if(p in layersToTables) {
			let query = layersToTables[p];
			let keys = this.param[p].split(',');
			let sth = this.db.prepare(query);
			try {
				keys.forEach(key => {
					sth.executeQuery(key);
					let row;
					while((row = sth.fetch()) != null) {
						let id = row[0];
						globalIds[id] = id;
					}
				});				
			}
			finally {
				sth.close();
			}
		}
		else {
			this.quit("Invalid layer: "+p);
		}
	});


	Lang.keys(globalIds).forEach(gid => {
		this.outputData(gid);
	});
}

JavaLink.run();