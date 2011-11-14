package es.elv.nwnx2.jvm.xmpp.commands;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;

public interface XMPPCommandListener {
	/**
	 * Called when someone requests one of the registered command names.
	 *
	 * @param chat the XMPP Chat interface
	 * @param command the used command or alias (one of getCommandNames())
	 * @param arguments the full input String, except the initial command word
	 */
	public void handleCommand(Chat chat, String command, String arguments) throws XMPPException;

	/**
	 * Gets all command names to which this command responds to.
	 * The first one should be the visible command; all following are aliases.
	 * @return array of command names: the canonical name and any optional aliases
	 */
	public String[] getCommandNames();

	/**
	 * Returns the help text for the given command.
	 * @return String, or null, if no help is to be available
	 */
	public String getCommandHelp();
}
