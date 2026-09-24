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

JavaLink.queryStructureJunction = function(types) {
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
		FROM INSPECTION_SITE_STRUCTURE_JUNCTION_VIEW`;

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

	var whereType = [];
	if(types.indexOf('POLE_PRIMARY') != -1) {
		whereType.push('ASSETGROUP = 201');
	}
	if(types.indexOf('POLE_SECONDARY') != -1) {
		whereType.push('ASSETGROUP = 301');
	}
	if(types.indexOf('POLE_TRANSMISSION') != -1) {
		whereType.push('ASSETGROUP = 101');
	}
	if(types.indexOf('VAULT') != -1) {
		whereType.push('(ASSETGROUP = 903 AND ASSETTYPE IN (601))');
	}
	if(whereType) {
		where.push('('+whereType.join(' OR ')+')');
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
		where.push('CIRCUIT = ?');
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

	var whereType = [];
	if(types.indexOf('OH_PRIMARY') != -1) {
		whereType.push('(ASSETGROUP = 202 AND ASSETTYPE IN (210,211,212))');
	}
	if(types.indexOf('UG_PRIMARY') != -1) {
		whereType.push('(ASSETGROUP = 202 AND ASSETTYPE NOT IN (210,211,212))');
	}
	if(types.indexOf('OH_SECONDARY') != -1) {
		whereType.push('(ASSETGROUP = 302 AND ASSETTYPE IN (311,312,313))');
	}
	if(types.indexOf('UG_SECONDARY') != -1) {
		whereType.push('(ASSETGROUP = 302 AND ASSETTYPE NOT IN (311,312,313))');
	}
	if(whereType) {
		where.push('('+whereType.join(' OR ')+')');
	}

	this.outputQueryResults(sql + ' WHERE ' + where.join(' AND '), bind);
};

JavaLink.queryDevice = function(types) {
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
		FROM INSPECTION_SITE_ELECTRIC_DEVICE_VIEW`;

	let where = [];
	let bind = [];

	if (mode === 'criteria') {
		where.push('CIRCUIT = ?');
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

	var whereType = [];
	if(types.indexOf('OH_TRANSFORMER') != -1) {
		whereType.push('(ASSETGROUP = 212 AND ASSETTYPE IN (311,312,313,314))');
	}
	if(types.indexOf('UG_TRANSFORMER') != -1) {
		whereType.push('(ASSETGROUP = 212 AND ASSETTYPE IN (316,317,318))');
	}
	if(types.indexOf('OH_SWITCH') != -1) {
		whereType.push('(ASSETGROUP = 211 AND ASSETTYPE IN (301,302))');
	}
	if(types.indexOf('UG_SWITCH') != -1) {
		whereType.push('(ASSETGROUP = 211 AND ASSETTYPE IN (303))');
	}
	if(types.indexOf('TRANSMISSION_SWITCH') != -1) {
		whereType.push('ASSETGROUP = 107');
	}
	if(whereType) {
		where.push('('+whereType.join(' OR ')+')');
	}

	this.outputQueryResults(sql + ' WHERE ' + where.join(' AND '), bind);
};

JavaLink.process = function() {
	let types = this.param.TYPE ? this.param.TYPE.split(',') : ['POLE_PRIMARY','POLE_SECONDARY', 'UG_PRIMARY', 'OH_PRIMARY', 'UG_SECONDARY', 'OH_SECONDARY', 'OH_TRANSFORMER', 'UG_TRANSFORMER', 'POLE_TRANSMISSION', 'VAULT', 'OH_SWITCH', 'UG_SWITCH', 'TRANSMISSION_SWITCH'];
	if(types.indexOf('POLE_PRIMARY') != -1 || types.indexOf('POLE_SECONDARY') != -1 || types.indexOf('POLE_TRANSMISSION') != -1 || types.indexOf('VAULT') != -1) {
		this.queryStructureJunction(types);
	}
	if(types.indexOf('UG_PRIMARY') != -1 || types.indexOf('OH_PRIMARY') != -1 || types.indexOf('UG_SECONDARY') != -1 || types.indexOf('OH_SECONDARY') != -1) {
		this.queryElectricLine(types);
	}
	if(types.indexOf('OH_TRANSFORMER') != -1 || types.indexOf('UG_TRANSFORMER') != -1 || types.indexOf('OH_SWITCH') != -1 || types.indexOf('UG_SWITCH') != -1 || types.indexOf('TRANSMISSION_SWITCH') != -1) {
		this.queryDevice(types);
	}
};

JavaLink.run();
