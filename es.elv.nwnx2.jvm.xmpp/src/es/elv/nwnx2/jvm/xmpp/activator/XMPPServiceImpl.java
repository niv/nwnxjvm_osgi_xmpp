package es.elv.nwnx2.jvm.xmpp.activator;

import java.util.Collection;
import java.util.Dictionary;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.XMPPFileTransferListener;
import es.elv.nwnx2.jvm.xmpp.XMPPService;

public class XMPPServiceImpl extends Feature implements
		ChatManagerListener, MessageListener,RosterListener, ConnectionListener,
		FileTransferListener, XMPPService,
		ManagedService {
	private XMPPConnection connection;

	private ConnectionConfiguration config;

	private String host = null;
	private String service = null;
	private Integer port = null;

	private String username = null;
	private String password = null;
	private String resource = null;

	private String presenceMessage = null;
	private Integer presencePriority = null;
	private Presence.Mode presenceMode = null;
	private boolean presenceAppendVersion = true;

	private FileTransferManager fileTransferManager = null;

	@Override
	protected void unload() throws Exception {
		disconnect();
	}

	private void disconnect() {
		if (connection != null) {
			connection.disconnect();
			config = null;
			connection = null;
			fileTransferManager = null;
		}

	}

	private void connect() throws XMPPException {
		disconnect();

		config = new ConnectionConfiguration(host, port, service);
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		SASLAuthentication.supportSASLMechanism("PLAIN", 0);

		connection = new XMPPConnection(config);

		// According to the docs, this should re-connect in intervals if the
		// connection fails.
		connection.connect();
		connection.login(username, password, resource);
		String message = presenceMessage;
		if (presenceAppendVersion)
			message += " (" + bundleContext.getBundle().getSymbolicName() + " " + bundleContext.getBundle().getVersion() + ")";

		connection.sendPacket(new Presence(Presence.Type.available, message,
			presencePriority, presenceMode));

		connection.getChatManager().addChatListener(this);
		connection.getRoster().addRosterListener(this);
		connection.addConnectionListener(this);
		fileTransferManager  = new FileTransferManager(connection);
		fileTransferManager.addFileTransferListener(this);
		FileTransferNegotiator.setServiceEnabled(connection, true);
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		chat.addMessageListener(this);
		for (ChatManagerListener ss : locateServicesFor(ChatManagerListener.class))
			ss.chatCreated(chat, createdLocally);
	}

	@Override
	public void updated(@SuppressWarnings("rawtypes") Dictionary dv) throws ConfigurationException {
		if (dv == null)
			return;
		try {

			service = (String) dv.get("service");
			host = (String) dv.get("host");
			port = (Integer) dv.get("port");
			password = (String) dv.get("password");
			username = (String) dv.get("username");
			resource = (String) dv.get("resource");
			presenceMessage = (String) dv.get("presenceMessage");
			presencePriority = (Integer) dv.get("presencePriority");
			presenceMode = Presence.Mode.valueOf((String) dv.get("presenceMode"));
			presenceAppendVersion = (Boolean) dv.get("presenceAppendVersion");

			if (username != null && password != null && resource != null &&
					service != null && host != null && port != null &&
					presenceMessage != null && presenceMode != null && presencePriority != null)
				connect();

		} catch (XMPPException e) {
			throw new ConfigurationException("(everything)", e.getMessage(), e);
		}
	}

	@Override
	public void processMessage(Chat chat, Message message) {
		for (MessageListener ss : locateServicesFor(MessageListener.class))
			try {
				ss.processMessage(chat, message);
			} catch (Exception e) {
				log.error(e);
			}
	}

	@Override
	public void entriesAdded(Collection<String> addresses) {
		for (RosterListener ss : locateServicesFor(RosterListener.class))
			try {
				ss.entriesAdded(addresses);
			} catch (Exception e) {
				log.error(e);
			}
	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		for (RosterListener ss : locateServicesFor(RosterListener.class))
			try {
				ss.entriesDeleted(addresses);
			} catch (Exception e) {
				log.error(e);
			}

	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
		for (RosterListener ss : locateServicesFor(RosterListener.class))
			try {
				ss.entriesUpdated(addresses);
			} catch (Exception e) {
				log.error(e);
			}

	}

	@Override
	public void presenceChanged(Presence presence) {
		for (RosterListener ss : locateServicesFor(RosterListener.class))
			try {
				ss.presenceChanged(presence);
			} catch (Exception e) {
				log.error(e);
			}
}


	@Override
	public void connectionClosed() {
		for (ConnectionListener ss : locateServicesFor(ConnectionListener.class))
			try {
				ss.connectionClosed();
			} catch (Exception e) {
				log.error(e);
			}
}

	@Override
	public void connectionClosedOnError(Exception e) {
		for (ConnectionListener ss : locateServicesFor(ConnectionListener.class))
			try {
				ss.connectionClosedOnError(e);
			} catch (Exception ex) {
				log.error(ex);
			}
	}

	@Override
	public void reconnectingIn(int seconds) {
		for (ConnectionListener ss : locateServicesFor(ConnectionListener.class))
			try {
				ss.reconnectingIn(seconds);
			} catch (Exception e) {
				log.error(e);
			}
	}

	@Override
	public void reconnectionFailed(Exception e) {
		for (ConnectionListener ss : locateServicesFor(ConnectionListener.class))
			try {
				ss.reconnectionFailed(e);
			} catch (Exception ex) {
				log.error(ex);
			}
	}

	@Override
	public void reconnectionSuccessful() {
		for (ConnectionListener ss : locateServicesFor(ConnectionListener.class))
			try {
				ss.reconnectionSuccessful();
			} catch (Exception e) {
				log.error(e);
			}
	}

	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		for (XMPPFileTransferListener ss : locateServicesFor(XMPPFileTransferListener.class))
			try {
				if (ss.fileTransferRequest(request))
					return;
			} catch (Exception e) {
				log.error(e);
			}
		request.reject();
	}

	@Override
	public void sendMessage(String jid, String message) throws XMPPException {
		getChat(jid).sendMessage(message);
	}

	private Chat getChat(String jid) {
		return connection.getChatManager().createChat(jid, this);
	}

	@Override
	public XMPPConnection getConnection() {
		return connection;
	}

	@Override
	public FileTransferManager getFileTransferManager() {
		return fileTransferManager;
	}
}
