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
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;

import gks.clark.inspections.model.CriteriaSiteFilter;
import gks.clark.inspections.model.Department;
import gks.clark.inspections.model.DisplaySettings;
import gks.clark.inspections.model.GlobalidSiteFilter;
import gks.clark.inspections.model.Inspection;
import gks.clark.inspections.model.InspectionTableModel;
import gks.clark.inspections.model.Program;
import gks.clark.inspections.model.ProximitySiteFilter;
import gks.clark.inspections.model.Site;
import gks.clark.inspections.model.SiteIdentifier;
import gks.clark.inspections.model.SiteList;
import gks.clark.inspections.model.SiteTableModel;
import gks.clark.inspections.model.WorkOrder;
import gks.clark.inspections.model.WorkOrderTableModel;
import gks.config.Configured;
import gks.control.BasicControl;
import gks.form.chooser.Chooser;
import gks.form.details.DetailsEditor;
import gks.form.editor.Editor;
import gks.map.MapLayerSet;
import gks.map.proxy.MGGeometry;
import gks.map.proxy.MGMapObject;
import gks.map.proxy.MGPoint;
import gks.form.ValueModel;
import gks.trace.TraceFeature;
import gks.trace.TraceMapSelectionWindow;
import gks.ui.GuiUtils;
import gks.ui.SimpleDialog;
import gks.ui.SwingProxy;
import gks.ui.table.ArrayTableModel;
import gks.util.Location;
import gks.util.NavigateInterface;
import gks.util.TabularModule;
import gks.util.lang.Tuple;

/**
 * Modules for inspections piece
 */
public class PlannerModule extends TabularModule implements PropertyChangeListener {

	static final String WIN_INSPECTION_DETAIL = "inspectionDetail";
	private static final String WIN_FILTER = "winFilter";
	private static final String WIN_TRACE_START_FEATURE_SELECTOR = "traceStartFeatureSelector";
	private static final String WIN_TRACE_STOP_FEATURE_SELECTOR = "traceStopFeatureSelector";

	private static final String WIN_WORK_ORDER_DETAIL = "workOrderDetail";

	private InspectionsControl control;

	static enum ItemType {
		SITE,
		INSPECTION, 
		WORK_ORDER
	}
	
	private ItemType visibleItemType;
	
	
	
	/**
	 * the work order selected when sites are queried. Used to provide context for further steps
	 */
	private WorkOrder activeWorkOrder;
	private LocalMapLayerManager<WorkOrder> workOrderLayerManager;
	private LocalMapLayerManager<Site> siteLayerManager;
	private LocalMapLayerManager<Inspection> inspectionLayerManager;
	private SiteFilter siteFilter;
	private String inspectionListKey;
	private Program[] visiblePrograms;
	
	@Configured
	Integer refreshRate;
	Timer refreshTimer;

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

		visiblePrograms = Program.all();
		
		actionViewWorkOrders();
		
		workOrderLayerManager = new LocalMapLayerManager<WorkOrder>(getMapControl(), "CPU Inspections - Work Order", "gks/clark/inspections/layer/WorkOrder.xml", false);

		siteLayerManager = new LocalMapLayerManager<Site>(getMapControl(), "CPU Inspections - Site", "gks/clark/inspections/layer/Site.xml", true);

		inspectionLayerManager = new LocalMapLayerManager<Inspection>(getMapControl(), "CPU Inspections", "gks/clark/inspections/layer/Inspection.xml", false) {

			@Override
			protected void createMapObject(Inspection insp) {
				if(insp.isMappable()) {
					MGGeometry geometry = geometryForObject(insp);
					String style = "default";
					if(insp.getCompletionStatus() != null) {
						String compCode = insp.getCompletionStatus().getCode();
						String priCode = insp.getIssuePriority() != null ? insp.getIssuePriority().getCode() : null;
						if("X".equals(compCode)) {
							if("C".equals(priCode)) {
								style="critical";
							}
							else {
								style = "exception";
							}
						}
					}
					layer.createMapObject(insp.getMapKey(),null,null,style,geometry,mcs);
				}
			}
			
		};

		if (refreshRate != null && refreshRate > 0) {
			refreshTimer = new javax.swing.Timer(1000 * 60 * refreshRate, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					actionRefresh();
				}
			});
			refreshTimer.start();
		}
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
		if(inspectionLayerManager != null) {
			inspectionLayerManager.destroy();
		}
		
		if(refreshTimer != null) {
			refreshTimer.stop();
		}

	}

	public void chooseTraceMapFeature(ValueModel model) {
		Window w = windowManager().show(WIN_TRACE_START_FEATURE_SELECTOR);
		GuiUtils.setImplementation(w, ValueModel.class, model);
	}
	
	public void chooseStopTraceMapFeature(ValueModel model) {
		Window w = windowManager().show(WIN_TRACE_STOP_FEATURE_SELECTOR);
		GuiUtils.setImplementation(w, ValueModel.class, model);
	}

	public void actionRefresh() {
		// this is pretty gross
		if(visibleItemType == ItemType.WORK_ORDER) {
			actionViewWorkOrders();
		}
		else if(visibleItemType == ItemType.SITE) {
			setSiteFilter(siteFilter);
		}
		else if(visibleItemType == ItemType.INSPECTION) {
			if(inspectionListKey != null) {
				control.proxy().queryInspection(inspectionListKey)
				.onComplete(PlannerModule.this, "onInspectionsQueried", new Object[] { inspectionListKey }).start();
			}
			else {
				actionViewExceptions();
			}
		}
		
	}
	public void actionShowSiteFilter() {
		activeWorkOrder = tableView().getSelection(WorkOrder.class).iterator().next();

		windowManager().show(WIN_FILTER);
	}
	
	public void actionViewWorkOrders() {
		activeWorkOrder = null;
		
		control.proxy().queryWorkOrders(visiblePrograms)
		.onComplete(PlannerModule.this, "onWorkOrdersQueried").start();
	}

	public void actionViewExceptions() {
		control.proxy().queryExceptions(visiblePrograms)
				.onComplete(PlannerModule.this, "onExceptionsQueried").start();
	}

	public void actionNewList() {
		SiteList siteList = new SiteList();
		siteList.setWorkOrder(activeWorkOrder.getKey());
		siteList.setProgram(activeWorkOrder.getProgram());
		// just a default
		siteList.setDepartment(Department.lookup("LINE"));

		ArrayList<SiteIdentifier> siteIdentifiers = new ArrayList<SiteIdentifier>();
		for(Site s : tableView().getData(Site.class)) {
			SiteIdentifier si = new SiteIdentifier();
			si.setAssetID(s.getAssetID());
			si.setAssetType(s.getAssetType());
			double x = (s.getMinX() + s.getMaxX()) / 2;
			double y = (s.getMinY() + s.getMaxY()) / 2;
			MGPoint ll = getMapControl().getMap().mcsToLonLat(x,y);
			si.setLon(ll.getX());
			si.setLat(ll.getY());	
			siteIdentifiers.add(si);
		}
		siteList.setSites(siteIdentifiers.toArray(new SiteIdentifier[0]));
		if (Editor.edit(this, siteList, "New List")) {
			control.proxy().createList(siteList).onComplete(this, "onListSaved").start();
		}
	}

	public void actionAddSites() {
		// ortho:3 (Primary),ortho:11 (secondary), logical:Pole  
		MapLayerSet layers[] = new MapLayerSet[] {
			new MapLayerSet("ortho", "3"), // primary 
			new MapLayerSet("ortho", "11"), // secondary
			new MapLayerSet("logical", "Pole"), // Pole
		};
		
		List<MGMapObject> mapObjects = new ArrayList<MGMapObject>();
		for(MapLayerSet mls : layers) {
			mapObjects.addAll(Arrays.asList(getMapControl().getMapObjects(mls)));
		}
		if(!mapObjects.isEmpty()) {
			MapObjectQuery q = MapObjectQuery.fromMapObjects(mapObjects.toArray(new MGMapObject[0]));
			control.proxy().querySite(q)
			.onComplete(PlannerModule.this, "onSitesAdded").start();
		}
		else {
			alert("No available sites in map selection");
		}

	}

	public void actionRemoveSites() {
		Site[] sites = tableView().getSelection().toArray(new Site[0]);
		if(sites.length > 0) {
			SiteTableModel model = tableView().getTable().getModel(SiteTableModel.class);
			model.removeValues(sites);		
			siteLayerManager.removeFeatures(Arrays.asList(sites));
		}
	}

	public void actionDisplaySettings(ActionEvent e) {
		
		DisplaySettings displaySettings = new DisplaySettings();
		displaySettings.setPrograms(visiblePrograms);

		if (Editor.edit(this, displaySettings, "Display Settings")) {
			this.visiblePrograms = displaySettings.getPrograms();
			actionRefresh();
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
		WorkOrder wo = tableView().getSelection(WorkOrder.class).iterator().next();
		actionOpenList(wo);
	}
	
	public void actionOpenList(WorkOrder workOrder) {
		String listKey = workOrder.getListId();
		
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
		siteLayerManager.clear();
		inspectionLayerManager.clear();
		
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
		inspectionLayerManager.clear();
		
		setMapLayer(new MapLayerSet(MapLayerSet.PHYSICAL, siteLayerManager.getLayerName()));

		setTitle("Sites");

		actionManager().setConditional("workOrder", false);
		actionManager().setConditional("site", site.length > 0);
		actionManager().setConditional("inspection", false);
		actionManager().setConditional("list", false);
	}
	
	public void onSitesAdded(Site site[]) {
		Set<Tuple> siteKeys = new HashSet<Tuple>();
		for(Site s : tableView().getData(Site.class)) {
			siteKeys.add(new Tuple(s.getAssetType(), s.getAssetID()));
		}
		List<Site> sitesToAdd = new ArrayList<Site>();
		for(Site s : site) {
			Tuple k = new Tuple(s.getAssetType(), s.getAssetID());
			if(!siteKeys.contains(k)) {
				sitesToAdd.add(s);
				siteKeys.add(k);
			}
		}
		
		if(!sitesToAdd.isEmpty()) {		
			SiteTableModel tableModel = tableView().getTable().getModel(SiteTableModel.class);
			tableModel.addValues(sitesToAdd.toArray());
			actionManager().setConditional("site", site.length > 0);
			
			siteLayerManager.addFeatures(sitesToAdd);
		}
	}

	public void onExceptionsQueried(Inspection insp[]) {
		this.inspectionListKey = null;

		InspectionTableModel tableModel = new InspectionTableModel();
		tableModel.setValues(insp);
		tableView().setTableColumnSet(tableModel, Inspection.class.getName()+"-exception");

		visibleItemType = ItemType.INSPECTION;
		
		workOrderLayerManager.clear();
		siteLayerManager.clear();
		inspectionLayerManager.setFeatures(Arrays.asList(insp));
		setMapLayer(new MapLayerSet(MapLayerSet.PHYSICAL, inspectionLayerManager.getLayerName()));
		
		setTitle("Exceptions");
		
		actionManager().setConditional("workOrder", false);
		actionManager().setConditional("list", true);
		actionManager().setConditional("inspection", insp.length > 0);
		actionManager().setConditional("site", false);
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
					setSiteFilter(filter);
				}
			};
			GuiUtils.setImplementation(chooser, PlannerModule.class, this);
			
			// Jon requested no default types, he wants users to explicitly choose
			AssetTypeFilter defaultTypes[] = new AssetTypeFilter[0];
			
			CriteriaSiteFilter cf = new CriteriaSiteFilter();
			cf.setTypes(defaultTypes);
			chooser.addChoice(cf, "gks/clark/inspections/model/CriteriaSiteFilter.xml", "Sites by Criteria");

			ProximitySiteFilter pf = new ProximitySiteFilter();
			pf.setLon(activeWorkOrder.getX());
			pf.setLat(activeWorkOrder.getY());
			pf.setDistance(100);
			pf.setTypes(defaultTypes);
			chooser.addChoice(pf, "gks/clark/inspections/model/ProximitySiteFilter.xml", "Sites by Proximity");

			TraceSiteFilter tsf = new TraceSiteFilter();
			tsf.setTypes(defaultTypes);
			chooser.addChoice(tsf, "gks/clark/inspections/TraceSiteFilter.xml", "Sites by Trace");
			
			GlobalidSiteFilter gsf = new GlobalidSiteFilter();
			chooser.addChoice(gsf, "gks/clark/inspections/model/GlobalidSiteFilter.xml", "Sites by Globalid");
			
			chooser.setPreferredSize(new Dimension(550, 525));
			
			SimpleDialog dlg = chooser.buildAsDialog(this);
			dlg.setTitle("Choose Sites...");
			dlg.setResizable(true);
			dlg.setDefaultCloseOperation(SimpleDialog.DISPOSE_ON_CLOSE);
			return dlg;
		} 
		else if (name.equals(WIN_TRACE_START_FEATURE_SELECTOR)) {
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
		}
		else if (name.equals(WIN_TRACE_STOP_FEATURE_SELECTOR)) {
			final TraceMapSelectionWindow window = new TraceMapSelectionWindow(this, "ELECTRIC");
			window.setAllowMultipleSelections(true);
			window.setTitle("Select start feature");
			window.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TraceMapSelectionWindow w = (TraceMapSelectionWindow)e.getSource();
					String cmd = e.getActionCommand();
					ValueModel model = (ValueModel) GuiUtils.getImplementation(window, ValueModel.class);
					if(cmd.equals(SimpleDialog.CMD_OK)) {
						List<TraceFeature> stopFeatures = new ArrayList<TraceFeature>(Arrays.asList((TraceFeature[])model.getValue()));
						for(TraceFeature selectedFeature : w.getSelectedFeatures()) {
							boolean existingStopFeature = false;
							for(TraceFeature f : stopFeatures) {
								if(f.equivalent(selectedFeature)) {
									existingStopFeature = true;
								}
							}
							if(!existingStopFeature) {
								// rebuild object to assign sequence number
								stopFeatures.add(new TraceFeature(selectedFeature.getMapObject(), selectedFeature.getFeatureSource()));
							}
						}
						model.setValue(stopFeatures.toArray(new TraceFeature[0]));
						w.close();
					}
					else if(cmd.equals("none")) {
						model.setValue(new TraceFeature[0]);
						w.close();
					}
					else if(cmd.equals(SimpleDialog.CMD_CANCEL)) {
						w.close();
					}
				}
			});
			return window;
		}
		else if (name.equals(WIN_INSPECTION_DETAIL)) {
			InspectionDetailsEditor view = new InspectionDetailsEditor(this);
			JFrame f = view.buildAsFrame();
			f.setTitle("Inspection");
			f.setPreferredSize(new Dimension(600, 650));
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

	protected void setSiteFilter(SiteFilter filter) {
		this.siteFilter = filter;
		
		control.proxy().querySite(filter)
				.onComplete(PlannerModule.this, "onSitesQueried", new Object[] { filter }).start();
	}

	
	public void inspectionUpdated(Inspection selectedValue) {
		// may be smarter in the future
		actionRefresh();
	}

	
	

}
