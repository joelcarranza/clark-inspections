package gks.clark.inspections;

import gks.trace.TraceSession;

public class TraceSiteFilter implements SiteFilter {

	private TraceSession session;
	private Long traceID;

	public TraceSession getTraceSession() {
		return session;
	}

	public void setTraceSession(TraceSession v) {
		session = v;
	}

	public Long getTraceID() {
		return traceID;
	}

	public void setTraceID(Long v) {
		traceID = v;
	}

	public boolean isTraceFilter() {
		return session != null;
	}
}
