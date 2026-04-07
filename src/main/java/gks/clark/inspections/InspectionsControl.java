/*
 * InspectionApplicationControl - Jun 14, 2005
 * 
 *

 * NOTICES
 * -------
 *
 * Copyright 2000 by Gatekeeper Systems All Rights Reserved.
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
package gks.clark.inspections;

import java.io.File;

import gks.clark.inspections.model.Site;
import gks.clark.inspections.model.SiteFilter;
import gks.clark.inspections.model.SiteList;
import gks.control.BasicControl;
import gks.util.QueryBuilder;
import gks.util.dto.DataTransferException;

public class InspectionsControl extends BasicControl {
	private AsyncInspectionsControl proxy;

	public Site[] querySite(SiteFilter filter) throws DataTransferException {
		QueryBuilder q = new QueryBuilder();
		q.append("CIRCUIT", filter.getCircuit());
		return scriptQuery("/scripts/inspections/read_site", q, Site.class);
	}

	
	public void saveList(SiteList siteList, Site site[]) {
		
	}
	
	public AsyncInspectionsControl proxy() {
    	if(proxy == null) {
    		proxy = createProxy(AsyncInspectionsControl.class);
    	}
    	return proxy;
    }

}
