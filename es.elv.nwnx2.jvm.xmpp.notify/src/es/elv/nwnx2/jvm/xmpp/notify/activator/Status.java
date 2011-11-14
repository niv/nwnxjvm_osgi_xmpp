package es.elv.nwnx2.jvm.xmpp.notify.activator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

import com.avaje.ebean.annotation.Transactional;

import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.XMPPService;
import es.elv.nwnx2.jvm.xmpp.commands.XMPPCommandListener;
import es.elv.nwnx2.jvm.xmpp.notify.Registration;
import es.elv.nwnx2.jvm.xmpp.notify.User;
import es.elv.osgi.base.Autolocate;
import es.elv.osgi.persistence.ebean.PersistenceService;

public class Status extends Feature implements XMPPCommandListener {
	@Autolocate
	protected XMPPService xse;

	@Autolocate
	protected PersistenceService p;

	@Override
	public String[] getCommandNames() {
		return new String[] {"status"};
	}

	@Override
	@Transactional
	public void handleCommand(Chat chat, String command, String arguments) throws XMPPException {
		final String part = chat.getParticipant().toLowerCase();
		final String woResource = part.split("/", 2)[0];

		User findUnique = p.get().find(User.class).join("registrations").where().eq("jabberId", woResource).findUnique();

		if (findUnique != null && findUnique.registrations.size() > 0) {
			String res = "";
			for (Registration r : findUnique.registrations) {
				if (r.resource == null) {
					chat.sendMessage("You are registered to receive notifications.");
					return;
				}
				res += r.resource == null ? "*" : r.resource + (r.onlineOnly ? " (only when online)" : "" ) + " ";
			}
			chat.sendMessage("You are registered with the following resources: " + res);
		} else {
			chat.sendMessage("You are NOT registered to receive notifications.");
		}
	}

	@Override
	public String getCommandHelp() {
		return "Shows registration info.";
	}
}
