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

JavaLink.process = function() {

	var layersToTables = {
		// XXX:!
		"Primary Conductor":{ 
			table:"EDGE",
			key:"VIA_ELEMENT_ID"
		}
	};

	// key and value is circuit ID
	var circuitIDs = {}

	// gather all circuit IDs 
	Lang.keys(this.param).forEach(function(p) {
		if(p in layersToTables) {
			var query = layersToTables[p];
			var keys = this.param[p].split(',');
			var ids;
			ids = this.db.queryAll(`
					SELECT NOMINAL_FEEDERID 
					FROM ${query.table} 
					WHERE ${Sql.whereIn(query.key,keys)}
			`).map(Lang.first);
			ids.forEach(function(id) {
				circuitIDs[id] = id;
			});
		}
		else {
			this.quit("Invalid layer: "+p);
		}
	},this);

	

	var circuitIDList = Lang.keys(circuitIDs);
	if(circuitIDList.length == 0) {
		this.quit("No circuits found");
	}
	circuitIDList.forEach(k => {
		this.outputData(k, k);
	})
}

JavaLink.run();