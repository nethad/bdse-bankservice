package ch.uzh.ejb.bank.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="SHARES")
@NamedQueries({
	@NamedQuery(name="Share.findBySymbolAndPortfolio",
			query="SELECT OBJECT(s) FROM Share s WHERE s.symbol=:symbol AND s.portfolio=:portfolio")
})
public class Share implements Serializable {
	
	private static final long serialVersionUID = 8599928676653269841L;
	
	@Id
	@GeneratedValue
	private long shareId;
	
	private String symbol;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="portfolioId")
	private Portfolio portfolio;
	
	private long quantity;
	
	private double avgPrice;
	
	public Share() {}

	public Share(String symbol, long quantity, double purchasePrice) {
		this.symbol = symbol;
		this.quantity = quantity;
		this.avgPrice = purchasePrice;
	}

	public long getShareId() {
		return shareId;
	}
	
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Portfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		if(this.portfolio instanceof Portfolio) {
			this.portfolio.getShares().remove(this);
		}
		this.portfolio = portfolio;
		if(!portfolio.getShares().contains(this)) {
			portfolio.getShares().add(this);
		}
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public double getAveragePurchasePrice() {
		return avgPrice;
	}

	public void setAveragePurchasePrice(double purchasePrice) {
		this.avgPrice = purchasePrice;
	}
}
