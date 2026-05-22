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

import gks.util.Debug;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import javax.swing.filechooser.FileFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Transfer handler which accepts java.io.File objects and passes
 * them on to {@link FileDropTarget}
 */
public class FileDrop extends TransferHandler {

	FileFilter filter = null;
	private FileDropTarget target;
	
	public FileDrop(FileDropTarget target) {
		this.target = target;
	}
	
	public FileDrop(FileFilter filter) {
		this.filter = filter;
	}
	
	
    public boolean importData(JComponent comp, Transferable t) {
      // Make sure we have the right starting points
      if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        return false;
      }

      // Grab the tree, its model and the root node
      try {
        List<File> fileList = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
        if(filter != null) {
        	List<File> filtered = new ArrayList<File>();
        	for (File f : fileList) {
				if(filter.accept(f))
					filtered.add(f);
			}
        	if(filtered.isEmpty())
        		return false;
        	fileList = filtered;
        }
        target.addFiles(fileList.toArray(new File[0]));
        return true;
      }
      catch (UnsupportedFlavorException ufe) {
    	  Debug.dumpException(ufe, "importData()",FileDrop.class.getName());
      }
      catch (IOException ioe) {
        Debug.dumpException(ioe, "importData()",FileDrop.class.getName());
      }
      return false;
    }

	public int getSourceActions(JComponent comp) {
        return COPY | MOVE;
    }
	
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        for (int i = 0; i < transferFlavors.length; i++) {
          if (!transferFlavors[i].equals(DataFlavor.javaFileListFlavor)) {
            return false;
          }
        }
        return true;
    }
}