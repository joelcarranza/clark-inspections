/*
 * 
 *
 * NOTICES
 * -------
 *
 * Copyright 1999, 2000 by Gatekeeper Systems All Rights Reserved.
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

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import gks.clark.inspections.model.CriteriaSiteFilter;
import gks.clark.inspections.model.GlobalidSiteFilter;
import gks.clark.inspections.model.Inspection;
import gks.clark.inspections.model.InspectionTableModel;
import gks.clark.inspections.model.ProximitySiteFilter;
import gks.clark.inspections.model.Site;
import gks.clark.inspections.model.SiteList;
import gks.clark.inspections.model.SiteTableModel;
import gks.clark.inspections.model.WorkOrder;
import gks.clark.inspections.model.WorkOrderTableModel;
import gks.control.BasicControl;
import gks.form.chooser.Chooser;
import gks.form.details.DetailsEditor;
import gks.form.editor.Editor;
import gks.map.MapLayerSet;
import gks.form.ValueModel;
import gks.trace.TraceFeature;
import gks.trace.TraceMapSelectionWindow;
import gks.ui.GuiUtils;
import gks.ui.SimpleDialog;
import gks.ui.SwingProxy;
import gks.util.Location;
import gks.util.NavigateInterface;
import gks.util.TabularModule;

/**
 * Modules for inspections piece
 */
public class PlannerModule extends TabularModule implements PropertyChangeListener {

	private static final String WIN_INSPECTION_DETAIL = "inspectionDetail";
	private static final String WIN_FILTER = "winFilter";
	private static final String WIN_TRACE_FEATURE_SELECTOR = "traceFeatureSelector";
	private static final String WIN_WORK_ORDER_DETAIL = "workOrderDetail";

	private InspectionsControl control;

	static enum ItemType {
		SITE,
		INSPECTION, 
		WORK_ORDER
	}
	
	private ItemType visibleItemType;
	
	
	private String inspectionListKey;
	
	/**
	 * the work order selected when sites are queried. Used to provide context for further steps
	 */
	private WorkOrder activeWorkOrder;
	private LocalMapLayerManager<WorkOrder> workOrderLayerManager;
	private LocalMapLayerManager<Site> siteLayerManager;
	private LocalMapLayerManager<Inspection> inspectionLayerManager;

	public PlannerModule(NavigateInterface application) {
		super(application);
	}

	@Override
	public void start() throws Exception {
		super.start();
		configuration().inject(this);

		control = new InspectionsControl();

		control.addPropertyChangeListener(SwingProxy.createPropertyChangeListener(this));

		buildView();

		tableView().setTableColumnSet(Site.class.getName());
		
		actionViewWorkOrders(null);
		
		workOrderLayerManager = new LocalMapLayerManager<WorkOrder>(getMapControl(), "CPU Inspections - Work Order", "gks/clark/inspections/layer/WorkOrder.xml");

		siteLayerManager = new LocalMapLayerManager<Site>(getMapControl(), "CPU Inspections - Site", "gks/clark/inspections/layer/Site.xml");

		inspectionLayerManager = new LocalMapLayerManager<Inspection>(getMapControl(), "CPU Inspections", "gks/clark/inspections/layer/Inspection.xml");

	}

	@Override
	public void stop() throws Exception {
		super.stop();
		
		if(workOrderLayerManager != null) {
			workOrderLayerManager.destroy();
		}
		if(siteLayerManager != null) {
			siteLayerManager.destroy();
		}

	}

	public void chooseTraceMapFeature(ValueModel model) {
		Window w = windowManager().show(WIN_TRACE_FEATURE_SELECTOR);
		GuiUtils.setImplementation(w, ValueModel.class, model);
	}

	public void actionShowSiteFilter(ActionEvent e) {
		activeWorkOrder = tableView().getSelection(WorkOrder.class).iterator().next();

		windowManager().show(WIN_FILTER);
	}
	
	public void actionViewWorkOrders(ActionEvent e) {
		activeWorkOrder = null;
		
		control.proxy().queryWorkOrders()
		.onComplete(PlannerModule.this, "onWorkOrdersQueried").start();
	}

	public void actionViewExceptions(ActionEvent e) {
		control.proxy().queryExceptions()
				.onComplete(PlannerModule.this, "onInspectionsQueried", new Object[] { null }).start();
	}

	public void actionNewList(ActionEvent e) {
		SiteList siteList = new SiteList();
		siteList.setWorkOrder(activeWorkOrder.getKey());
		List<Site> sites = tableView().getData(Site.class);
		siteList.setSites(sites.toArray(new Site[0]));
		if (Editor.edit(this, siteList, "New List")) {
			control.proxy().createList(siteList).onComplete(this, "onListSaved").start();
		}
	}

	public void actionOpen(ActionEvent e) {
		if(visibleItemType == ItemType.INSPECTION) {
			Window window = windowManager().show(WIN_INSPECTION_DETAIL);
			DetailsEditor<Object> editor = DetailsEditor.forWindow(window);
			editor.view(tableView().getSelection());
		}
		else if(visibleItemType == ItemType.WORK_ORDER) {
			Window window = windowManager().show(WIN_WORK_ORDER_DETAIL);
			DetailsEditor<Object> editor = DetailsEditor.forWindow(window);
			editor.view(tableView().getSelection());
		}
	}

	public void actionOpenList(ActionEvent e) {
		// XXX: this is a hack
		String listKey = tableView().getSelection(WorkOrder.class).iterator().next().getListId();
		
		control.proxy().queryInspection(listKey)
				.onComplete(PlannerModule.this, "onInspectionsQueried", new Object[] { listKey }).start();

	}
	
	public void onWorkOrdersQueried(WorkOrder workOrders[]) {
		WorkOrderTableModel tableModel = new WorkOrderTableModel();
		tableModel.setValues(workOrders);
		tableView().setTableColumnSet(tableModel, WorkOrder.class.getName());

		visibleItemType = ItemType.WORK_ORDER;
		setTitle("Work Orders");

		workOrderLayerManager.setFeatures(Arrays.asList(workOrders));
		setMapLayer(new MapLayerSet(MapLayerSet.PHYSICAL, workOrderLayerManager.getLayerName()));
		
		actionManager().setConditional("workOrder", true);
		actionManager().setConditional("list", false);
		actionManager().setConditional("inspection", false);
		actionManager().setConditional("site", false);
	}

	

	public void onSitesQueried(Site site[], SiteFilter siteFilter) {
		this.inspectionListKey = null;
		
		

		
		SiteTableModel tableModel = new SiteTableModel();
		tableModel.setValues(site);
		tableView().setTableColumnSet(tableModel, Site.class.getName());

		visibleItemType = ItemType.SITE;
		
		workOrderLayerManager.setFeature(activeWorkOrder);
		
		siteLayerManager.setFeatures(Arrays.asList(site));
		setMapLayer(new MapLayerSet(MapLayerSet.PHYSICAL, siteLayerManager.getLayerName()));

		setTitle("Sites");

		actionManager().setConditional("workOrder", false);
		actionManager().setConditional("site", site.length > 0);
		actionManager().setConditional("inspection", false);
		actionManager().setConditional("list", false);
	}

	public void onInspectionsQueried(Inspection insp[], String listKey) {
		this.inspectionListKey = listKey;

		InspectionTableModel tableModel = new InspectionTableModel();
		tableModel.setValues(insp);
		tableView().setTableColumnSet(tableModel, Inspection.class.getName());

		visibleItemType = ItemType.INSPECTION;
		
		workOrderLayerManager.clear();
		siteLayerManager.clear();
		inspectionLayerManager.setFeatures(Arrays.asList(insp));
		setMapLayer(new MapLayerSet(MapLayerSet.PHYSICAL, inspectionLayerManager.getLayerName()));
		
		setTitle("Inspections");
		
		actionManager().setConditional("workOrder", false);
		actionManager().setConditional("list", true);
		actionManager().setConditional("inspection", insp.length > 0);
		actionManager().setConditional("site", false);
	}

	public void onListSaved(String listKey) {
		control.proxy().queryInspection(listKey)
				.onComplete(PlannerModule.this, "onInspectionsQueried", new Object[] { listKey }).start();
	}

	public Window createWindow(String name) {
		if (name.equals(WIN_FILTER)) {

			
			Chooser<SiteFilter> chooser = new Chooser<SiteFilter>(this) {
				@Override
				public void ok(SiteFilter filter) {
					if (filter instanceof ProximitySiteFilter) {
						ProximitySiteFilter pf = (ProximitySiteFilter) filter;
						Location mcs = getMapControl().mcsLocation(new Location(pf.getLon(), pf.getLat()));
						pf.setX(mcs.getX());
						pf.setY(mcs.getY());
					}
					control.proxy().querySite(filter)
							.onComplete(PlannerModule.this, "onSitesQueried", new Object[] { filter }).start();
				}
			};
			GuiUtils.setImplementation(chooser, PlannerModule.class, this);
			
			CriteriaSiteFilter cf = new CriteriaSiteFilter();
			cf.setCircuit(activeWorkOrder.getFeeder());
			chooser.addChoice(cf, "gks/clark/inspections/model/CriteriaSiteFilter.xml", "Sites by Criteria");

			ProximitySiteFilter pf = new ProximitySiteFilter();
			pf.setLon(activeWorkOrder.getX());
			pf.setLat(activeWorkOrder.getY());
			pf.setDistance(1000);
			chooser.addChoice(pf, "gks/clark/inspections/model/ProximitySiteFilter.xml", "Sites by Proximity");

			chooser.addChoice(new TraceSiteFilter(), "gks/clark/inspections/model/TraceSiteFilter.xml", "Sites by Trace");
			
			chooser.addChoice(new GlobalidSiteFilter(), "gks/clark/inspections/model/GlobalidSiteFilter.xml", "Sites by Globalid");
			
			chooser.setPreferredSizeFromChoiceIndex(0);
			SimpleDialog dlg = chooser.buildAsDialog(this);
			dlg.setTitle("Choose Sites...");
			dlg.setResizable(true);
			dlg.setDefaultCloseOperation(SimpleDialog.DISPOSE_ON_CLOSE);
			return dlg;
		} else if (name.equals(WIN_TRACE_FEATURE_SELECTOR)) {
			final TraceMapSelectionWindow window = new TraceMapSelectionWindow(this, "ELECTRIC");
			window.setAllowMultipleSelections(false);
			window.setTitle("Select start feature");
			window.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if ("ok".equals(cmd)) {
						ValueModel model = (ValueModel) GuiUtils.getImplementation(window, ValueModel.class);
						TraceFeature f = window.getSelectedFeature();
						if (f != null) {
							model.setValue(f);
							windowManager().show(WIN_FILTER);
						}
						window.close();
					} else {
						window.close();
					}
				}
			});
			return window;
		} else if (name.equals(WIN_INSPECTION_DETAIL)) {
			InspectionDetailsEditor view = new InspectionDetailsEditor(this);
			JFrame f = view.buildAsFrame();
			f.setTitle("Inspection");
			f.setPreferredSize(new Dimension(600, 400));
			return f;
		} 
		else if (name.equals(WIN_WORK_ORDER_DETAIL)) {
			WorkOrderDetailsEditor view = new WorkOrderDetailsEditor(this);
			JFrame f = view.buildAsFrame();
			f.setTitle("Work Order");
			f.setPreferredSize(new Dimension(600, 400));
			return f;
		} 
		else {
			throw new RuntimeException(name);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		Object value = evt.getNewValue();

		if (name == BasicControl.PROPERTY_BUSY) {
			setViewBusy(evt.getSource(), ((Boolean) value).booleanValue());
		} else if (name == BasicControl.PROPERTY_STATUS_MESSAGE) {
			setStatusMessage((String) value);
		} else if (name == BasicControl.PROPERTY_PROGRESS) {
			setProgress(((Integer) value).intValue());
		}
	}

	public InspectionsControl getControl() {
		return control;
	}

}
