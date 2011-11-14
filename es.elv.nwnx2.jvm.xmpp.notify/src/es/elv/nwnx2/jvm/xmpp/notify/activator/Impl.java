package es.elv.nwnx2.jvm.xmpp.notify.activator;

import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import es.elv.nwnx2.jvm.events.game.PlayerLogin;
import es.elv.nwnx2.jvm.events.game.PlayerLogout;
import es.elv.nwnx2.jvm.hierarchy.DM;
import es.elv.nwnx2.jvm.hierarchy.PlayerCreature;
import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.hierarchy.events.Handler;
import es.elv.nwnx2.jvm.xmpp.XMPPService;
import es.elv.nwnx2.jvm.xmpp.notify.Registration;
import es.elv.nwnx2.jvm.xmpp.notify.User;
import es.elv.osgi.base.Autolocate;
import es.elv.osgi.persistence.ebean.PersistenceService;
import es.elv.osgi.persistence.ebean.PersistentModelClassService;

public class Impl extends Feature implements PersistentModelClassService {

	@Autolocate
	protected XMPPService xse;

	@Autolocate
	protected PersistenceService p;


	protected void sendToAll(String message, PlayerCreature actor) {
		List<Registration> findList =
			p.get().find(Registration.class).join("user").findList();

		for (Registration r : findList) {
			String jid = r.user.jabberId;

			try {
				boolean send = true;

				if (r.onlineOnly) {
					Presence entry;
					if (r.resource != null)
						entry = xse.getConnection().getRoster().getPresenceResource(jid + "/" + r.resource);
					else
						entry = xse.getConnection().getRoster().getPresence(jid);

					if (entry.isAvailable())
						send = true;
					else
						send = false;
				}

				if (r.user.ingameAccount != null && r.user.ingameAccount.equals(actor.getAccount()))
					send = false;

				if (send)
					xse.sendMessage(jid, message);

			} catch (XMPPException e1) {
				log.error("", e1);
			}
		}
	}


	@Handler void onPlayerLogin(PlayerLogin e) {
		final String isDM = (e.player instanceof DM) ? "[DM] " : "";
		final String message = String.format("%s (%s) %sentered.", e.player.getName(), e.player.getAccount(), isDM);
		sendToAll(message, e.player);
	}

	@Handler void onPlayerLogout(PlayerLogout e) {
		final String isDM = (e.player instanceof DM) ? "[DM] " : "";
		final String message = String.format("%s (%s) %sleft.", e.player.getName(), e.player.getAccount(), isDM);
		sendToAll(message, e.player);
	}


	@Override
	public Class<?>[] getAnnotatedModels() {
		return new Class<?>[] { Registration.class, User.class };
	}
}

