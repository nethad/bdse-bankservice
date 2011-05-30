package ch.uzh.ejb.bank.impl;

import java.util.ArrayList;
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
import ch.uzh.ejb.bank.entities.Mortgage;
import ch.uzh.ejb.bank.impl.utils.AccountHistoryUtil;
import ch.uzh.ejb.bank.process.MortgageApplication;

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

	private Account selectedAccount;
	private Customer selectedCustomer;
	
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
    
    @SuppressWarnings("unchecked")
	private Customer getCustomerByUsername(String userName) {
    	List<Customer> customers = null;
		if(userName != null) {
			Query q = em.createNamedQuery("Customer.findByUserName");
			q.setParameter("userName", userName);
			try {
				customers = q.getResultList();
			} catch(Exception ex) {
				customers = new ArrayList<Customer>();
			}
		}
		return customers.get(0);
    }
    
    @Override
    @PermitAll
    public void setDefaultCustomer() {
    	String userName = context.getCallerPrincipal().getName();
    	this.selectedCustomer = getCustomerByUsername(userName);
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
//	@PermitAll
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
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
//	@PermitAll
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
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
	
	@SuppressWarnings("unchecked")
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public List<Customer> getAllCustomers() {
		List<Customer> customers = null;
		Query q = em.createNamedQuery("Customer.findAllCustomers");
		try {
			customers = q.getResultList();	
		} catch(Exception ex) {
			customers = new ArrayList<Customer>();
		}
		return customers;
	}
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public Account createAccount(double balance,
			Account.Type accountType, float interest, double creditLimit) throws Exception {
		
		checkIfCustomerIsSelected();
		
		Account account = new Account(0.0, accountType, interest, creditLimit);
		account.setCustomer(getManagedEntity(this.selectedCustomer));
		account.setAccountStatus(Status.OPEN);
		em.persist(account);
		FinancialTransaction fta = new FinancialTransaction(account, new Date(), 0.0, 
				AccountHistoryUtil.HISTORY_CREATED);
		em.persist(fta);
		
		// Account is generated with zero balance, this is the initial deposit.
		deposit(account, balance);
		
		return account;
	}

	private void checkIfCustomerIsSelected() throws Exception {
		if (this.selectedCustomer == null) {
			throw new Exception("No customer object given");
		}
	}
	
	private void checkIfAccountIsSelected() throws Exception {
		if (this.selectedAccount == null) {
			throw new Exception("No account object given");
		}
	}
	
	private void checkIfAccountIsOpen(Account account) throws Exception {
		if (account.getAccountStatus() != Status.OPEN) {
			throw new Exception("Account is closed.");
		}
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
	public List<Account> getAccounts() throws Exception {
		checkIfCustomerIsSelected();
		return getAccounts(selectedCustomer);
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@SuppressWarnings("unchecked")
	public List<Account> getAllAccounts() {
		List<Account> accounts = null;
			Query q = em.createNamedQuery("Account.findAllAccounts");
			try {
				accounts = q.getResultList();
			} catch(Exception ex) {
				accounts = new ArrayList<Account>();
			}
		return accounts;
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void setAccountStatus(Account account, Status status) throws Exception {
		
		if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
			throw new Exception("You are not owner of this account.");
		}
		
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
			throw new Exception("Unknown status: " + status);
		}
		}
		getManagedEntity(account).setAccountStatus(status);
		em.persist(fta);
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Account deposit(Account toAccount, double value) throws Exception {
		if(value < 0.0) {
			throw new Exception("Can only deposit positive values. Use withdraw.");
		}
		checkIfAccountIsOpen(toAccount);
		
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
	public Account deposit(double value) throws Exception {
		checkIfAccountIsSelected();
		return deposit(selectedAccount, value);
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Account withdraw(Account fromAccount, double value) throws Exception {
		if(value < 0.0) {
			throw new Exception("Can only withdraw positive values. Use deposit.");
		}
		checkIfAccountIsOpen(fromAccount);
		
		fromAccount = getAccount(fromAccount.getAccountId());
		double newValue = fromAccount.getBalance() - value;
		if(newValue < fromAccount.getCreditLimit()) {
			throw new Exception("Can not withdraw specified ammount.");
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
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public Account withdraw(double value) throws Exception {
		checkIfAccountIsSelected();
		return withdraw(selectedAccount, value);
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void transfer(Account fromAccount, Account toAccount, double value) throws Exception {
		if(value < 0.0) {
			context.setRollbackOnly();
			throw new Exception("Can only transfer positive values.");
		} else if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(fromAccount)) {
			throw new Exception("You are not owner of this account.");
		}
		withdraw(fromAccount, value);
		deposit(toAccount, value);
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public void transfer(long targetAccountId, double value) throws Exception {
//		if(value < 0.0) {
//			context.setRollbackOnly();
//			throw new Exception("Can only transfer positive values.");
//		} else if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(fromAccount)) {
//			throw new Exception("You are not owner of this account.");
//		}
		checkIfAccountIsSelected();
		Account toAccount = getAccount(targetAccountId);
		if (toAccount == null) {
			throw new Exception("Target account does not exist.");
		}
		transfer(selectedAccount, toAccount, value);
	}
	
	<T> T getManagedEntity(T entity) {
		return em.merge(entity);
	}

	@Override
	public void selectAccount(long id) throws Exception {
		Account account = getAccount(id);
		if (account == null) {
			throw new Exception("Account is not existant");
		} else if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
			throw new Exception("You are not owner of this account.");
		} else {
			// everything's OK
			this.selectedAccount = account;
		}
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public void selectCustomer(long id) {
		Customer customer = getCustomer(id);
		if (customer == null) {
			throw new RuntimeException("Customer is not existant");
		} else {
			// everything's OK
			this.selectedCustomer = customer;
		}
	}
	
	@Override
	public long getSelectedCustomerId() throws Exception {
		checkIfCustomerIsSelected();
		return this.selectedCustomer.getCustomerId();
	}
	
	boolean isLoggedInUserAccountOwnerOrClerkOrAdmin(Account account) {
		if (context.isCallerInRole(ADMINISTRATOR_ROLE) || context.isCallerInRole(CLERK_ROLE)) {
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
	public long getSelectedAccountId() throws Exception {
		if (this.selectedAccount == null) {
			throw new Exception("No account selected.");
		}
		return this.selectedAccount.getAccountId();
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	@SuppressWarnings("unchecked")
	public String getAccountHistory(Account account, Date from, Date to) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		if(!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
			throw new Exception("You are not owner of this account.");
		}
		
		List<FinancialTransaction> transactions = null;
		if(account != null) {
			Query q = em.createNamedQuery("FIN_TA.findByAccount");
			q.setParameter("account", account);
			transactions = q.getResultList();
			if(transactions.size() > 0) {
				sb.append(AccountHistoryUtil.HISTORY_HEADER);
				sb.append('\n');
				sb.append(AccountHistoryUtil.SEPERATOR);
				for(FinancialTransaction transaction : 
						AccountHistoryUtil.filterTransactionsByTimeRange(transactions, from, to)) {
					
					sb.append('\n');
					String description = transaction.getDescription();
					sb.append(description);
					if(description.equals(AccountHistoryUtil.HISTORY_WITHDRAWAL) || 
							description.equals(AccountHistoryUtil.HISTORY_DEPOSIT)) {
						sb.append(transaction.getAmount());
					}
				}
			}
		} else {
			throw new IllegalArgumentException("Account can not be null.");
		}
		
		return sb.toString();
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public double getTotalBalance(Customer customer) throws Exception {
		List<Account> accounts = getAccounts(customer);
		double balance = 0;
		for(Account account : accounts) {
			if(!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
				throw new Exception("You are not owner of this account.");
			}
			balance += account.getBalance();
		}
		
		return balance;
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public double getIncome(Customer customer, Date from, Date to) throws Exception {
		List<Account> accounts = getAccounts(customer);
		double income = 0;
		for(Account account : accounts) {
			if(!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
				throw new Exception("You are not owner of this account.");
			}
			income += cummulateTransactionsType(account, from, to,
					AccountHistoryUtil.HISTORY_DEPOSIT);
		}
		
		return income;
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public double getNetChange(Customer customer, Date from, Date to) throws Exception {
		List<Account> accounts = getAccounts(customer);
		double netChange = 0;
		for(Account account : accounts) {
			if(!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
				throw new Exception("You are not owner of this account.");
			}
			double income = cummulateTransactionsType(account, from, to, 
					AccountHistoryUtil.HISTORY_DEPOSIT);
			double expenses = cummulateTransactionsType(account, from, to, 
					AccountHistoryUtil.HISTORY_WITHDRAWAL);
			
			netChange += income - expenses;
		}
		
		return netChange;
	}
	
	@SuppressWarnings("unchecked")
	double cummulateTransactionsType(Account account, Date from, Date to, String type) {
		double sum = 0;
		Query q = em.createNamedQuery("FIN_TA.findByAccountAndDescription");
		q.setParameter("account", account);
		q.setParameter("description", type);
		List<FinancialTransaction> transactions = q.getResultList();
		
		for(FinancialTransaction ta : 
			AccountHistoryUtil.filterTransactionsByTimeRange(transactions, from, to)) {
			sum += ta.getAmount();
		}
		return sum;
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void payOutMortgage(MortgageApplication mortgageApplication) throws Exception {
		double sum = mortgageApplication.getRequiredSum() - mortgageApplication.getAvailableFunds();
		deposit(sum);
		em.persist(new Mortgage(mortgageApplication.getCustomer(), sum));
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public void updateCustomer(Customer customer) {
		if(customer != null) {
			em.merge(customer);
		}
	}
}
