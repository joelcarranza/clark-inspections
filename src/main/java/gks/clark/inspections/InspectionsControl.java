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
import java.util.Properties;

import gks.field.control.ControlException;
import gks.field.list.FieldListControl;
import gks.field.storage.PersistentCacheManager;

public class InspectionsControl extends FieldListControl {





	public void initialize(PersistentCacheManager cacheManager, File cacheRoot) throws ControlException {
		super.initializeWithCacheManager(cacheManager);
		setFileRoot(new File(cacheRoot, "files"));
		setOnline(true);
	}


	
	File fileRoot = null;

	public static final String PROPERTY_SIGNED_IN_TO_POWER_ON = "signedInToPowerOn";

	
	
	public File getFileRoot() {
		return fileRoot;
	}




	public void setFileRoot(File fileRoot) {
		this.fileRoot = fileRoot;
	}





}
