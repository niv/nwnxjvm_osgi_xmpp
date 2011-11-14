package es.elv.nwnx2.jvm.xmpp;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;

public interface XMPPService {
	public void sendMessage(String jid, String message) throws XMPPException;
	public XMPPConnection getConnection();
	public FileTransferManager getFileTransferManager();
}
