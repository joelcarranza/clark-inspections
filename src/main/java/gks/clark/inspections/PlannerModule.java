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

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;

import gks.clark.inspections.model.SiteFilter;
import gks.control.BasicControl;
import gks.field.control.ControlException;
import gks.field.list.FieldListControl;
import gks.field.list.FieldListFilter;
import gks.field.list.ui.FieldListModule;
import gks.field.storage.CreateCachesTask;
import gks.field.storage.PersistentCacheManager;
import gks.form.chooser.Chooser;
import gks.ui.GuiUtils;
import gks.ui.SimpleDialog;
import gks.ui.SwingProxy;
import gks.util.NavigateInterface;
import gks.util.ResourceLoader;
import gks.util.lang.ExceptionUtils;


public class PlannerModule extends FieldListModule implements PropertyChangeListener {
	
	public static final String WIN_FILTER = "winFilter";
	private static final String WIN_NEW_LIST_DIALOG = "winList";

	private InspectionsControl control;
	private AsyncInspectionsControl asyncControl;

	public PlannerModule(NavigateInterface application) {
		super(application);
	}
	
	@Override
	protected FieldListControl createControl() throws ControlException {
		Properties controlProperties = ResourceLoader.getProperties("gks/clark/inspections/control.properties");		
		control = new InspectionsControl();
		control.setView(this);
		control.configure(controlProperties);
		return control;
	}		
	
	@Override
	public void start() throws Exception {
    	super.start();
		configuration().inject(this);

		control = (InspectionsControl) fieldListControl();

		asyncControl = control.proxy();
		
		control.addPropertyChangeListener(SwingProxy.createPropertyChangeListener(this));

    	buildView();
    	
    	setViewBusy(true);
		setStatusMessage("Initializing...");
        
        new CreateCachesTask(this) {
        	

			@Override
        	public void persistentCacheManagerCreated() {
        		setViewBusy(false);
				initialize(getCacheManager(), getCacheRoot());
        	}
        	
        	@Override
        	public void persistentCacheManagerCreationFailed(Exception e) {
        		setViewBusy(false);
        		setStatusMessage("Startup failed");
				die(e);
        	}
        }.execute();
    }
	

	protected void initialize(final PersistentCacheManager cacheManager, final File cacheRoot) {
		setViewBusy(true);
		new javax.swing.SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				control.initialize(cacheManager, cacheRoot);
				return null;
			}
			
			public void done() {
				setViewBusy(false);
				
				try {
					get();
					setStatusMessage("");
				} catch (InterruptedException ignored) {
				} catch (ExecutionException e) {
					die(ExceptionUtils.unwrapToException(e));
				}
			}
		}.execute();		
	}
	 
	
	public void actionShowFilter(ActionEvent e) {
		windowManager().show(WIN_FILTER);
	}
	
	public Window actionNewList() {
		return windowManager().show(WIN_NEW_LIST_DIALOG);
	}

	public void actionOpen(ActionEvent e) {
		
	}

	
	public Window createWindow(String name) {
		if(name.equals(WIN_FILTER)) {

			Chooser<FieldListFilter> chooser = new Chooser<FieldListFilter>(this)
			{
				@Override
				public void ok(FieldListFilter filter) {					
					asyncControl.setFilter(filter);
				}

				@Override
				public SimpleDialog buildAsDialog(JComponent parent, int modality) {
					SimpleDialog dlg = super.buildAsDialog(parent, modality);
					dlg.addAction("reset", "Reset", null, SimpleDialog.SW_CORNER);
					return dlg;
				}

				@Override
				protected void applyActionPerformed(SimpleDialog dlg) {
					super.applyActionPerformed(dlg);
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if(cmd.equals("reset")) {
						setChoiceValues(new SiteFilter());
					}
					else {
						super.actionPerformed(e);
					}
				}
				
				
			};
			GuiUtils.setImplementation(chooser,PlannerModule.class,this);
			chooser.addChoice(new SiteFilter(),
					"gks/clark/inspections/SiteFilter.xml","Sites by Circuit");

//			chooser.setPreferredSizeFromChoiceIndex(1);
			SimpleDialog dlg = chooser.buildAsDialog(this);
			dlg.setTitle("Filter");
			dlg.setDefaultCloseOperation(SimpleDialog.HIDE_ON_CLOSE);
			return dlg;
		}
		else if(name.equals(WIN_NEW_LIST_DIALOG ))
		{
	    	Frame f = GuiUtils.findAncestor(Frame.class, this);
            return new WorkOrderListDialog(f, this);
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
			setViewBusy(evt.getSource(),((Boolean) value).booleanValue());
		}
		else if (name == BasicControl.PROPERTY_STATUS_MESSAGE) {
			setStatusMessage((String) value);
		}
		else if (name == BasicControl.PROPERTY_PROGRESS) {
 			setProgress(((Integer) value).intValue());
		}
	}
	public InspectionsControl getControl() {
		return control;
	}

	
}
