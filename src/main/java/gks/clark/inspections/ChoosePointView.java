package gks.clark.inspections;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JComponent;

import gks.form.Form;
import gks.form.FormComponent;
import gks.form.util.AbstractView;
import gks.map.MapControl;
import gks.map.event.MapDigitizeEvent;
import gks.map.event.MapDigitizeListener;
import gks.map.event.MapSelectionEvent;
import gks.map.event.MapSelectionListener;
import gks.map.proxy.MGMap;
import gks.map.proxy.MGMapLayer;
import gks.trace.TraceFeature;
import gks.util.Location;

/**
 * Set form values lat/lon based on user choosing point on map 
 */
public class ChoosePointView implements FormComponent, ActionListener, MapDigitizeListener, PropertyChangeListener {

	private final JButton button;
	private PlannerModule owner;
	private Form form;

	public ChoosePointView(Form form) {
		this.form = form;
		form.addPropertyChangeListener(Form.PROPERTY_ACTIVE, this);
		owner = (PlannerModule) form.getOwner();
		button = new JButton("From Map Point...");
		button.addActionListener(this);
		updateView();
	}

	private void updateView() {
	}

	@Override
	public JComponent component() {
		return button;
	}

	public void bind(Form form, String id) {
		if(id != null) {
			form.defineComponent(id,this);
		}
	}


	public void setEnabled(boolean enabled) {
		button.setEnabled(enabled);
	}

	public boolean isEnabled() {
		return button.isEnabled();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		owner.getMapControl().digitize(MapDigitizeEvent.POINT, this);
		owner.mapCommand().focus().run();	
	}

	@Override
	public void addComponent(JComponent component) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mapShapeDigitized(MapDigitizeEvent event) {
		if(event.getType() == MapDigitizeEvent.POINT) {
			Location ll = event.getLocations()[0];
			form.getModel().setValue("lon", ll.getX());
			form.getModel().setValue("lat", ll.getY());
		}
		owner.getMapControl().removeMapDigitizeListener(this);
		component().requestFocus();
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName() == Form.PROPERTY_ACTIVE) {
			if(Boolean.FALSE.equals(evt.getNewValue())) {
				MapControl mapControl = owner.getMapControl();
				if(mapControl != null) {
					mapControl.cancelDigitize();
				}
			}
		}
	}
}
