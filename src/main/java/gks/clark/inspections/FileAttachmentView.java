/*
 *
 * NOTICES
 * -------
 * 
 * Copyright 2012 by Gatekeeper Systems All Rights Reserved.
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

import gks.clark.mwm.MWMControlProxy;
import gks.clark.mwm.WorkOrderDetailView;
import gks.control.ControlTask;
import gks.form.Form;
import gks.form.anno.FactoryParam;
import gks.form.datasource.AbstractDataSource;
import gks.form.details.DetailsEditor;
import gks.form.editor.Editor;
import gks.form.util.AbstractView;
import gks.ui.GuiUtils;
import gks.ui.Layout;
import gks.util.DoubleClickGesture;
import gks.util.Environment;
import gks.util.Environment.FileTypeInfo;
import gks.util.lang.ExceptionUtils;
import gks.util.FileDownloader;
import gks.util.NavigateInterface;
import gks.util.ResourceLoader;
import gks.util.Utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Manages a set of FileAttachment objects. This is read-only but you can created
 * an view that supports adding files by implementing {@link #getFileDropTarget()}
 */
public class FileAttachmentView extends AbstractView implements ActionListener, FileDropTarget {
	
	public static final String REMOVE_MSG = "Are you sure you want to delete this file? This action cannot be undone.";
	
	private JList list;
	private JComponent component;
	private FileAttachment values[] = new FileAttachment[0];
	private Form form;
	private boolean editable = true;
	
	static class FileCellRenderer extends JPanel implements ListCellRenderer {
		JLabel icon;
		JLabel title;
		JLabel body;
		private JLabel footer;
		private final DateFormat DF = new SimpleDateFormat("M-dd-yyyy h:mm a");
		
		public FileCellRenderer() {
			setLayout(new BorderLayout(5,0));
			icon = createLabel(Font.PLAIN);
			title = createLabel(Font.BOLD);
			body = createLabel(Font.PLAIN);
			footer = createLabel(Font.PLAIN);
			
			JPanel center = new JPanel(new BorderLayout(0,3));
			center.setOpaque(false);
			center.add(title,BorderLayout.NORTH);
			center.add(body,BorderLayout.CENTER);
			center.add(footer,BorderLayout.SOUTH);
			add(center,BorderLayout.CENTER);
			add(icon,BorderLayout.WEST);
			setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		}

		protected JLabel createLabel(int style) {
			JLabel l = new JLabel();
			l.setFont(UIManager.getFont("Label.font").deriveFont(style));
			l.setOpaque(false);
			return l;
		}
		
		
		public void setForeground(Color color) {
			if(icon != null) {
			icon.setForeground(color);
			title.setForeground(color);
			body.setForeground(color);
			}
			else {
				super.setForeground(color);
			}
		}
		
		
		
		
		
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			Color bg = null;
			Color fg = null;
			
			if (isSelected) {
	            setBackground(bg == null ? list.getSelectionBackground() : bg);
	            setForeground(fg == null ? list.getSelectionForeground() : fg);
			}
			else {
			    setBackground(list.getBackground());
			    setForeground(list.getForeground());
			}
	
			footer.setForeground(isSelected ? Color.LIGHT_GRAY : Color.DARK_GRAY);
	
	
			setEnabled(list.isEnabled());
			setFont(list.getFont());
		
			FileAttachment file = (FileAttachment)value;
			FileTypeInfo info = Environment.getDefaultEnvironment().getFileTypeInfo(file.getFileType());
			icon.setIcon(info != null ? info.getSmallIcon() : null);
			title.setText(file.getName());
			body.setText(file.getComments());
			footer.setText(Utils.isNotEmpty(file.getUploadUser()) && file.getUploadDate() != null ? "Uploaded by "+file.getUploadUser()+" on "+DF.format(file.getUploadDate()) : null);
			return this;
		}
	}
	
	public void setBorder(Border border) {
		component.setBorder(border);
	}
	
	public void setVisible(boolean visible) {
		component.setVisible(visible);
	}
	
	public FileAttachmentView(Form form) {
		this.form = form;
		JPanel main = new JPanel(new BorderLayout(10,10));
		// we need this to be initialized first for createIcon
		component = main;
		
		list = new JList();
		list.setCellRenderer(new FileCellRenderer());
		list.setDragEnabled(true);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane center = new JScrollPane(list,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		list.addMouseListener(new DoubleClickGesture() {
			
			@Override
			public void onDoubleClick(MouseEvent e) {
				if(list.getSelectedIndex() != -1) {
					FileAttachment f = getSelectedFile();
					openAttachment(f);
				}
			}
		});
		
		FileDropTarget fileHandler = getFileDropTarget();
		if(fileHandler != null) {
			center.setTransferHandler(new FileDrop(fileHandler));
		}
		
		main.add(center,BorderLayout.CENTER);
		JComponent buttons = createButtons(fileHandler != null);
		main.add(buttons,BorderLayout.EAST);
		Dimension buttonSize = buttons.getPreferredSize();
		center.setPreferredSize(new Dimension(300,buttonSize.height));
	}
	
	// this must be overrided to support the addition of files
	protected FileDropTarget getFileDropTarget() {
		return this;
	}

	private JComponent createButtons(boolean create) {
		Layout layout = Layout.vstack(5);
		
		Layout buttonList = Layout.vgrid(5);
		if(create) {
			buttonList.add(Layout.hgrid(5).child(
					createIconButton("add",ResourceLoader.getIcon("gks/clark/mwm/images/addfiles.png"),"Add",REQ_EDITABLE)
			));
		}
	
		buttonList.child(
				createButton("view","View",REQ_SELECTED)
				);
		buttonList.child(
				createButton("save","Save",REQ_SELECTED)
				);

		layout.child(buttonList);
		
		layout.child(createButton("remove","Remove",REQ_EDITABLE | REQ_SELECTED | REQ_OWNER));
		return layout;
	}


	private JButton createButton(String cmd, String text, int flags) {
		final JButton b = GuiUtils.createButton(cmd, text, this);
		if(flags != 0) {
			ButtonListener l = new ButtonListener(b,flags);
			l.init();
		}
		return b;
	}
	
	
	class ButtonListener implements ListSelectionListener,PropertyChangeListener
	{
		int flags;
		JButton b;
		
		
		private ButtonListener(JButton b, int flags) {
			this.b = b;
			this.flags = flags;
		}

		public void init() {
			if((flags & REQ_SELECTED)!=0) {
				list.getSelectionModel().addListSelectionListener(this);
			}
			if((flags & REQ_EDITABLE)!=0) {
				component.addPropertyChangeListener("editable",this);
			}
			updateButton();
		}

		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			updateButton();
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent e) {
			updateButton();
		}

		private void updateButton() {
			boolean enabled = true;
			if((flags & REQ_SELECTED)!=0 && list.getSelectedIndex() == -1) {
				enabled = false;
			}
			if((flags & REQ_EDITABLE)!=0 && !editable) {
				enabled = false;
			}
			b.setEnabled(enabled);
		}
	}
	
	
	private static final int REQ_SELECTED = 1;
	private static final int REQ_EDITABLE = 2;
	private static final int REQ_OWNER = 4;
	
	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	private JButton createIconButton(String cmd, Icon icon, String title, int flags) {
		final JButton b = GuiUtils.createButton(cmd,icon,this);
		b.setText(title);
		b.setMargin(new Insets(2,2,2,2));
		if(flags != 0) {
			ButtonListener l = new ButtonListener(b,flags);
			l.init();
		}
		return b;
	}

	public void openAttachment(FileAttachment a) {
		NavigateInterface nav = form.getNavigate();
		nav.openFile(a.getPath(), a.getName(),a.getFileType());
	}
	
	protected void startControlTask(ControlTask task) {
		task.view(list);
		task.start();
	}

	protected Form getForm() {
		return form;
	}

	public void fileAttachmentUpdated(FileAttachment a) {
		int ix = Utils.indexOf(values, a);
		if(ix != -1) {
			((DefaultListModel)list.getModel()).setElementAt(a,ix);
		}
		fireValueChanged();
	}

	public void fileAttachmentCreated(FileAttachment a[]) {
		values = (FileAttachment[]) Utils.splice(values, values.length, 0, a);
		DefaultListModel model = new DefaultListModel();
		if(values != null) {
			for (FileAttachment f : values) {
				model.addElement(f);
			}
		}
		list.setModel(model);
		fireValueChanged();
	}

	
	public Object getValue() {
		return values;
	}


	public Class<?> getType() {
		return FileAttachment[].class;
	}

	public void setValue(Object value) {
		this.values = value != null ? (FileAttachment[])value : new FileAttachment[0];
		DefaultListModel model = new DefaultListModel();
		if(values != null) {
			for (FileAttachment f : values) {
				model.addElement(f);
			}
		}
		list.setModel(model);
	}

	public void setEditable(boolean editable) {
		
		this.editable = editable;
		component.firePropertyChange("editable", !this.editable, this.editable);
	}
	
	public JComponent component() {
		return component;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("add")) {
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(true);
			int returnVal = chooser.showOpenDialog(list);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File files[] = chooser.getSelectedFiles();
				getFileDropTarget().addFiles(files);
			}
		}
		else if(cmd.equals("view")) {
			openAttachment(getSelectedFile());
		}
		else if(cmd.equals("save")) {
			saveAttachment(getSelectedFile());
		}
		else if(cmd.equals("remove")) {
			if(GuiUtils.confirm(component, REMOVE_MSG)) {
				int ix = list.getSelectedIndex();
				if(ix != -1) {
					this.values = (FileAttachment[]) Utils.remove(this.values, ix, 1);
					((DefaultListModel)list.getModel()).remove(ix);
					fireValueChanged();
				}
			}
		}
		else 
			throw new UnsupportedOperationException(cmd);
	}

	private void saveAttachment(FileAttachment selectedFile) {
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(new File(selectedFile.getName()));
		int returnVal = chooser.showSaveDialog(list);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			new SwingWorker<Void, Void>() {

				@Override
				protected Void doInBackground() throws Exception {
					try(FileDownloader downloader = new FileDownloader(null, selectedFile.getPath())) {
						if(downloader.getHttpCode() == 200) {	
							downloader.saveFile(chooser.getSelectedFile());
						}
						else {
							throw new IOException("HTTP Server Error: "+ downloader.getHttpMessage());
						}
					}
					return null;
				}

				@Override
				protected void done() {
					try {
						get();
					}
					catch(ExecutionException e) {
						GuiUtils.alert(list, ExceptionUtils.unwrapToException(e));
					} 
					catch (InterruptedException ignored) {
					}
				}
				
				
			}.execute();

		}
	}

	protected FileAttachment getSelectedFile() {
		FileAttachment f = (FileAttachment)list.getSelectedValue();
		return f;
	}
	
	public void addFiles(File... files) {
		InspectionDetailsEditor parent = (InspectionDetailsEditor) form.getOwner();
		AsyncInspectionsControl controlProxy = parent.getControlProxy();
		startControlTask(controlProxy.attachFiles(files).onComplete(this, "fileAttachmentCreated"));

	}
}
