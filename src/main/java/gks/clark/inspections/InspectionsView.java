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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import gks.clark.inspections.model.Inspection;
import gks.clark.inspections.model.InspectionTableModel;
import gks.form.Form;
import gks.form.util.AbstractView;
import gks.ui.GuiUtils;
import gks.ui.table.ArrayTableModel;
import gks.ui.table.TabularView;
import gks.ui.table.TabularViewSelectionListener;
import gks.util.DoubleClickGesture;
import gks.util.Utils;

public class InspectionsView extends AbstractView {

	protected Object values[];
	protected TabularView table;
	private InspectionTableModel tableModel;
	private WorkOrderDetailsEditor owner;
	private SwingWorker<Inspection[], Void> loadTask;
	private JPanel component;
	private JLabel label;
	private ProgressBar progressBar;
	private Inspection[] inspections;
	private String listId;

	public InspectionsView(Form form) {
		this.owner = (WorkOrderDetailsEditor)form.getOwner();
		table = new TabularView(getClass().getName());
		tableModel = new InspectionTableModel();
		table.setTableModel(tableModel);
		table.setTableColumnSet(Inspection.class.getName());
		table.addSelectionListener(ArrayTableModel.OBJECT_VALUE_COLUMN, new TabularViewSelectionListener() {
			public void selectionChanged(TabularView view, Collection<?> selectedValues, boolean isAdjusting) {
				if(!isAdjusting) {
					fireValueChanged();
				}
			}
		});
		table.getTable().addMouseListener(new DoubleClickGesture() {

			@Override
			public void onDoubleClick(MouseEvent e) {
				InspectionsView.this.onDoubleClick(e);
			}
		});

		label = new JLabel();
		progressBar = new ProgressBar(100, 20);

		Box north = Box.createHorizontalBox();
		north.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		north.add(label);
		north.add(Box.createHorizontalGlue());

		component = new JPanel(new BorderLayout(5, 5));
		component.add(table, BorderLayout.CENTER);
		component.add(north, BorderLayout.NORTH);


	}

	protected void onDoubleClick(MouseEvent e) {
		Collection<Inspection> sel = table.getSelection(ArrayTableModel.OBJECT_VALUE_COLUMN, Inspection.class);
		owner.openInspections(sel.toArray(new Inspection[0]));
	}



	public Object getValue() {
		return table.getSelection(ArrayTableModel.OBJECT_VALUE_COLUMN).toArray();
	}

	public Class<?> getType() {
		return Inspection[].class;
	}

	public void setValue(Object value) {
		table.select(ArrayTableModel.OBJECT_VALUE_COLUMN, Arrays.asList((Object[])value));
	}

	public JComponent component() {
		return component;
	}


	public TabularView getTabularView() {
		return table;
	}

	/**
	 * Set the preferred number of rows to shown in scroll pane. this is useful in setting the
	 * preferred size of the entire container and thus setting up a default height
	 */
	public void setVisibleRowCount(int rows) {
		Dimension d = table.getTable().getPreferredSize();
		table.getTable().setPreferredScrollableViewportSize(new Dimension(d.width,table.getTable().getRowHeight()*rows));
	}


	public void setListId(String listId) {
		if(!Utils.equals(this.listId, listId)) {
			this.listId = listId;
			if(Utils.isNotEmpty(listId)) {
				if(this.loadTask != null) {
					loadTask.cancel(false);
				}

				this.owner.setBusy(true);
				tableModel.setValues(new Inspection[0]);
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
							updateView(results);
						} catch (ExecutionException e) {
							GuiUtils.alert(component(), e);
						} catch (InterruptedException ignored) {
						}
					}
				};
				loadTask.execute();
			}
			else {
				tableModel.setValues(new Inspection[0]);
				owner.showInspectionsOnMap(new Inspection[0]);
			}
		}
	}

	protected void updateView(Inspection[] results) {
		this.inspections = results;
		owner.showInspectionsOnMap(results);
		tableModel.setValues(results);
		int completed = 0;
		int total = results.length;
		for(Inspection i : results) {
			if(i.isComplete()) {
				completed++;
			}
		}
		if(total > 0) {
			label.setText(completed + " / " + total);
			progressBar.setPercentage((float) completed / total);
			label.setIcon(progressBar);
		}
		else {
			label.setText("No assessments");
			label.setIcon(null);
		}
	}

	public Inspection[] getInspections() {
		return inspections;
	}
	
}
