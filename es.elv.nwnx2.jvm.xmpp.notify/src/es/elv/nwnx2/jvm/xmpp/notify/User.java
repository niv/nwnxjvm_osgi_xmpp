package es.elv.nwnx2.jvm.xmpp.notify;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.annotation.PrivateOwned;

@Entity
public class User {
	@Id
	public long id;

	@Column(nullable=false, unique=true)
	public String jabberId;

	public String ingameAccount;

	@OneToMany(cascade=CascadeType.ALL,mappedBy="user")
	@PrivateOwned
	public List<Registration> registrations = new LinkedList<Registration>();
}
