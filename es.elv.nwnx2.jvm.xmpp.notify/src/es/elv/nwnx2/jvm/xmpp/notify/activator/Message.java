package es.elv.nwnx2.jvm.xmpp.notify.activator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.nwnx.nwnx2.jvm.Color;
import org.nwnx.nwnx2.jvm.Scheduler;

import es.elv.nwnx2.jvm.hierarchy.PlayerCreature;
import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.XMPPService;
import es.elv.nwnx2.jvm.xmpp.commands.XMPPCommandListener;
import es.elv.osgi.base.Autolocate;
import es.elv.osgi.persistence.ebean.PersistenceService;

public class Message extends Feature implements XMPPCommandListener {

	@Autolocate
	protected XMPPService xse;

	@Autolocate
	protected PersistenceService p;

	@Override
	public String[] getCommandNames() {
		return new String[] { "msg", "@" };
	}

	@Override
	public void handleCommand(final Chat chat, String command, String arguments)
			throws XMPPException {

		final String[] va = arguments.split(" +", 2);

		if (va.length != 2 || va[0].length() < 1 || va[1].length() < 1) {
			chat.sendMessage("Syntax: <account> <message to send>");
			return;
		}

		/*User u = p.get().find(User.class).where().
			eq("ingameAccount", va[0].toLowerCase()).findUnique();
		if (u == null) {
			chat.sendMessage("That account has not registered.");
			return;
		}*/

		Scheduler.schedule(new Runnable() {
			@Override public void run() {
			    try {
			    	for (PlayerCreature p : PlayerCreature.all())
				    	if (p.getAccount().equalsIgnoreCase(va[0])) {
				    		p.message(Color.ALICEBLUE.color(chat.getParticipant()) +
				    				Color.AZURE.color(" (via notify bot) says: ") + Color.ANTIQUEWHITE1.color(va[1]));
				    		chat.sendMessage("Message delivered.");
				    		return;
				    	}
				    chat.sendMessage("That account is not online at the moment.");
			    } catch (XMPPException e) {
			    	log.error(e);
			    }
			}
		});

	}

	@Override
	public String getCommandHelp() {
		return "Send a message to someone ingame.";
	}
}
