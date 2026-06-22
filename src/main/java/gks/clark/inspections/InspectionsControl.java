/*
 * InspectionApplicationControl - Jun 14, 2005
 * 
 *

 * NOTICES
 * -------
 *
 * Copyright 2000 by Gatekeeper Systems All Rights Reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import gks.clark.inspections.model.CriteriaSiteFilter;
import gks.clark.inspections.model.GlobalidSiteFilter;
import gks.clark.inspections.model.Inspection;
import gks.clark.inspections.model.ProximitySiteFilter;
import gks.clark.inspections.model.Site;
import gks.clark.inspections.model.SiteList;
import gks.clark.inspections.model.WorkOrder;
import gks.control.BasicControl;
import gks.field.control.ControlException;
import gks.util.FileLink;
import gks.util.QueryBuilder;
import gks.util.ServerLink;
import gks.util.Utils;
import gks.util.dto.DataTransferException;
import gks.util.dto.ServerLinkInputStream;
import gks.util.dto.ServerLinkOutputStream;

/**
 * Interfaces to backend API scripts
 */
public class InspectionsControl extends BasicControl {
	private AsyncInspectionsControl proxy;

	public Site[] querySite(SiteFilter filter) throws DataTransferException {
		if(filter == null) {
			throw new IllegalArgumentException();
		}
		QueryBuilder q = new QueryBuilder();
		if(filter instanceof CriteriaSiteFilter) {
			CriteriaSiteFilter cf = (CriteriaSiteFilter)filter;
			q.append("MODE", "criteria");
			q.append("CIRCUIT", cf.getCircuit());
			if(cf.getTypes() != null && cf.getTypes().length > 0) {
				q.append("TYPE", cf.getTypes());				
			}
		}
		else if(filter instanceof ProximitySiteFilter) {
			ProximitySiteFilter pf = (ProximitySiteFilter)filter;
			q.append("MODE", "proximity");
			q.append("X", pf.getX());
			q.append("Y", pf.getY());
			q.append("DISTANCE", pf.getDistance());
		}
		else if(filter instanceof GlobalidSiteFilter) {
			GlobalidSiteFilter gf = (GlobalidSiteFilter)filter;
			q.append("MODE", "globalid");
			q.append("GLOBALID", gks.util.Utils.join(" ", getNormalizedIds(gf.getIdString())));
		}
		else if(filter instanceof TraceSiteFilter) {
			TraceSiteFilter tf = (TraceSiteFilter)filter;
			if(tf.getTraceID() == null) {
				TraceOperation op = new TraceOperation(tf.getTraceSession());
				op.execute();
				try {
					op.get();
					tf.setTraceID(tf.getTraceSession().getID());
				}
				catch(Exception e) {
					throw new DataTransferException("Trace failed", e);
				}
			}
			q.append("MODE", "trace");
			q.append("TRACE_ID", tf.getTraceID());
		}
		else {
			throw new UnsupportedOperationException(filter.getClass().getName());
		}
		return scriptQuery("/scripts/inspections/read_site", q, Site.class);
	}
	
	private static List<String> getNormalizedIds(String idString) {
        if (idString == null || idString.trim().isEmpty()) return Collections.emptyList();
        return Arrays.stream(idString.trim().split("\\s+"))
            .map(s -> s.replaceAll("[{}\\-]", "").toUpperCase())
            .filter(s -> s.length() == 32)
            .map(s -> "{" + s.substring(0,8) + "-" + s.substring(8,12) + "-"
                          + s.substring(12,16) + "-" + s.substring(16,20) + "-"
                          + s.substring(20) + "}")
            .collect(Collectors.toList());
    }

	public Inspection[] queryInspection(String listKey) throws DataTransferException {
		QueryBuilder q = new QueryBuilder();
		q.append("LIST", listKey);
		return scriptQuery("/scripts/inspections/read_inspection", q, Inspection.class);
	}
	
	public Inspection[] queryExceptions() throws DataTransferException {
		QueryBuilder q = new QueryBuilder();
		q.append("EXCEPTIONS", "true");
		return scriptQuery("/scripts/inspections/read_inspection", q, Inspection.class);
	}

	public WorkOrder[] queryWorkOrders() throws DataTransferException {
		QueryBuilder q = new QueryBuilder();
		return scriptQuery("/scripts/inspections/read_work_order", q, WorkOrder.class);
	}

	/**
	 * Create a list of inspections based on sites and various user parameters
	 */
	public String createList(SiteList siteList) throws DataTransferException {
		List<String> data = new ArrayList<String>();
		siteList.serializeModifiableToStream(new ServerLinkOutputStream(data));

		QueryBuilder q = new QueryBuilder();
		q.append("data", Utils.join(ServerLink.WORDSEP, data));

		ServerLink link = new ServerLink(getHostname(), "/scripts/inspections/create_inspections", q.urlQueryString());
		try {
			ServerLinkInputStream in = link.open();
			while (in.next()) {
				return in.read();
			}
		} finally {
			link.release();
		}

		throw new DataTransferException("No data returned");
	}

	public void save(Inspection insp) throws DataTransferException {
		List<String> data = new ArrayList<String>();
		insp.serializeModifiableToStream(new ServerLinkOutputStream(data));

		QueryBuilder q = new QueryBuilder();
		q.append("data", Utils.join(ServerLink.WORDSEP, data));

		ServerLink link = new ServerLink(getHostname(), "/scripts/inspections/update_inspection", q.urlQueryString());
		try {
			ServerLinkInputStream in = link.open();
			while (in.next()) {
			}
		} finally {
			link.release();
		}

	}

	public AsyncInspectionsControl proxy() {
		if (proxy == null) {
			proxy = createProxy(AsyncInspectionsControl.class);
		}
		return proxy;
	}

	
	 public static final String SCRIPT_UPLOAD_FILE =
		        "/scripts/dispatch/upload_file";

    /**
     * Create a new item in MWM_ORDER_FILE for an uploaded file and return an associated record
     * These files are not associated with the work order until the save occurs
     */
    public FileAttachment[] attachFiles(File[] files) throws ControlException {
        String query = "";
        List<FileAttachment> results = new ArrayList<FileAttachment>();
        for (File file : files) {
            if (!file.exists()) throw new RuntimeException();
            try {
                FileLink link = new FileLink(
                    getHostname(),
                    SCRIPT_UPLOAD_FILE,
                    query,
                    file,
                    file.getName()
                );
                try {
                    ServerLinkInputStream in = link.open();
                    if (in.next()) {
                        FileAttachment a = new FileAttachment();
                        a.parseFromStream(in);
                        results.add(a);
                    }
                } finally {
                    link.release();
                }
            } catch (DataTransferException e) {
                throw new ControlException(
                    "Unable to upload file " + file.getAbsolutePath(),
                    e
                );
            }
        }
        return results.toArray(new FileAttachment[0]);
    }
}
