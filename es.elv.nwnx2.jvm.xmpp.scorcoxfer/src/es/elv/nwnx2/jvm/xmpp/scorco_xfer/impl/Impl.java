package es.elv.nwnx2.jvm.xmpp.scorco_xfer.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.nwnx.nwnx2.jvm.NWObject;
import org.nwnx.nwnx2.jvm.SCORCO;
import org.nwnx.nwnx2.jvm.aspectj.ScheduleWait;
import org.progeeks.nwn.gff.Element;
import org.progeeks.nwn.gff.Struct;
import org.progeeks.nwn.io.gff.GffReader;

import es.elv.nwnx2.jvm.hierarchy.PlayerCreature;
import es.elv.nwnx2.jvm.hierarchy.bundles.Feature;
import es.elv.nwnx2.jvm.xmpp.XMPPFileTransferListener;
import es.elv.nwnx2.jvm.xmpp.XMPPService;
import es.elv.nwnx2.jvm.xmpp.scorco_xfer.Permission;
import es.elv.osgi.base.Autolocate;
import es.elv.osgi.persistence.ebean.PersistenceService;
import es.elv.osgi.persistence.ebean.PersistentModelClassService;

public class Impl extends Feature implements XMPPFileTransferListener, PersistentModelClassService {

	@Autolocate
	private PersistenceService p;

	@Autolocate
	private XMPPService xmpp;

	@Override
	public boolean fileTransferRequest(FileTransferRequest request) {
		String rq = request.getRequestor().split("/")[0];
		if (p.get().find(Permission.class).where().ieq("jabberId", rq).findList().size() == 0) {
			try {
				xmpp.sendMessage(request.getRequestor(), "I don't need your stuff!");
			} catch (XMPPException e) {
				log.error(e);
			}
			return false;
		}

		try {
			return null != retrieveThread(request.accept());
		} catch (Exception e) {
			log.error(e);
			return false;
		}
	}

	@ScheduleWait PlayerCreature getByJID(String jid) throws Exception {
		final List<Permission> perm = p.get().find(Permission.class).where().ieq("jabberId", jid).findList();
		if (perm.size() < 1)
			return null;

	    for (final PlayerCreature p : PlayerCreature.all())
	    	for (Permission pp : perm)
	    		if (p.getAccount().equalsIgnoreCase(pp.account))
	    			return p;

	    return null;
	}


	private Thread retrieveThread(final IncomingFileTransfer ft) throws Exception {
		final String rq = ft.getPeer().split("/")[0];

		final PlayerCreature target = getByJID(rq);

	    if (null == target) {
	    	ft.cancel();
			xmpp.sendMessage(ft.getPeer(), "You're not online.");
	    	return null;
	    }

		Thread tt = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					InputStream recieveFile = ft.recieveFile();
				    ByteArrayOutputStream buf = new ByteArrayOutputStream();
				    int result = recieveFile.read();
				    while(result != -1) {
				      byte b = (byte)result;
				      buf.write(b);
				      result = recieveFile.read();
				    }
				    final byte[] data = buf.toByteArray();

				    try {
				    	xmpp.sendMessage(ft.getPeer(), "Got " + ft.getFileName() + " (" + data.length + " bytes), verifying ..");
				    	Struct t = verify(data);
				    	Element name =  t.getValue("LocalizedName");
				    	String lName = "";
				    	if (name != null) {
				    		lName = name.getStringValue();
				    	} else {
				    		if (t.getValue("FirstName") != null)
				    			lName += t.getValue("FirstName").getStringValue();
				    		if (t.getValue("LastName") != null)
				    			lName += t.getValue("LastName").getStringValue();
				    	}
				    	xmpp.sendMessage(ft.getPeer(), " object name: " + lName);
				    	if (!doLoad(data, target))
				    		throw new RuntimeException("Sent file does not load properly via RCO.");

				    	log.info(ft.getPeer() + " imported " + ft.getFileName() + " name = " + lName);
				    	xmpp.sendMessage(ft.getPeer(), "Imported.");
				    } catch (IOException exx) {
				    	xmpp.sendMessage(ft.getPeer(), "Sent file does not pass verification: " + exx.getMessage());
				    }
				} catch (XMPPException e1) {
					log.error(e1);
				} catch (Exception e) {
					log.error(e);
					try {
						xmpp.sendMessage(ft.getPeer(), "Error retrieving file: " + e.getMessage());
					} catch (XMPPException e1) {
						log.error(e1);
					}
				}
			}
		});
		tt.start();
		return tt;
	}

	@ScheduleWait private boolean doLoad(final byte[] data, final PlayerCreature target) throws Exception {
		log.info("Loading to target: " + target + " size = " + data.length + " pData=" + Arrays.toString(Arrays.copyOfRange(data, 0, 8)));
		NWObject o = SCORCO.loadObject(data, target.getLocation(), target);
		return o != null;
	}

	private Struct verify(byte[] data) throws IOException {
		GffReader gffReader = new GffReader(new ByteArrayInputStream(data));
		String type = gffReader.getHeader().getType().toLowerCase().trim();
		if (!type.equals("uti") && !type.equals("utc") && !type.equals("bic"))
			throw new IOException("is not a supported GFF file (found: " + type + ")");
		String vers = gffReader.getHeader().getVersion().toLowerCase().trim();
		if (!vers.equals("v3.2"))
			throw new IOException("is not a supported GFF version (found: " + vers + ")");

		return gffReader.readRootStruct();
	}

	@Override
	public Class<?>[] getAnnotatedModels() {
		return new Class<?>[] { Permission.class };
	}
}
