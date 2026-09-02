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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.SwingWorker;

import gks.clark.inspections.model.Inspection;
import gks.clark.inspections.model.WorkOrder;
import gks.form.details.DetailsEditor;
import gks.util.TableMapSelectionMediator;
import gks.util.Utils;

/**
 * Editor window for {@link Inspection} object
 */
public class WorkOrderDetailsEditor extends DetailsEditor<WorkOrder> {
	private final PlannerModule module;
	private TableMapSelectionMediator inspectionMapSelectionMediator;

	public WorkOrderDetailsEditor(PlannerModule mobileWorkOrdersModule) {
		module = mobileWorkOrdersModule;
		setActionConfigPath("gks/clark/inspections/workOrderDetailAction.xml");
	}

	public void actionZoom(ActionEvent e) {
		WorkOrder call = (WorkOrder) getSelectedValue();
		module.mapCommand().layer(module.getMapLayer()).view(Collections.singleton(call), module.getMinZoomWidth())
				.run();
	}
	
	public InspectionsControl getControl() {
		return module.getControl();
	}

	@Override
	protected void setBusy(boolean busy) {
		super.setBusy(busy);
	}

	public void actionOpenAllInspections() {
		module.actionOpenList((WorkOrder) getSelectedValue());
	}
	
	
	public void openInspections(Inspection[] inspections) {
		Window window = module.windowManager().show(PlannerModule.WIN_INSPECTION_DETAIL);
		DetailsEditor<Object> editor = DetailsEditor.forWindow(window);
		editor.view(Arrays.asList(inspections));
	}

	public void showInspectionsOnMap(Inspection[] inspections) {
		module.setInspectionMapFeatures(inspections);
	}

	@Override
	protected void windowShown(Window window) {
		super.windowShown(window);
		if (inspectionMapSelectionMediator == null) {
			InspectionsView view = (InspectionsView) getVisibleForm().getComponentById("_inspectionsView");
			inspectionMapSelectionMediator = new TableMapSelectionMediator(view.getTabularView(), module.getInspectionMapSelection());
		}
		else {
			inspectionMapSelectionMediator.setEnabled(true);
		}
	}

	@Override
	protected void windowHidden(Window window) {
		super.windowHidden(window);
		if (inspectionMapSelectionMediator != null) {
			inspectionMapSelectionMediator.setEnabled(false);
		}
		module.setInspectionMapFeatures(new Inspection[0]);
	}

	@Override
	protected void updateView(WorkOrder value) {
		super.updateView(value);
		
		actionManager().setConditional("list", Utils.isNotEmpty(value.getListId()));
	}
	
	
	
	
}