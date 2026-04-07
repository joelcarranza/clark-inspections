/*
 *
 * NOTICES
 * -------
 *
 * Copyright 2002 by Gatekeeper Systems All Rights Reserved.
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
 *     1010 E. Union St.
 *     Pasadena, CA 91106
 *
 *     Tel: (626) 449-3070 or (800) 424-3070
 *     Fax: (626) 440-1742
 *
 *     E-Mail: info@gatekeeper.com
 *     URL:    http://www.gatekeeper.com/
 *
 */

package gks.clark.inspections;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;

import gks.clark.inspections.model.SiteList;
import gks.field.storage.StorageManagerLinkException;
import gks.ui.GlassPane;
import gks.ui.GridTool;
import gks.ui.GuiUtils;
import gks.ui.OkCancelDialog;
import gks.ui.UIStyle;
import gks.util.DateComboBox;
import gks.util.Debug;

/**
 * Data entry form for creating a new WorkOrderList.
 */
public class WorkOrderListDialog extends OkCancelDialog implements ActionListener {

    Frame owningFrame;
    PlannerModule parent;
    InspectionsControl control;

    SiteList list;

    JComboBox crewCBO;
    DateComboBox dateCBO;
    JTextArea filterTXT;

    JButton okButton;
    JButton cancelButton;

    GlassPane glassPane;

    WorkOrderListDialog(Frame frame, PlannerModule parent) {
    	super(frame,true);
		// hook up to parent & control
		this.owningFrame = frame;
		this.parent = parent;
		this.control = this.parent.getControl();
	
		Debug.trcln(1, ">>WorkOrderListDialog()");
		list = new SiteList();
		list.setFilter(this.parent.getControl().getFilter());
		okButtonPolicy = DISABLE_OK_NEVER;
		build();
		getRootPane().setGlassPane(glassPane = new GlassPane());
		setResizable(false);
		setTitle("New Work Order Assignment List");
	}
    
    @Override
	public JComponent createMainPanel() {
		JPanel p = new JPanel();
		GridTool tool = new GridTool(p);
		tool.addField("Schedule Date", dateCBO = new DateComboBox());
		tool.addField("Crew", crewCBO = new JComboBox());
	        crewCBO.setLightWeightPopupEnabled(false);
	    tool.addRowFiller(Box.createVerticalStrut(UIStyle.getMajorGap()),2);
		tool.addRowFiller(new JLabel("<html><b>Filter</b></html>"),2);
		tool.addFiller(filterTXT =
			new JTextArea(list.getFilter().displayString(),4,30),2);
		colorDisabledText(filterTXT);
		filterTXT.setEditable(false);
		return p;
    }


    private void colorDisabledText(final JComponent c)
    {
    	java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
    	boolean themeActive = Boolean.TRUE.equals(toolkit.getDesktopProperty("win.xpstyle.themeActive"));
    	if(!themeActive)
    	{
        	final Color color = UIManager.getColor("Panel.background");
        	final Color disabledColor = color.brighter();
        	final Color defaultColor = c.getBackground();
        	final Border defaultBorder = c.getBorder();
    		c.addPropertyChangeListener("editable",new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent evt) {
					Boolean enabled = (Boolean) evt.getNewValue();
					c.setBackground(enabled.booleanValue() ? defaultColor : disabledColor);
					c.setBorder(enabled.booleanValue() ? defaultBorder : BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(disabledColor.darker(),1),
							BorderFactory.createEmptyBorder(2,2,2,2)));
				}
			});
    	}
    }

    /**
     * Implements ActionListener interface.
     */
    public void actionPerformed(ActionEvent e) {
    	try
		{
			if (e.getActionCommand().equals("OK")) {
			    okAction();
			} else if (e.getActionCommand().equals("Cancel")) {
			    cancelAction();
			} else {
			    throw new RuntimeException(
				"Unknown Action Command: " + e.getActionCommand());
			}
		}
    	catch(Exception ex)
		{
    		alert(ex);
		}
    }

    /**
     * Closes without prompting to save any changes.
     */
    @Override
	public void close() {
		dispose();
    }


    @Override
	public void okAction() {
	 	if (!parent.okToChangeList(this))
			return;

	//	list.setCrew((WorkOrderCrew)crewCBO.getSelectedItem());
		list.setScheduleDate((Date)dateCBO.getSelectedItem());

		if (!list.isValid()) {
		    alert("Crew, Schedule Date, and a valid filter are required");
		    return;
		}
		Debug.trcln(1, "WorkOrder List is now: {0}");
		parent.getControl().proxy().saveList(list).onComplete(this,"close").onFail(this, "handleError").start();
    }
    
    public void handleError(Exception e) {
    	if(e instanceof StorageManagerLinkException) {
    		StorageManagerLinkException sle = (StorageManagerLinkException)e;
    		if(sle.getWrappedException().getCode() == 411) {
    			String msg = "Failed to save Work Order List. A list with the "
    					+ "selected crew and schedule date has already been created";
				GuiUtils.alert(this,msg);
    			return;
    		}
    	}
    	
    	GuiUtils.alert(this,e);
    }

    private void alert(Exception e)
    {
    	parent.alert(e);
    }
}
