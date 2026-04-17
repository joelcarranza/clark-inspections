package gks.clark.inspections;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import gks.form.Form;
import gks.form.util.AbstractView;
import gks.trace.TraceFeature;

public class StartFeatureView extends AbstractView implements ActionListener {

	private final JButton button;
	private TraceFeature value;
	private PlannerModule owner;

	public StartFeatureView(Form form) {
		owner = (PlannerModule) form.getOwner();
		button = new JButton();
		button.addActionListener(this);
		updateView();
	}

	private void updateView() {
		button.setText(value != null ? value.getDisplayName() : "Choose...");
		button.setToolTipText(value != null ? value.getHTMLDescription() : null);
	}

	@Override
	public JComponent component() {
		return button;
	}

	@Override
	public Class<?> getType() {
		return TraceFeature.class;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		if (this.value != value) {
			this.value = (TraceFeature) value;
			updateView();
			fireValueChanged();
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
		owner.chooseTraceMapFeature(StartFeatureView.this);
	}
}
