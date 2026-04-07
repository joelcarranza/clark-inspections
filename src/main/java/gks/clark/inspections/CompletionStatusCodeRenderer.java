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

import java.awt.Color;

import javax.swing.Icon;

import gks.ui.CheckBoxIcon;
import gks.ui.ValueRenderer;

public class CompletionStatusCodeRenderer implements ValueRenderer {
	Icon complete;
	Icon incomplete;

	public CompletionStatusCodeRenderer() {
		complete = new CheckBoxIcon(true);
		incomplete = new CheckBoxIcon(false);

	}

	public Color getBackground(Object value) {
		return null;
	}

	public String getDisplayName(Object value) {
		return null;
	}

	public Color getForeground(Object value) {
		return null;
	}

	public Icon getIcon(Object value) {
		if (((Boolean) value).booleanValue()) {
			return complete;
		} else {
			return incomplete;
		}
	}

	public String getTooltipText(Object value) {
		return null;
	}

}
