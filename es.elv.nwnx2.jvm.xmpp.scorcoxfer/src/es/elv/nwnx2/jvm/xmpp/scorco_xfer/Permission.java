package es.elv.nwnx2.jvm.xmpp.scorco_xfer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Permission {

	@Id
	public long id;

	@Column(nullable=false)
	public String jabberId;

	@Column(nullable=false)
	public String account;
}