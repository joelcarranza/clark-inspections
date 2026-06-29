/*
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gks.form.EnabledProperty;
import gks.form.Form;
import gks.form.anno.FactoryParam;
import gks.form.util.AbstractView;
import gks.trace.TraceFeature;
import gks.trace.TraceFeatureValueRenderer;
import gks.ui.ArrayListModel;
import gks.ui.GuiUtils;
import gks.ui.ValueRenderer;
import gks.ui.ValueRendererListAdapter;
import gks.ui.layout.TableLayout;
import gks.ui.table.ArrayTableModel;
import gks.util.ReflectionUtils;
import gks.util.Utils;

/**
 * Shows a list of TraceFeature used as stop features. The value is the data
 * shown not the selection. 
 * 
 * @since 3.3.0
 */
public class StopFeatureListView extends AbstractView implements EnabledProperty, ActionListener, ListSelectionListener {
	private TraceFeature[] value = new TraceFeature[0];
	private final JPanel component;
	private final JLabel label;
	private final JButton addButton;
	private final JButton removeButton;
	private final JButton clearButton;
	private final JScrollPane pane;

	private final JList box;
	private PlannerModule owner;
	public StopFeatureListView(Form form, @FactoryParam(value="valueRendererClass") Class<ValueRenderer> valueRendererClass) {
		owner = (PlannerModule) form.getOwner();
		box = new JList() {

			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
			
		};
		box.getSelectionModel().addListSelectionListener(this);
		box.setVisibleRowCount(5);
		ValueRenderer valueRenderer = valueRendererClass != null ? ReflectionUtils.invokeConstructor(valueRendererClass) : new TraceFeatureValueRenderer();
		box.setCellRenderer(new ValueRendererListAdapter(valueRenderer));
		pane = new JScrollPane(box,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel header = new JPanel(new TableLayout(new double[] {
			TableLayout.PREFERRED,
			6,
			TableLayout.FILL,
			1,
			TableLayout.PREFERRED,
			6,
			TableLayout.PREFERRED			
		},new double[] {TableLayout.PREFERRED}));
		
		addButton = GuiUtils.createIconButton("gks/trace/images/add.png");
		addButton.setToolTipText("Add stop features");
		addButton.addActionListener(this);
		header.add(addButton,"0,0,0,0");
		
		label = new JLabel("Stop Features");
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				owner.chooseStopTraceMapFeature(StopFeatureListView.this);
			}
			
		});
		header.add(label,"2,0,2,0");
		
		removeButton = GuiUtils.createIconButton("gks/trace/images/remove.png");
		removeButton.setToolTipText("Remove selected stop features");
		removeButton.addActionListener(this);
		
		header.add(removeButton,"4,0,4,0");
		
		clearButton = GuiUtils.createIconButton("gks/trace/images/clear.png");
		clearButton.setToolTipText("Remove all stop features");
		clearButton.addActionListener(this);
		header.add(clearButton,"6,0,6,0");
		
		component = new JPanel(new BorderLayout(0,5));
		component.add(pane,BorderLayout.CENTER);
		component.add(header,BorderLayout.NORTH);
		
		removeButton.setEnabled(false);
		pane.setVisible(this.value.length > 0);
		removeButton.setVisible(this.value.length > 0);
		clearButton.setVisible(this.value.length > 0);
		
	}
	
	public JComponent component() {
		return component;
	}

	
	public Class<TraceFeature[]> getType() {
		return TraceFeature[].class;
	}

	
	public Object getValue() {
		return value;
	}

	
	public void setValue(Object value) {		
		TraceFeature oldValue[] =  this.value;
		TraceFeature[] data = value != null ? (TraceFeature[])value : new TraceFeature[0];
		if(!Utils.arrayEquals(oldValue, data)) {
			box.setModel(new ArrayListModel(data));
			this.value = data;

			pane.setVisible(this.value.length > 0);
			
			removeButton.setVisible(this.value.length > 0);
			removeButton.setEnabled(box.getSelectedIndex() != -1);
			
			clearButton.setVisible(this.value.length > 0);
			component.validate();
			if(component.getParent() != null) {
				component.getParent().validate();
			}
			fireValueChanged();
		}
	}

	public void setEnabled(boolean enabled) {
		addButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
		clearButton.setEnabled(enabled);
		box.setEnabled(enabled);
	}
	
	public boolean isEnabled() {
		return box.isEnabled();
	}
	
	public void setVisibleRowCount(int visibleRowCount) {
		box.setVisibleRowCount(visibleRowCount);
	}
	
	
	public void valueChanged(ListSelectionEvent e) {
		removeButton.setEnabled(box.getSelectedIndex() != -1);
	}

	
	
	public void actionPerformed(ActionEvent e) {
		Object button = e.getSource();
		if(button == addButton) {
			owner.chooseStopTraceMapFeature(StopFeatureListView.this);
		}
		else if(button == removeButton) {
			TraceFeature traceFeatureToRemove = (TraceFeature)box.getSelectedValue();
			removeTraceFeature(traceFeatureToRemove);
		}
		else if(button == clearButton) {
			setValue(null);
		}
		else {
			throw new RuntimeException();
		}
	}

	/**
	 * Remove a single traceFeature from values. 
	 */
	private void removeTraceFeature(TraceFeature traceFeatureToRemove) {
		// this is slightly more complicated than you might expect because 
		// of the sequence numbers must be maintained

		boolean changed = false;			
		List<TraceFeature> values = new ArrayList<TraceFeature>();
		for(TraceFeature f : value) {
			if(f == traceFeatureToRemove) {
				changed = true;
				continue;
			}
			else {
				values.add(new TraceFeature(f.getMapObject(), f.getFeatureSource()));
			}
		}
		if(changed) {
			setValue(values.toArray(new TraceFeature[0]));
		}
	}
}
