package es.elv.nwnx2.jvm.xmpp.commands.activator;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.commands.XMPPCommandListener;

public class MessageListenerImpl extends Feature implements MessageListener {
	@Override
	protected void load() throws Exception {
		// trackService(XMPPCommandListener.class);
	}

	@Override
	public void processMessage(Chat chat, Message message) {
		boolean handled = false;
		if (null == message || null == message.getBody())
			return;

		final String[] va = message.getBody().split(" +", 2);
		final String command = va[0];
		String arguments = "";
		if (va.length > 1)
			arguments = va[1];

		for (XMPPCommandListener ss : locateServicesFor(XMPPCommandListener.class)) {
			for(String cv : ss.getCommandNames())
				if (cv.equalsIgnoreCase(command)) {
					try {
						ss.handleCommand(chat, command, arguments);
						log.debug(chat.getParticipant() + " $$ " + message.getBody());

					} catch (XMPPException ex) {
						log.error("in command of " + ss.getClass().toString(), ex);
						continue;
					}
					handled = true;
					break;
				}
			if (handled)
				break;
		}

		// Compare against the error to prevent XMPP ICQ transport service feedback loops.
		// It sometimes does that otherwise in fringe cases.
		if (!message.getBody().equals("No such command.") && !handled)
			try {
				chat.sendMessage("No such command.");
			} catch (XMPPException e) {
				log.error("Error rejecting command.", e);
			}
	}
}
