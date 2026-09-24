package gks.clark.inspections;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import gks.form.Form;
import gks.form.FormComponent;
import gks.map.MapControl;
import gks.map.proxy.MGMapObject;
import gks.map.proxy.MGPoint;
import gks.ui.GuiUtils;

/**
 * Set form values lat/lon based on the current map selection
 */
public class SelectionPointView implements FormComponent, ActionListener {

	private final JButton button;
	private PlannerModule owner;
	private Form form;

	public SelectionPointView(Form form) {
		this.form = form;
		owner = (PlannerModule) form.getOwner();
		button = new JButton("From Selection");
		button.addActionListener(this);
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
		MapControl mapControl = owner.getMapControl();
		MGMapObject sel[] = mapControl != null ? mapControl.getSelectedMapObjects() : new MGMapObject[0];
		if(sel.length == 0) {
			GuiUtils.alert(button, "You must select a feature on the map");
			return;
		}
		if(sel.length > 1) {
			GuiUtils.alert(button, "You must select only one feature on the map");
			return;
		}
		MGPoint ll = sel[0].getCenter(false);
		form.getModel().setValue("lon", ll.getX());
		form.getModel().setValue("lat", ll.getY());
	}

	@Override
	public void addComponent(JComponent component) {
		throw new UnsupportedOperationException();
	}
}
