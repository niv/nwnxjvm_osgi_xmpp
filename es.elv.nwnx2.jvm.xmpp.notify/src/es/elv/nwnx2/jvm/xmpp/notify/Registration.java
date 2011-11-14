package es.elv.nwnx2.jvm.xmpp.notify;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(
	uniqueConstraints=@UniqueConstraint(columnNames={"user", "resource"})
)
public class Registration {

	@Id
	public long id;

	@ManyToOne
	@Column(nullable=false)
	public User user;

	public String resource;

	@Column(nullable=false)
	public boolean onlineOnly;
}
