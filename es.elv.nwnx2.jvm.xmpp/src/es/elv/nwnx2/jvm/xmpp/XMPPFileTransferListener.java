package es.elv.nwnx2.jvm.xmpp;

import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

public interface XMPPFileTransferListener {
	/**
	 * Return true to handle the given request; it will be rejected otherwise.
	 *
	 * @param request
	 * @return
	 */
	public boolean fileTransferRequest(FileTransferRequest request);
}
