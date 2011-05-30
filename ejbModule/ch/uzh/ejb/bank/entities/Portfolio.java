package ch.uzh.ejb.bank.entities;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="PORTFOLIOS")
@NamedQueries({
	@NamedQuery(name="Portfolio.findByCustomer",
			query="SELECT OBJECT(p) FROM Portfolio p WHERE p.customer=:customer")
})
public class Portfolio implements Serializable {

	private static final long serialVersionUID = -3157991335085902375L;
	
	@Id
	@GeneratedValue
	private long portfolioId;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="customerId")
	private Customer customer;
	
	@OneToMany(mappedBy="portfolio", fetch=FetchType.EAGER)
	private List<Share> shares;

	public Portfolio() {
		this.shares = new LinkedList<Share>();
	}
	
	public Portfolio(Customer customer) {
		this();
		this.customer = customer;
	}

	public long getPortfolioId() {
		return portfolioId;
	}

	public Customer getCustomer() {
		return customer;
	}
	
	public List<Share> getShares() {
		return shares;
	}
}
