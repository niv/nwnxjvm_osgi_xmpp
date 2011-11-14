package es.elv.nwnx2.jvm.xmpp.commands.online.activator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.nwnx.nwnx2.jvm.Scheduler;
import org.nwnx.nwnx2.jvm.aspectj.Schedule;

import es.elv.nwnx2.jvm.hierarchy.DM;
import es.elv.nwnx2.jvm.hierarchy.PlayerCreature;
import es.elv.nwnx2.jvm.xmpp.commands.XMPPCommandListener;

public class Activator implements XMPPCommandListener {

	@Override
	public String[] getCommandNames() {
		return new String[] {"online", "o"};
	}

	@Override
	public void handleCommand(Chat chat, String command, String arguments) throws XMPPException {
		Scheduler.schedule(new Runnable() {
			@Override public void run() {
				PlayerCreature[] players = PlayerCreature.all();

				if (players.length > 0) {
					StringBuilder b = new StringBuilder();
					for (PlayerCreature p : players) {
						b.append(p.getName() + " (" + p.getAccount() + ")");
						if (p instanceof DM)
							b.append(" [DM]");
						b.append(", ");
					}

					chat.sendMessage("Online: " + b.substring(0, b.length() - 2));

				} else
					chat.sendMessage("Noone online.");
			}
		});
	}

	@Override
	public String getCommandHelp() {
		return "Shows the names of all players currently online.";
	}
}