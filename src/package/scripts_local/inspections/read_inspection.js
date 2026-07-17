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
	let sql = `
		SELECT
			i.ID,
			i.X,
			i.Y,
			l.program_type,
			s.asset_type,
			s.equipment,
			s.location,
			a.order_key,
            o.ORDERSUBKEY,
			o.ORDNBR,
            a.scheduled_ts,
            o.entry_ts,
            o.CUSTNAME,
            o.SPECNEEDS,
            o.RESPHONE,
            o.BUSPHONE,
            o.SVCRTACCT,
            o.SVCADDR,
            o.SVCCITY,
            o.SVCZIP,
            o.REQUEST1,
            o.REASONCD1,
            o.OUTAGETYPE orderdesc,
            o.workloccd,
            o.worklocdesc,
            o.CRUSRID,
            o.compltn_ts,
            a.priority,
            (case o.mat_required_flag when 'Y' then 'M' else '' end) as materials_required,
			i.id ATTACHMENTS,
			i.completion_status,
			i.issue_priority,
			i.comment,
			i.resolution_status,
			i.resolution_work_order,
			i.resolution_comment		
		FROM WM_INSPECTION i
		JOIN WM_INSPECTION_LIST l on i.list_id = l.id
		LEFT JOIN WM_ORDER o on l.program_order_key = o.order_key
		LEFT JOIN WM_ASSIGNMENT a ON A.ORDER_KEY = o.ORDER_KEY
		LEFT JOIN INSPECTION_SITE s ON i.asset_id = s.asset_id`;

	let where = [];
	let bind = [];

	if (this.param['EXCEPTIONS'] === 'true') {
		where.push("i.completion_status in ('U','X') and resolution_status is null");

		var programs = this.param['PROGRAMS'].split(',');
	    var programWhere = Sql.whereIn('l.program_type', programs);

	    where.push(programWhere);
	} else {
		where.push('i.list_id = ?');
		bind.push(this.param['LIST']);
	}

	sql = sql + ' WHERE ' + where.join(' AND ');
	this.outputQueryTreeResults(sql, {
		ATTACHMENTS: `SELECT
            F.ID,
            F.NAME,
            F.PATH,
            F.CREATE_TS,
            F.CREATOR,
            F.COMMENT
        FROM MWM_ORDER_FILE F
        WHERE F.INSPECTION_ID = ?`
	}, bind);
};

JavaLink.run();
