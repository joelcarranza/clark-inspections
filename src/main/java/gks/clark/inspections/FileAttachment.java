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
 *     URL:    http://www.gatekeeper.com
 *
 *
 */
package gks.clark.inspections;

import java.io.File;
import java.util.Date;


/**
 *
 */
public class FileAttachment implements gks.util.dto.ServerTransferObject  {
    private static final long serialVersionUID = 63504970748920L;

    /**************************
     *
     * Fields
     *
     **************************/
    String key;
    String name;
    String uploadUser;
    String path;
    Date uploadDate;
    String comments;


	public String getUploadUser() {
		return uploadUser;
	}

	public String getFileType() {
    	int ix = getPath().lastIndexOf('.');
    	return ix != -1 ? getPath().substring(ix+1) : "dat";
    }

    /**************************
     *
     * Constructor
     *
     **************************/

    /**
     * Construct a new Refusal Document instance
     */
    public FileAttachment() {
    }

    /**************************
     *
     * Getter and setters
     *
     **************************/


    /**
    * Access the value of the <code>key</code> property.
    */
    public String getKey() {
        return key;
    }


    /**
    * Access the value of the <code>path</code> property.
    */
    public String getPath() {
        return path;
    }

    /**
    * Access the value of the <code>name</code> property.
    */
    public String getName() {
        return name;
    }

    public Date getUploadDate() {
		return uploadDate;
	}


	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}


	/**************************
     *
     * Server Serializable object methods
     *
     **************************/
    public void parseFromStream(gks.util.dto.DataTransferInputStream stream)
        throws gks.util.dto.DataTransferException {
        this.key = stream.read();
        this.name = stream.read();
        this.path = stream.read();
        this.uploadDate = stream.readDate();
        this.uploadUser = stream.read();
        this.comments = stream.read();
    }

    public void serializeToStream(gks.util.dto.DataTransferOutputStream stream) throws gks.util.dto.DataTransferException {
        stream.write(this.key);
        stream.write(this.name);
        stream.write(this.path);
        stream.write(this.uploadDate);
        stream.write(this.uploadUser);
        stream.write(this.comments);
    }

    public void serializeModifiableToStream(
        gks.util.dto.DataTransferOutputStream stream) throws gks.util.dto.DataTransferException {
        stream.write(this.key);
    }
}
