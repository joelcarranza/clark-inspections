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

JavaLink.queryPole = function() {
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
		FROM INSPECTION_SITE_POLE_VIEW`;

	let where = [];
	let bind = [];

	if (mode === 'criteria') {
		where.push('GLOBALID in (SELECT GLOBALID FROM INSPECTION_SITE_FEEDER WHERE FEEDERID = ?)');
		bind.push(this.param['CIRCUIT']);


	}
	else if (mode === 'proximity') {
		where.push('GEOM.STDistance(geometry::Point(?, ?, 2286)) <= ?');
		let meters = 0.3048 * +this.param['DISTANCE'];
		bind = bind.concat([this.param['X'], this.param['Y'], meters]);
	}
	else if (mode === 'globalid') {
		const ids = this.param['GLOBALID'].trim().split(/\s+/);
		where.push(Sql.whereIn('GLOBALID', ids));
	}
	else if (mode === 'trace') {
		where.push('GLOBALID in (SELECT GLOBALID FROM INSPECTION_SITE_TRACE_RESULTS WHERE TRACE_ID = ?)');
		bind.push(this.param['TRACE_ID']);
	}
	else {
		throw new Error(`Unsupported MODE: ${mode}`);
	}
	this.outputQueryResults(sql + ' WHERE ' + where.join(' AND '), bind);
};

JavaLink.queryElectricLine = function(types) {
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
		FROM INSPECTION_SITE_ELECTRIC_LINE_VIEW`;

	let where = [];
	let bind = [];

	if (mode === 'criteria') {
		where.push('GLOBALID in (SELECT GLOBALID FROM INSPECTION_SITE_FEEDER WHERE FEEDERID = ?)');
		bind.push(this.param['CIRCUIT']);


	}
	else if (mode === 'proximity') {
		where.push('GEOM.STDistance(geometry::Point(?, ?, 2286)) <= ?');
		let meters = 0.3048 * +this.param['DISTANCE'];
		bind = bind.concat([this.param['X'], this.param['Y'], meters]);
	}
	else if (mode === 'globalid') {
		const ids = this.param['GLOBALID'].trim().split(/\s+/);
		where.push(Sql.whereIn('GLOBALID', ids));
	}
	else if (mode === 'trace') {
		where.push('GLOBALID in (SELECT GLOBALID FROM INSPECTION_SITE_TRACE_RESULTS WHERE TRACE_ID = ?)');
		bind.push(this.param['TRACE_ID']);
	}
	else {
		throw new Error(`Unsupported MODE: ${mode}`);
	}

	var assetGroups = [];
	if(types.indexOf('PRIMARY') != -1) {
		assetGroups.push('202');
		assetGroups.push('203');
	}
	if(types.indexOf('SECONDARY') != -1) {
		assetGroups.push('302');
		assetGroups.push('303');
	}
	where.push(Sql.whereIn('ASSETGROUP', assetGroups, true));

	this.outputQueryResults(sql + ' WHERE ' + where.join(' AND '), bind);
};

JavaLink.process = function() {
	let types = this.param.TYPE ? this.param.TYPE.split(',') : ['POLE','PRIMARY', 'SECONDARY'];
	if(types.indexOf('POLE') != -1) {
		this.queryPole();
	}
	if(types.indexOf('PRIMARY') != -1 || types.indexOf('SECONDARY') != -1) {	
		this.queryElectricLine(types);
	}
};

JavaLink.run();
