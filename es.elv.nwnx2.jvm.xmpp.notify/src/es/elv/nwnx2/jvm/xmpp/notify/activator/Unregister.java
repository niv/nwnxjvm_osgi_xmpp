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

public class Unregister extends Feature implements XMPPCommandListener {

	@Autolocate
	protected XMPPService xse;

	@Autolocate
	protected PersistenceService p;

	@Override
	public String[] getCommandNames() {
		return new String[] {"unregister", "u"};
	}

	/*
	 * unregister -> unregisters all resources
	 * unregister ALL / * -> unregisters all resources
	 * unregister specific -> unregisters a specific resource
	 */

	@Override
	@Transactional
	public void handleCommand(Chat chat, String command, String arguments) throws XMPPException {
		final String part = chat.getParticipant().toLowerCase();
		final String woResource = part.split("/", 2)[0];

		String resourceToRemove = arguments.toLowerCase().trim();

		if ("".equals(resourceToRemove) || "*".equals(resourceToRemove) || "ALL".equals(resourceToRemove))
			resourceToRemove = null;

		User user = p.get().find(User.class).join("registrations").where().eq("jabberId", woResource).findUnique();
		if (user == null) {
			chat.sendMessage("You are not registered.");
			return;
		}

		for (Registration reg : user.registrations) {
			if (resourceToRemove == null && reg.resource == null) {
				p.get().delete(user.registrations);
				user.registrations.clear();
				chat.sendMessage("Removed you from receiving notifications.");
				return;
			}

			if (reg.resource != null && reg.resource.equalsIgnoreCase(resourceToRemove)) {
				user.registrations.remove(reg);
				p.get().delete(reg);
				chat.sendMessage("Removed resource " + resourceToRemove +
						" from receiving notifications.");
				return;
			}
		}

		chat.sendMessage("Resource to remove not found (try 'status').");
	}

	@Override
	public String getCommandHelp() {
		return "Unregister your XMPP ID from receiving enter/leave notifications. " +
			"Specific resources are not removed and must be removed manually.";
	}

}
