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

JavaLink.useDefaultDataSource();

JavaLink.process = function() {
	this.outputQueryResults(`
		with list_stats as (
select 
    list_id, 
    count(*) as total,
    count(completion_status) as completed
from wm_inspection
group by list_id)
SELECT 
            o.order_key,
            o.ordertype,
            o.lon,
            o.lat,
            o.ORDERSUBKEY,
            o.ORDNBR,
            o.ordersubtype,
            o.entry_ts,
            o.HAZARD,
            o.HAZARDRMK1,
            case o.LIFESUPPORTIND when 'Y' then 'Y' else 'N' end,
            o.CUSTNAME,
            o.SPECNEEDS,
            o.RESPHONE,
            o.BUSPHONE,
            case o.CALLFIRST when 'Y' then 'Y' else 'N' end,
            o.SVCRTACCT,
            o.SVCADDR,
            o.SVCCITY,
            o.SVCZIP,
            o.CYCLE,
            o.ROUTE,
            case when o.feederid is not null then left(o.feederid, len(o.feederid)-1) end SUBSTATION,
            case when o.feederid is not null then '0'+right(o.feederid, 1) end FEEDER,
            o.GEOCODE,
            o.lighttype,
            o.REQUEST1,
            o.REASONCD1,
            o.OUTAGETYPE orderdesc,
            o.workloccd,
            o.worklocdesc,
            o.CRUSRID,
            l.id as list_id,
            ls.completed as completed,
            ls.total as total,
            a.assigned_to as crew
        FROM WM_ORDER o
        LEFT JOIN WM_LIST l on l.order_key = o.order_key
        LEFT JOIN LIST_STATS ls on l.id = ls.list_id
        LEFT JOIN WM_ASSIGNMENT a ON A.ORDER_KEY = o.ORDER_KEY
        WHERE o.compltn_ts is null
	`);
};

JavaLink.run();
