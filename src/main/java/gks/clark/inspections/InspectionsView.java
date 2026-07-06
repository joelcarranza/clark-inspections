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
 
 package gks.clark.inspections;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gks.clark.inspections.model.Inspection;
import gks.clark.inspections.model.InspectionTableModel;
import gks.config.table.TableColumnSet;
import gks.form.Form;
import gks.form.util.AbstractView;
import gks.ui.GKSTable;
import gks.ui.GuiUtils;
import gks.ui.table.ArrayTableModel;
import gks.util.DoubleClickGesture;
import gks.util.TableSorter;
import gks.util.Utils;

public class InspectionsView extends AbstractView implements ListSelectionListener {

	protected Object values[];
	protected GKSTable table;
	protected JScrollPane scroll;
	private WorkOrderDetailsEditor owner;
	private SwingWorker<Inspection[], Void> loadTask;
	
	public InspectionsView(Form form) {
		this.owner = (WorkOrderDetailsEditor)form.getOwner();
		table = new GKSTable();
		scroll = new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ArrayTableModel model = new InspectionTableModel();
		TableSorter sort = new TableSorter(model);
		table.setModel(sort);
		sort.addMouseListenerToHeaderInTable(table);
		TableColumnSet tcs = TableColumnSet.lookup(Inspection.class.getName());
		tcs.install(table);
		table.getSelectionModel().addListSelectionListener(this);
		table.addMouseListener(new DoubleClickGesture() {
			
			@Override
			public void onDoubleClick(MouseEvent e) {
				InspectionsView.this.onDoubleClick(e);
			}
		});
	
	}
	
	protected void onDoubleClick(MouseEvent e) {
		Inspection[] sel = (Inspection[]) table.getSelectedValues(ArrayTableModel.OBJECT_VALUE_COLUMN);
		owner.openInspections(sel);
	}



	public Object getValue() {
		Object[] sel = table.getSelectedValues(ArrayTableModel.OBJECT_VALUE_COLUMN);
		return sel;
	}

	public Class<?> getType() {
		return Inspection[].class;
	}

	public void setValue(Object value) {
		table.setSelectedValues(ArrayTableModel.OBJECT_VALUE_COLUMN, new HashSet<Object>(Arrays.asList((Object[])value)));
	}

	public JComponent component() {
		return scroll;
	}
	

	/**
	 * Set the preferred number of rows to shown in scroll pane. this is useful in setting the
	 * preferred size of the entire container and thus setting up a default height
	 */	
	public void setVisibleRowCount(int rows) {
		Dimension d = table.getPreferredSize();
		table.setPreferredScrollableViewportSize(new Dimension(d.width,table.getRowHeight()*rows));
	}


	public void valueChanged(ListSelectionEvent e) {
		if(!e.getValueIsAdjusting()) {
			fireValueChanged();
		}
	}

	
	public void setListId(String listId) {
		if(Utils.isNotEmpty(listId)) {
			if(this.loadTask != null) {
				loadTask.cancel(false);
			}
			
			this.owner.setBusy(true);
			table.getModel(InspectionTableModel.class).setValues(new Inspection[0]);
			loadTask = new SwingWorker<Inspection[], Void>() {
	
				@Override
				protected Inspection[] doInBackground() throws Exception {
					return owner.getControl().queryInspection(listId);
				}
	
				@Override
				protected void done() {
					if(isCancelled()) {
						return;
					}
					InspectionsView.this.owner.setBusy(false);
					try {
						Inspection[] results = get();
						table.getModel(InspectionTableModel.class).setValues(results);
					} catch (ExecutionException e) {
						GuiUtils.alert(component(), e);
					} catch (InterruptedException ignored) {
					}
				}
			};
			loadTask.execute();
		}
		else {
			table.getModel(InspectionTableModel.class).setValues(new Inspection[0]);
		}
		
	}
	
}
