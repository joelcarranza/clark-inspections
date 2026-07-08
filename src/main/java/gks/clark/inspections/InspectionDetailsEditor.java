/*
 * NOTICES
 * -------
 *
 * Copyright 2011 by Gatekeeper Systems All Rights Reserved.
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
 *     URL:    http://www.gatekeeper.com
 */

/**
* 
*/
package gks.clark.inspections;

import java.awt.event.ActionEvent;
import java.util.Collections;

import javax.swing.SwingWorker;

import gks.clark.inspections.model.Inspection;
import gks.form.details.DetailsEditor;
import gks.util.concurrent.ObservableFuture;

/**
 * Editor window for {@link Inspection} object
 */
public class InspectionDetailsEditor extends DetailsEditor<Inspection> {
	private final PlannerModule module;

	public InspectionDetailsEditor(PlannerModule mobileWorkOrdersModule) {
		module = mobileWorkOrdersModule;
		setActionConfigPath("gks/clark/inspections/inspectionDetailAction.xml");
	}

	public void actionZoom(ActionEvent e) {
		Inspection call = (Inspection) getSelectedValue();
		module.mapCommand().layer(module.getMapLayer()).view(Collections.singleton(call), module.getMinZoomWidth())
				.run();
	}
	
	public AsyncInspectionsControl getControlProxy() {
		return module.getControl().proxy();
	}
	
	@Override
	protected SwingWorker<?, ?> createSaveTask(Inspection value) {
		return module.getControl().proxy().save(value).view(getVisibleForm().component());
	}

	@Override
	protected void saveTaskCompleted(Runnable actionOnComplete) {
		module.inspectionUpdated(getSelectedValue());
		super.saveTaskCompleted(actionOnComplete);
	}
	
	
}