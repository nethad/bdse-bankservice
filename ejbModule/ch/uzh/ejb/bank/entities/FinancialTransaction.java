package ch.uzh.ejb.bank.entities;

import java.io.Serializable;
import java.util.Date;

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
@Table(name="FIN_TA")
@NamedQueries({
	@NamedQuery(name="FIN_TA.findById",
		query="SELECT DISTINCT OBJECT(f) FROM FinancialTransaction f WHERE f.transactionId=:id"),
	@NamedQuery(name="FIN_TA.findByAccount",
		query="SELECT OBJECT(f) FROM FinancialTransaction f WHERE f.account=:account"),
	@NamedQuery(name="FIN_TA.findByAccountAndDescription",
			query="SELECT OBJECT(f) FROM FinancialTransaction f WHERE f.account=:account AND f.description=:description")
})
public class FinancialTransaction implements Serializable {
	private static final long serialVersionUID = 9156961808314737011L;
	
	@Id
	@GeneratedValue
	private long transactionId;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="accountId")
	private Account account;
	
	private Date date;
	
	private double amount;
	
	private String description;
	
	public FinancialTransaction() {}
	
	public FinancialTransaction(Account account,
			Date date, double amount, String description) {
		super();
		this.account = account;
		this.date = date;
		this.amount = amount;
		this.description = description;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Date getDate() {
		return date;
	}

	public void setTimestamp(Date date) {
		this.date = date;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
