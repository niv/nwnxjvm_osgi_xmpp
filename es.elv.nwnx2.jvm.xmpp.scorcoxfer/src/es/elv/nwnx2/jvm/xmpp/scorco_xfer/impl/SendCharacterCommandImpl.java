package es.elv.nwnx2.jvm.xmpp.scorco_xfer.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.nwnx.nwnx2.jvm.SCORCO;
import org.nwnx.nwnx2.jvm.aspectj.Schedule;
import org.nwnx.nwnx2.jvm.aspectj.ScheduleWait;

import es.elv.nwnx2.jvm.hierarchy.PlayerCreature;
import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.XMPPService;
import es.elv.nwnx2.jvm.xmpp.commands.XMPPCommandListener;

public class SendCharacterCommandImpl extends Feature implements XMPPCommandListener {

	private XMPPService xmpp;

	static private Map<String, String> authorizedMap = new HashMap<String, String>();
	static {
		authorizedMap.put("Kyareth", "elven@swordcoast.net");
		authorizedMap.put("Esthae", "elven@swordcoast.net");
		authorizedMap.put("Sayenne", "evarthiel@swordcoast.net");
		authorizedMap.put("Thandawave", "evarthiel@swordcoast.net");
	}

	@Override
	protected void load() throws Exception {
		xmpp = locateServiceFor(XMPPService.class);
	}


	@Override
	public String getCommandHelp() {
		return null;
	}

	@Override
	public String[] getCommandNames() {
		return new String[] { "getCharacter" };
	}

	@Override
	public void handleCommand(Chat chat, String command, String arguments)
			throws XMPPException {
		doIt(chat);
	}

	@Schedule void doIt(Chat chat) throws XMPPException {
		final String p = chat.getParticipant().split("/", 2)[0];
		try {
			final PlayerCreature target = getByJID(p);
			if (target == null) {
				chat.sendMessage("You're not online.");
				return;
			}

			final byte[] data = SCORCO.saveObject(target);
			chat.sendMessage("Data size: " + data.length + "  ..");

			OutgoingFileTransfer transfer = xmpp.getFileTransferManager().createOutgoingFileTransfer(p);
			InputStream is = new ByteArrayInputStream(data);
			transfer.sendStream(is, "character.bic", data.length, "");
		} catch (Exception e) {
			log.error(e);
		}
	}

	@ScheduleWait PlayerCreature getByJID(String jid) throws Exception {
	    for (PlayerCreature p : PlayerCreature.all())
	    	for (Entry<String, String> ee: authorizedMap.entrySet())
	    		if (ee.getValue().equals(jid) && ee.getKey().equals(p.getAccount()))
	    			return p;
	    return null;
	}

}
