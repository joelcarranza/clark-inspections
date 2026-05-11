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



JavaLink.process = function() {
	const mode = this.param['MODE'];

	const sql = `SELECT
			ASSET_TYPE,
			ASSET_ID,
			MIN_X,
			MIN_Y,
			MAX_X,
			MAX_Y,
			type_description,
			equipment,
			location
		FROM INSPECTION_SITE`;

	let where = [];
	let params = [];

	if (mode === 'criteria') {
		where.push('GLOBALID in (SELECT GLOBALID FROM INSPECTION_SITE_FEEDER WHERE FEEDERID = ?)');
		params.push(this.param['CIRCUIT']);
	}
	else if (mode === 'proximity') {
		where.push('GEOM.STDistance(geometry::Point(?, ?, 2286)) <= ?');
		params = params.concat([this.param['X'], this.param['Y'], this.param['DISTANCE']]);
	}
	else if (mode === 'globalid') {
		const ids = this.param['GLOBALID'].trim().split(/\s+/);
		where.push(Sql.whereIn('GLOBALID', ids));
	}
	else if (mode === 'trace') {
		where.push('GLOBALID in (SELECT GLOBALID FROM INSPECTION_SITE_TRACE_RESULTS WHERE TRACE_ID = ?)');
		params.push(this.param['TRACE_ID']);
	}
	else {
		throw new Error(`Unsupported MODE: ${mode}`);
	}

	this.outputQueryResults(sql + ' WHERE ' + where.join(' AND '), params);
};

JavaLink.run();
