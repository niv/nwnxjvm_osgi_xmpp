package es.elv.nwnx2.jvm.xmpp.notify.activator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;

import com.avaje.ebean.annotation.Transactional;

import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.XMPPService;
import es.elv.nwnx2.jvm.xmpp.commands.XMPPCommandListener;
import es.elv.nwnx2.jvm.xmpp.notify.Registration;
import es.elv.nwnx2.jvm.xmpp.notify.User;
import es.elv.osgi.base.Autolocate;
import es.elv.osgi.persistence.ebean.PersistenceService;

public class Register extends Feature implements XMPPCommandListener {

	@Autolocate
	protected XMPPService xse;

	@Autolocate
	protected PersistenceService p;


	@Override
	public String[] getCommandNames() {
		return new String[] {"register", "r"};
	}


	/*
	 * register -> alias for register * / ALL
	 * register * ->  registers all resources
	 * register specific -> registers a specific resource
	 * register [specific] online register only when resource is online
	 */

	@Override
	@Transactional
	public void handleCommand(Chat chat, String command, String arguments) throws XMPPException {
		final String part = chat.getParticipant().toLowerCase();
		final String woResource = part.split("/", 2)[0];
		final String[] va = arguments.split(" +");

		String resourceToAdd = va[0].toLowerCase().trim();
		final boolean onlineOnly = "online".equalsIgnoreCase(va[0]) ||
			(va.length > 1 && "online".equalsIgnoreCase(va[1]));

		if ("".equals(resourceToAdd) || "*".equals(resourceToAdd) || "online".equalsIgnoreCase(resourceToAdd))
			resourceToAdd = null;

		User user = p.get().find(User.class).join("registrations").where().eq("jabberId", woResource).findUnique();
		if (user == null) {
			user = new User();
			user.jabberId = woResource;
		}

		for (Registration reg : user.registrations) {
			if (resourceToAdd != null && reg.resource == null) {
				chat.sendMessage("You are already registered to receive notifications on all resources." +
						"Unregister first and then register specific resources.");
				return;
			}

			if (resourceToAdd == null || reg.resource == null ||
					reg.resource.equals(resourceToAdd)) {
				chat.sendMessage("Already registered.");
				return;
			}
		}

		Registration newReg = new Registration();
		newReg.user = user;
		newReg.onlineOnly = onlineOnly;
		newReg.resource = resourceToAdd;
		user.registrations.add(newReg);
		p.get().save(user);

		if (resourceToAdd == null)
			chat.sendMessage("Added you to receive notifications.");
		else
			chat.sendMessage("Added resource '" + resourceToAdd + "' to receiving notifications.");

		log.info(part + " registers resource " + (resourceToAdd == null ? "*" : ""));

		RosterEntry entry = xse.getConnection().getRoster().getEntry(woResource);
		if (null == entry || (!entry.getType().equals(ItemType.both) &&
				!entry.getType().equals(ItemType.to))) {

			if (newReg.onlineOnly)
				chat.sendMessage("You will not receive notifications when I can't see you. " +
					"Accept the visibility request to see notifications.");

			xse.getConnection().getRoster().createEntry(woResource, woResource, null);
		}
	}


	@Override
	public String getCommandHelp() {
		return "Register your XMPP ID for receiving enter/leave notifications. " +
			"Supply optional resource to only receive notifies on that resource only. " +
			"Append 'online' (after an optional resource name) to only receive messages when online " +
			"(This does NOT work for ICQ/Yahoo/MSN/AIM/GTalk, only native XMPP connections).";
	}

}
