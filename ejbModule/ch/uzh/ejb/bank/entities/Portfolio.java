package ch.uzh.ejb.bank.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="PORTFOLIOS")
public class Portfolio implements Serializable {

	private static final long serialVersionUID = -3157991335085902375L;
	
	@Id
	@GeneratedValue
	private long portfolioId;
	
	private Customer customer;
	
	@OneToMany(mappedBy="portfolio")
	private List<Share> stocks;

	public long getPortfolioId() {
		return portfolioId;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<Share> getStocks() {
		return stocks;
	}

	public void setStocks(List<Share> stocks) {
		this.stocks = stocks;
	}
}
