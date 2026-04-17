package gks.clark.inspections;

import javax.swing.SwingWorker;

import gks.trace.TraceResults;
import gks.trace.TraceSession;
import gks.util.Debug;
import gks.util.QueryBuilder;
import gks.util.ServerLink;
import gks.util.ServerLinkException;
import gks.util.dto.DataTransferException;
import gks.util.dto.ServerLinkInputStream;

public class TraceOperation extends SwingWorker<TraceResults, Void> {

	public static final String SCRIPT_START_TRACE = "/scripts/trace/start_trace";
	public static final String SCRIPT_POLL_TRACE = "/scripts/trace/poll_trace";

	final TraceSession session;

	public TraceOperation(TraceSession session) {
		this.session = session;
	}

	private void initSession(TraceSession session) throws ServerLinkException {
		session.setResults(null);
		ServerLink link = new ServerLink(null, SCRIPT_START_TRACE, session.startQuery());
		try {
			session.setID(Long.parseLong(link.readLine()));
			Debug.trcln(1, "session.id = {0}", new Long(session.getID()));
		} finally {
			link.release();
		}
	}

	@Override
	protected TraceResults doInBackground() throws Exception {
		initSession(session);
		Debug.trcln(0, "Began trace. Session = {0}", session);
		TraceResults results = poll(session);
		session.setResults(results);
		return results;
	}

	private TraceResults poll(TraceSession session) throws DataTransferException, InterruptedException {
		TraceResults results = null;
		while (results == null) {
			results = pollTrace(session);
		}
		return results;
	}

	public TraceResults pollTrace(TraceSession session) throws DataTransferException {
		if (session.getResults() != null) {
			return session.getResults();
		}
		QueryBuilder q = new QueryBuilder();
		q.append("ID", session.getID());
		ServerLink link = new ServerLink(null, SCRIPT_POLL_TRACE, q.toString());
		try {
			ServerLinkInputStream in = link.open();
			while (in.next()) {
				TraceResults results = new TraceResults();
				results.parseFromStream(in);
				return results;
			}
			if (link.statusCode() == 201) {
				return null;
			} else {
				throw new DataTransferException("Unexpected EOF");
			}
		} finally {
			link.release();
		}
	}
}
