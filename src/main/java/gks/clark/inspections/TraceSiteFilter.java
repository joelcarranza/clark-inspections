package gks.clark.inspections;

import gks.clark.trace.ElectricTraceSession;

public class TraceSiteFilter implements SiteFilter {

	private ElectricTraceSession session;
	private Long traceID;

	public ElectricTraceSession getTraceSession() {
		return session;
	}

	public void setTraceSession(ElectricTraceSession v) {
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
