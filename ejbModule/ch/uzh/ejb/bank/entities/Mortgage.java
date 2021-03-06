package ch.uzh.ejb.bank.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="MORTGAGES")
public class Mortgage implements Serializable {

	private static final long serialVersionUID = -8975531706848997357L;

	@Id
	@GeneratedValue
	private long id;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="customerId")
	private Customer customer;
	
	private double ammount;
	
	public Mortgage(Customer customer, double sum) {
		this.customer = customer;
		this.ammount = sum;
	}
	
	public Mortgage() {}

	public long getId() {
		return id;
	}
	
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public double getAmmount() {
		return ammount;
	}
	public void setAmmount(double ammount) {
		this.ammount = ammount;
	}
}
