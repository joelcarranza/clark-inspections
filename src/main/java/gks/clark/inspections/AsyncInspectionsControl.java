/*
 * AsyncInspectionApplicationControl - Mar 30, 2005
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

import gks.clark.inspections.model.Inspection;
import gks.clark.inspections.model.Program;
import gks.clark.inspections.model.Site;
import gks.clark.inspections.model.SiteList;
import gks.control.ControlTask;

/**
 * Async proxy for {@link InspectionsControl}
 */
public interface AsyncInspectionsControl {

	/**
	 * @see InspectionsControl#queryWorkOrder
	 */
	public ControlTask queryWorkOrders(Program programs[]);
	
	/**
	 * @see InspectionsControl#querySite(SiteFilter)
	 */
	public ControlTask querySite(SiteFilter f);

	/**
	 * @see InspectionsControl#queryInspection(String)
	 */
	public ControlTask queryInspection(String listKey);

	/**
	 * @see InspectionsControl#createList(SiteList)
	 */
	public ControlTask createList(SiteList siteList);

	/**
	 * @see InspectionsControl#save(Inspection)
	 */
	public ControlTask save(Inspection inspection);

	/**
	 * @see InspectionsControl#queryExceptions()
	 */
	public ControlTask queryExceptions(Program programs[]);

	public ControlTask attachFiles(File[] files);

}