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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import gks.form.EnabledProperty;
import gks.form.Form;
import gks.form.util.AbstractView;
import gks.map.MapLayerSet;
import gks.map.proxy.MGMapObject;
import gks.organizer.OrganizerDialog;
import gks.ui.GKSTextField;
import gks.ui.GuiUtils;
import gks.ui.layout.TableLayout;
import gks.ui.layout.TableLayoutConstraints;
import gks.util.ServerLink;
import gks.util.SimpleCode;
import gks.util.Utils;
import gks.util.dto.ServerLinkInputStream;
import gks.util.lang.ExceptionUtils;

/**
 * OrganizerView with attached "Pick Circuits" button which allows a user to 
 * select a circuit from the map
 */
public class CircuitPicker extends AbstractView implements ActionListener, EnabledProperty {
	private JPanel component;
	private JButton openButton;
	private GKSTextField field;
	private OrganizerDialog dlg;	
	private int id;
	private SimpleCode value;
	private String title;
	private JButton pickButton;
	private PlannerModule module;
	private String circuitedMapLayers;

	public CircuitPicker(Form form) {
		module = (PlannerModule)form.getOwner();
		component = new JPanel(new TableLayout(new double[] {TableLayout.FILL,TableLayout.PREFERRED,TableLayout.PREFERRED},
				new double[] {TableLayout.PREFERRED}));		
		this.openButton = GuiUtils.createIconButton("gks/images/open.png");
		this.openButton.setActionCommand("open");
		this.openButton.addActionListener(this);
		
		
		this.pickButton = GuiUtils.createIconButton("gks/clark/inspections/images/query.gif");
		this.pickButton.setActionCommand("pick");
		this.pickButton.addActionListener(this);
		this.pickButton.setEnabled(module.getMapControl() != null);
		this.pickButton.setToolTipText(toolTipTextForPickButton());
		
		TableLayoutConstraints c = new TableLayoutConstraints();
		component.add(field = new GKSTextField(),c);
		field.setEditable(false);
		
		c.col1 = c.col2 = 1;
		component.add(openButton,c);
		c.col1 = c.col2 = 2;
		component.add(pickButton,c);
	}
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("open")) {
			Component target = (Component) e.getSource();
			if(dlg == null) {
				dlg = OrganizerDialog.create(target,id);
				dlg.addActionListener(new ActionListener() {
	
					public void actionPerformed(ActionEvent e) {
						setValue(new SimpleCode(dlg.getSelectedItemKey(),dlg.getSelectedItemLabel()));
					}			
				});			
				dlg.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			}
			if(title != null)
				dlg.setTitle(title);
			dlg.setVisible(true);		
		}
		else if(cmd.equals("pick")) {
			List<MGMapObject> mapObjects = new ArrayList<MGMapObject>();
			for(MapLayerSet ms : circuitMapLayerSets()) {
				MGMapObject f[] = module.getMapControl().getSelectedMapObjects(ms);
				mapObjects.addAll(Arrays.asList(f));
			}
			if(!mapObjects.isEmpty()) {
				setCircuitFromMapObjects(mapObjects.toArray(new MGMapObject[0]));
			}
			else {
				GuiUtils.alert(component(),"You must select primary conductor on the map");
			}
		}
		else if(cmd.equals("close")) {
			setValue(null);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	
	public JButton getButton() {
		return openButton;
	}

	public void setIcon(Icon defaultIcon) {
		openButton.setIcon(defaultIcon);
	}

	public void setText(String text) {
		openButton.setText(text);
	}
	
	public void setEnabled(boolean enabled) {
		field.setEnabled(enabled);
		openButton.setEnabled(enabled);
	}
	
	public boolean isEnabled() {
		return field.isEnabled();
	}
	

	public void setToolTipText(String text) {
		openButton.setToolTipText(text);
	}

	public void setOrganizerID(int id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	
	public Object getValue() {
		return value;
	}

	
	public Class<SimpleCode> getType() {
		return SimpleCode.class;
	}

	
	public void setValue(Object value) {
		if(!Utils.equals(this.value,value)) {
			this.value = (SimpleCode)value;
			fireValueChanged();
			field.setText(value != null ? value.toString() : null);
			field.getCaret().setDot(0);
		}
	}

	
	public JComponent component() {
		return component;
	}
	
	
	/*********************** end organizers stuff *********************/


	private void setCircuitFromMapObjects(MGMapObject mapObjects[]) {

		final MapObjectQuery query = MapObjectQuery.fromMapObjects(mapObjects);

		new SwingWorker<List<SimpleCode>, Void>() {

			@Override
			protected List<SimpleCode> doInBackground() throws Exception {

				List<SimpleCode> circuits = new ArrayList<SimpleCode>();

				ServerLink link = new ServerLink(module.getHost(), "/scripts/inspections/lookup_circuits",query.buildQuery());
				ServerLinkInputStream in = link.open();
				try {
					while(in.next()) {
						circuits.add(new SimpleCode(in.read(), in.read()));
					}					
				}
				finally {
					in.close();
				}
				Collections.sort(circuits);
				return circuits;
			}
			
			public void done() {
				try {
					List<SimpleCode> codes = get();
					if(codes.isEmpty()) {
						GuiUtils.alert(component(),"No circuits found for map selection");
					}
					else if(codes.size() == 1) {
						setValue(codes.get(0));						
					}
					else {
						SimpleCode result = chooseCircuit(codes);
						if(result != null) {
							setValue(result);
						}
					}
				}
				catch(ExecutionException e) {
					GuiUtils.alert(component(),ExceptionUtils.unwrapToException(e));
				}
				catch(CancellationException ignored) {
				}
				catch(InterruptedException ignored) {
				}
			}
			
		}.execute();
	}


	private SimpleCode chooseCircuit(List<SimpleCode> codes) {
		JComboBox box = new JComboBox(codes.toArray());
		box.setSelectedIndex(0);
		int result = JOptionPane.showConfirmDialog(component(), 
				box, 
				"You must choose a single circuit", 
				JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION) {
			return (SimpleCode) box.getSelectedItem();
		}
		else {
			return null;
		}
	}
	
	private String toolTipTextForPickButton() {
		StringBuffer buffer = new StringBuffer("<html>");
		buffer.append("Choose a circuit from a map feature.");
		if(circuitedMapLayers != null && module.getMapControl() != null) {
			buffer.append("<br>You may choose from one of the following layers:");
			buffer.append("<ul>");
			for(MapLayerSet ml : circuitMapLayerSets()) {
				for(String layer: module.getMapControl().getMapLayers(ml, false)) {
					buffer.append("<li> "+layer);
				}
			}
			buffer.append("</ul>");
		}
		buffer.append("</html>");
		return buffer.toString();
	}
	
	private List<MapLayerSet> circuitMapLayerSets() {
		List<MapLayerSet> results = new ArrayList<MapLayerSet>();
		for(String s : Utils.splitWordsComma(circuitedMapLayers)) {
			results.add(MapLayerSet.parse(s));
		}
		return results;
	}
	
	/********************* getters and setters *******************************/
	
	public String getCircuitedMapLayers() {
		return circuitedMapLayers;
	}

	public void setCircuitedMapLayers(String circuitedMapLayers) {
		this.circuitedMapLayers = circuitedMapLayers;
		this.pickButton.setToolTipText(toolTipTextForPickButton());
	}


}
