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
@Table(name="SHARES")
public class Share implements Serializable {
	
	private static final long serialVersionUID = 8599928676653269841L;
	
	public Share() {}
	
	@Id
	@GeneratedValue
	private long shareId;
	
	private String symbol;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="portfolioId")
	private Portfolio portfolio;
	
	private long quantity;
	
	private double purchasePrice;

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
		this.portfolio = portfolio;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public double getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}
}
