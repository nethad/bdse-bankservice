package ch.uzh.ejb.bank.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="CUSTOMERS")
@NamedQueries({
	@NamedQuery(name="Customer.findById",
		query="SELECT DISTINCT OBJECT(c) FROM Customer c WHERE c.customerId=:id"),
	@NamedQuery(name="Customer.findByfirstAndLastName",
		query="SELECT OBJECT(c) FROM Customer c WHERE c.firstName=:firstName AND c.lastName=:lastName"),
	@NamedQuery(name="Customer.findByUserName",
		query="SELECT DISTINCT OBJECT(c) FROM Customer c WHERE c.userName=:userName"),
	@NamedQuery(name="Customer.findAllCustomers",
		query="SELECT OBJECT(c) FROM Customer c")
})
public class Customer implements Serializable {
	private static final long serialVersionUID = -8392985723325257702L;

	public enum Gender {
		MALE,
		FEMALE,
		OTHER
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="CUSTOMERS_SEQ")
	@SequenceGenerator(name="CUSTOMERS_SEQ", sequenceName="CUSTOMERS_SEQ")
	private long customerId;
	private String userName;
	private String password;
	private String firstName;
	private String lastName;
	private String address;
	private Gender gender;
	private String nationality;
	
	public Customer() {}

	public Customer(String userName, String password, String firstName, String lastName, String address, Gender gender, String nationality) {
		this.userName = userName;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.gender = gender;
		this.nationality = nationality;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}
}
