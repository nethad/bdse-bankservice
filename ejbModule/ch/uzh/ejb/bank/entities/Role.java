package ch.uzh.ejb.bank.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="USER_ROLES")
public class Role implements Serializable {
	
	private static final long serialVersionUID = 5699084245742093820L;
	
	@Id
	@GeneratedValue
	@SuppressWarnings("unused")
	private int id;
	
	private String role;
	private String userName;
	
	public Role() {}
	
	public Role(String userName, String role) {
		this.userName = userName;
		this.role = role;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
