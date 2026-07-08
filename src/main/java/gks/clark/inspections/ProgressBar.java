/**
 * NOTICES
 * -------
 *
 * Copyright 2026 by Gatekeeper Systems All Rights Reserved.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * 
 */
public class ProgressBar implements Icon {
	int pct;
	
	public ProgressBar(int pct) {
		this.pct = pct;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		Color bg = c.getBackground();
		g2d.setColor(new Color(bg.getRed(),bg.getGreen(),bg.getBlue(),128));
    	g2d.fillRect(0, 0, 100,10);
    	
    	// stroke rectangle with foreground
	 	g2d.setColor(Color.DARK_GRAY);
    	g2d.drawRect(0, 0, 100,10);
    	
    	if(pct > 0) {
        	// fill progress bar
        	g2d.setColor(Color.BLUE);
        	g2d.fillRect(1, 1, pct, 9);
    	}
    	else {
    		// TODO: barber stripe?
    	}

	}

	@Override
	public int getIconWidth() {
		return 100;
	}

	@Override
	public int getIconHeight() {
		return 10;
	}

}
