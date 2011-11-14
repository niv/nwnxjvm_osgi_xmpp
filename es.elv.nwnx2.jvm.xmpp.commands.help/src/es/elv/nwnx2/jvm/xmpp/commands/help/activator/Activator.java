package es.elv.nwnx2.jvm.xmpp.commands.help.activator;

import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.osgi.framework.ServiceReference;

import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.commands.XMPPCommandListener;

public class Activator extends Feature implements XMPPCommandListener {

	@Override
	public String[] getCommandNames() {
		return new String[] { "help", "?" };
	}

	private Set<ServiceReference<?>> commands = new HashSet<ServiceReference<?>>();

	void addCommand(ServiceReference<?> sr) {
		commands.add(sr);
	}
	void removeCommand(ServiceReference<?> sr) {
		commands.remove(sr);
	}

	@Override
	public void handleCommand(Chat chat, String command, String arguments) throws XMPPException {
		StringBuilder ret = new StringBuilder();

		ret.append("Help? Help!\n");
		ret.append("Available commands:\n\n");

		for (ServiceReference<?> r : commands) {

			StringBuilder cret = new StringBuilder();

			XMPPCommandListener l = (XMPPCommandListener) bundleContext.getService(r);

			String help = l.getCommandHelp();
			if (null == help)
				continue;

			String[] cmd = l.getCommandNames();
			if (cmd == null || cmd.length < 1)
				continue;

			cret.append(cmd[0]);
			if (cmd.length > 1) {
				String cmd_aliases = cmd[1];
				for (int i = 2; i < cmd.length; i++)
					cmd_aliases += ", " + cmd[i];
				cret.append(" (Aliases: " + cmd_aliases + ")");
			}
			cret.append("\n");
			cret.append("    Provided by: " + r.getBundle().getSymbolicName() + " " + r.getBundle().getVersion() + "\n");
			cret.append("  " + help + "\n");
			cret.append("\n");
			ret.append(cret.toString());
		}

		chat.sendMessage(ret.toString());
	}

	@Override
	public String getCommandHelp() {
		return null;
	}
}
