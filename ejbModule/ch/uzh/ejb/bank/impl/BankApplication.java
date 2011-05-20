package ch.uzh.ejb.bank.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.security.auth.spi.Util;

import ch.uzh.ejb.bank.BankApplicationLocal;
import ch.uzh.ejb.bank.BankApplicationRemote;
import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.entities.FinancialTransaction;
import ch.uzh.ejb.bank.entities.Account.Status;
import ch.uzh.ejb.bank.impl.utils.AccountHistoryUtil;

/**
 * Session Bean implementation class BankApplication
 */
@Stateful
@SecurityDomain("bankapplication")
@DeclareRoles({"administrator, clerk, user"})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class BankApplication implements BankApplicationRemote, BankApplicationLocal {

	static final String ADMINISTRATOR_ROLE = "administrator";
	static final String CLERK_ROLE = "clerk";
	static final String USER_ROLE = "user";
//	private static final String[] CLERK_OR_HIGHER = new String[]{ADMINISTRATOR_ROLE, CLERK_ROLE};

	@Resource
	SessionContext context;
	
	@PersistenceContext(unitName="BankApplication")
	EntityManager em;

	Account selectedAccount;
	
    /**
     * Default constructor. 
     */
    public BankApplication() {}

    @PostConstruct
    public void ejbCreate() {
    	System.out.println("BankApplication.ejbCreate()");
    }
    
    @Remove
    @Override
    public void remove() {
    	System.out.println("BankApplication.remove()");
    }
    
    @Override
    @PermitAll
    public void test() {
    	// do nothing
    	// this method is for testing purposes.
    }
    
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
//	@PermitAll
	public Customer createCustomer(String userName, String password, String firstName, String lastName,
			String address, Customer.Gender gender, String nationality) {
		
		password = Util.createPasswordHash("MD5", Util.BASE64_ENCODING, null, null, password);
		
		Customer customer = new Customer(userName, password, firstName, lastName, address, gender, nationality);
		em.persist(customer);
		
		return customer;
	}

	@Override
	@SuppressWarnings("unchecked")
	@PermitAll
	public List<Customer> getCustomer(String firstName, String lastName) {
		List<Customer> customers = null;
		if(firstName != null && lastName != null) {
			Query q = em.createNamedQuery("Customer.findByfirstAndLastName");
			q.setParameter("firstName", firstName);
			q.setParameter("lastName", lastName);
			try {
				customers = q.getResultList();
			} catch(Exception ex) {
				customers = new ArrayList<Customer>();
			}
		}
		return customers;
	}

	@Override
	@PermitAll
	public Customer getCustomer(long id) {
		Customer customer = null;
		if(id >= 0) {
			Query q = em.createNamedQuery("Customer.findById");
			q.setParameter("id", id);
			try {
				customer = (Customer) q.getSingleResult();			
			} catch(Exception ex) {
				customer = null;
			}
		}
		return customer;
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public Account createAccount(double balance,
			Account.Type accountType, float interest, double creditLimit,
			Customer customer) {
		Account account = new Account(balance, accountType, interest, creditLimit, customer);
		em.persist(account);
		FinancialTransaction fta = new FinancialTransaction(account, new Date(), 0.0, 
				AccountHistoryUtil.HISTORY_CREATED);
		em.persist(fta);
		
		return account;
	}

	@Override
	@PermitAll
	public Account getAccount(long accountNumber) {
		Account account = null;
		if(accountNumber >= 0) {
			Query q = em.createNamedQuery("Account.findById");
			q.setParameter("id", accountNumber);
			try {
				account = (Account) q.getSingleResult();			
			} catch(Exception ex) {
				account = null;
			}
		}
		return account;
	}

	@Override
	@SuppressWarnings("unchecked")
	@PermitAll
	public List<Account> getAccounts(Customer customer) {
		List<Account> accounts = null;
		if(customer != null) {
			Query q = em.createNamedQuery("Account.findByCustomer");
			q.setParameter("customer", customer);
			try {
				accounts = q.getResultList();
			} catch(Exception ex) {
				accounts = new ArrayList<Account>();
			}
		}
		return accounts;
	}

	@Override
	@PermitAll
	public void setAccountStatus(Account account, Status status) {
		FinancialTransaction fta = null;
		switch(status) {
		case CLOSED: {
			fta = new FinancialTransaction(account, new Date(), 0.0, 
					AccountHistoryUtil.HISTORY_CLOSED);
			break;
		}
		case OPEN: {
			fta = new FinancialTransaction(account, new Date(), 0.0, 
					AccountHistoryUtil.HISTORY_OPENED);
			break;
		}
		default: {
			throw new IllegalArgumentException("Unknown status: " + status);
		}
		}
		getManagedEntity(account).setAccountStatus(status);
		em.persist(fta);
	}

	@Override
	@PermitAll
	public Account deposit(Account toAccount, double value) {
		if(value < 0.0) {
			throw new IllegalArgumentException("Can only deposit positive values. Use withdraw.");
		}
		toAccount = getAccount(toAccount.getAccountId());
		toAccount.setBalance(toAccount.getBalance() + value);
		em.merge(toAccount);
		FinancialTransaction fta = 
			new FinancialTransaction(toAccount, new Date(), value, 
					AccountHistoryUtil.HISTORY_DEPOSIT);
		em.persist(fta);
		
		return toAccount;
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public Account withdraw(Account fromAccount, double value) {
		if(value < 0.0) {
			throw new IllegalArgumentException("Can only withdraw positive values. Use deposit.");
		}
		fromAccount = getAccount(fromAccount.getAccountId());
		double newValue = fromAccount.getBalance() - value;
		if(newValue < fromAccount.getCreditLimit()) {
			throw new IllegalArgumentException("Can not withdraw specified ammount.");
		}
		fromAccount.setBalance(newValue);
		em.merge(fromAccount);
		FinancialTransaction fta = 
			new FinancialTransaction(fromAccount, new Date(), value, 
					AccountHistoryUtil.HISTORY_WITHDRAWAL);
		em.persist(fta);
		
		return fromAccount;
	}

	@Override
	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void transfer(Account fromAccount, Account toAccount, double value) {
		if(value < 0.0) {
			context.setRollbackOnly();
			throw new IllegalArgumentException("Can only transfer positive values.");
//			return;
		} else if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(fromAccount)) {
			throw new RuntimeException("You are not owner of this account.");
		}
		withdraw(fromAccount, value);
		deposit(toAccount, value);
	}
	
	<T> T getManagedEntity(T entity) {
		return em.merge(entity);
	}

	@Override
	public void selectAccount(long id) {
		Account account = getAccount(id);
		if (account == null) {
			throw new RuntimeException("Account is not existant");
		} else if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
			throw new RuntimeException("You are not owner of this account.");
		} else {
			// everything's OK
			this.selectedAccount = account;
		}
	}
	
	boolean isLoggedInUserAccountOwnerOrClerkOrAdmin(Account account) {
		if (context.isCallerInRole(ADMINISTRATOR_ROLE) || context.isCallerInRole("clerk")) {
			return true;
		}
		
		if (account.getCustomer().getUserName().equals(loggedInUserName()) ||
				loggedInUserIsAdmin()) {
			return true;
		} else {
			return false;
		}
	}
	
	String loggedInUserName() {
		return context.getCallerPrincipal().getName();
	}
	
	boolean loggedInUserIsAdmin() {
		return loggedInUserName().equals("admin");
	}

	@Override
	public long getSelectedAccountId() {
		if (this.selectedAccount == null) {
			throw new RuntimeException("No account selected.");
		}
		return this.selectedAccount.getAccountId();
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	@SuppressWarnings("unchecked")
	public String getAccountHistory(Account account, Date from, Date to) {
		StringBuilder sb = new StringBuilder();
		
		if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
			throw new RuntimeException("You are not owner of this account.");
		}
		
		List<FinancialTransaction> transactions = null;
		if(account != null) {
			Query q = em.createNamedQuery("FIN_TA.findByAccount");
			q.setParameter("account", account);
			try {
				transactions = q.getResultList();
				if(transactions.size() > 0) {
					sb.append(AccountHistoryUtil.HISTORY_HEADER);
					sb.append('\n');
					sb.append(AccountHistoryUtil.SEPERATOR);
					for(FinancialTransaction transaction : transactions) {
						sb.append('\n');
						String description = transaction.getDescription();
						sb.append(description);
						if(description.equals(AccountHistoryUtil.HISTORY_WITHDRAWAL) || 
								description.equals(AccountHistoryUtil.HISTORY_DEPOSIT)) {
							sb.append(transaction.getAmount());
						}
					}
				}
			} catch(Exception ex) {
				transactions = Collections.emptyList();
			}
		} else {
			throw new IllegalArgumentException("Account can not be null.");
		}
		
		return sb.toString();
	}
}
