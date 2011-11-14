package es.elv.nwnx2.jvm.xmpp.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jivesoftware.smack.XMPPException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.XMPPService;

public class LogListenerImpl extends Feature implements LogListener, ManagedService {
	private boolean printStackTraces = true;

	private Integer minLevel = LogService.LOG_WARNING;
	private Map<String, Integer> minLevels = new HashMap<String, Integer>();

	private List<String> logTargets = new LinkedList<String>();
	
	private XMPPService xmpp;

	void addLRS(LogReaderService lrs) {
		lrs.addLogListener(this);
	}

	void removeLRS(LogReaderService lrs) {
		lrs.removeLogListener(this);
	}

	@Override
	protected void load() throws Exception {
		xmpp = locateServiceFor(XMPPService.class);
	}
	
	private int getMinLevelFor(String bundleName) {
		if (minLevels.containsKey(bundleName))
			return minLevels.get(bundleName);
		else
			return minLevel;
	}

	@Override
	public void logged(LogEntry log) {
		if (log.getMessage() == null)
			return;

		if (log.getLevel() > getMinLevelFor(log.getBundle().getSymbolicName()))
			return;

		// Format: bundlesymbolicname [BundleID]: SEVERITY message\nException

		StringBuilder b = new StringBuilder();
		b.append(log.getBundle().getSymbolicName());
		b.append("[" + log.getBundle().getBundleId() + "]");
		b.append(": ");
		switch (log.getLevel()) {
			case LogService.LOG_DEBUG:   b.append("DEBUG"); break;
			case LogService.LOG_INFO:    b.append("INFO"); break;
			case LogService.LOG_WARNING: b.append("WARNING"); break;
			case LogService.LOG_ERROR:   b.append("ERROR"); break;
		}

		b.append(" ");
		b.append(log.getMessage());
		if (!printStackTraces && log.getException() != null)
			b.append(" (Exception: " + log.getException().getMessage() + ")");

		if (printStackTraces  && log.getException() != null) {
			StringWriter sw = new StringWriter();
			PrintWriter w = new PrintWriter(sw);
			log.getException().printStackTrace(w);
			b.append("\n");
			b.append(sw.getBuffer());
			b.append("\n");
		}

		final String ingameMessage = b.toString();

		for (String jid : logTargets)
			try {
				xmpp.sendMessage(jid, ingameMessage);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
	}
	
	private int resolveLevel(String level) throws ConfigurationException {
		if (level.equals("DEBUG"))
			return LogService.LOG_DEBUG;
		else if (level.equals("INFO"))
			return LogService.LOG_INFO;
		else if (level.equals("WARNING"))
			return LogService.LOG_WARNING;
		else if (level.equals("ERROR"))
			return LogService.LOG_ERROR;
		else
			throw new ConfigurationException("min_level",
				"Invalid value '" + level + "', requires one of: DEBUG INFO WARNING ERROR");
	}
	
	@Override
	public void updated(@SuppressWarnings("rawtypes") Dictionary c) throws ConfigurationException {
		if (c == null)
			return;

		String levelToSet = (String) c.get("min_level");
		minLevel = resolveLevel(levelToSet);

		String bundleMinLevels = (String) c.get("bundle_min_levels");
		if (bundleMinLevels != null && bundleMinLevels.trim().length() > 0) {
			bundleMinLevels = bundleMinLevels.trim();
			StringTokenizer tk = new StringTokenizer(bundleMinLevels, ",");
			while (tk.hasMoreTokens()) {
				String tok = tk.nextToken().trim();
				String[] splt = tok.split("=");
				if (splt.length != 2)
					throw new ConfigurationException("bundle_min_levels", "invalid bundle descriptor/token: " + tok);
				String symname = splt[0].trim();
				int lv = resolveLevel(splt[1].trim());
				minLevels.put(symname, lv);
			}
		}
		
		String targets = (String) c.get("log_targets");
		if (targets != null) {
			String[] targets_a = targets.split(", *");
			logTargets = Arrays.asList(targets_a);
		} else
			logTargets.clear();

		printStackTraces = (Boolean) c.get("print_stack_traces");
	}

}
