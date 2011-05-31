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

import ch.uzh.ejb.bank.BankApplicationRemote;
import ch.uzh.ejb.bank.entities.Account;
import ch.uzh.ejb.bank.entities.Customer;
import ch.uzh.ejb.bank.entities.FinancialTransaction;
import ch.uzh.ejb.bank.entities.Account.Status;
import ch.uzh.ejb.bank.entities.Mortgage;
import ch.uzh.ejb.bank.entities.Portfolio;
import ch.uzh.ejb.bank.entities.Role;
import ch.uzh.ejb.bank.entities.Share;
import ch.uzh.ejb.bank.impl.utils.AccountHistoryUtil;
import ch.uzh.ejb.bank.process.MortgageApplication;

/**
 * Stateful Session Bean implementation class for BankApplication
 */
@Stateful
@SecurityDomain("bankapplication")
@DeclareRoles({"administrator, clerk, user"})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class BankApplication implements BankApplicationRemote {

	static final String ADMINISTRATOR_ROLE = "administrator";
	static final String CLERK_ROLE = "clerk";
	static final String USER_ROLE = "user";

	@Resource
	SessionContext context;
	
	@PersistenceContext(unitName="BankApplication")
	EntityManager em;

	Account selectedAccount;
	Customer selectedCustomer;
	
    /**
     * Default constructor. 
     */
    public BankApplication() {}

    @Override
	@RolesAllowed({ADMINISTRATOR_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void addShare(String symbol, long quantity, double purchasePrice) throws Exception {
		checkIfCustomerIsSelected();
		if(symbol.trim().isEmpty()) {
			throw new Exception("Invalid symbol.");
		}
		if(quantity < 0) {
			throw new Exception("Invalid quatity.");
		}
		if(purchasePrice < 0.0) {
			throw new Exception("Invalid purchasePrice.");
		}
		Portfolio portfolio = getCustomerPortfolio();
		if(portfolio != null && portfolio.getCustomer().equals(selectedCustomer)) {
			Query q = em.createNamedQuery("Share.findBySymbolAndPortfolio");
			q.setParameter("symbol", symbol);
			q.setParameter("portfolio", portfolio);
			Share share = null;
			if(q.getResultList().size() > 0) {
				share = (Share) q.getSingleResult();
				long oldQ = share.getQuantity();
				share.setQuantity(oldQ + quantity);
				share.setAveragePurchasePrice((oldQ * share.getAveragePurchasePrice() + 
						quantity*purchasePrice)/(oldQ + quantity));
				return;
			}
			share = new Share(symbol, quantity, purchasePrice);
			share.setPortfolio(portfolio);
			em.persist(share);
		} else {
			throw new Exception("Customer does not have a valid portfolio.");
		}
	}
    
    void checkIfAccountIsOpen(Account account) throws Exception {
		if (account.getAccountStatus() != Status.OPEN) {
			throw new Exception("Account is closed.");
		}
	}
    
    void checkIfAccountIsSelected() throws Exception {
		if (this.selectedAccount == null) {
			throw new Exception("No account object given");
		}
	}
    
    void checkIfCustomerIsSelected() throws Exception {
		if (this.selectedCustomer == null) {
			throw new Exception("No customer object given");
		}
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
		deposit_intern(account, balance);
		
		return account;
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Customer createCustomer(String userName, String password, String firstName, String lastName,
			String address, Customer.Gender gender, String nationality) throws Exception {
		
		password = Util.createPasswordHash("MD5", Util.BASE64_ENCODING, null, null, password);
		
		Customer customer = new Customer(userName, password, firstName, lastName, address, gender, nationality);
		em.persist(customer);
		Role role = new Role(userName, "user");
		em.persist(role);
		
		createPortfolio(customer);
		
		return customer;
	}

	void createPortfolio(Customer customer) throws Exception {
		Portfolio portfolio = new Portfolio(customer);
		em.persist(portfolio);
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
	public Account deposit(double value) throws Exception {
		checkIfAccountIsSelected();
		return deposit_intern(selectedAccount, value);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	Account deposit_intern(Account toAccount, double value) throws Exception {
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
	
	@PostConstruct
    public void ejbCreate() {
    	System.out.println("BankApplication.ejbCreate()");
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
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public String getAccountHistory(Date from, Date to) throws Exception {
		checkIfAccountIsSelected();
		return getAccountHistory_intern(this.selectedAccount, from, to);
	}

	@SuppressWarnings("unchecked")
	String getAccountHistory_intern(Account account, Date from, Date to) throws Exception {
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
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public Customer getCustomer(long id) {
		Customer customer = null;
		if(id >= 0) {
			Query q = em.createNamedQuery("Customer.findById");
			q.setParameter("id", id);
			try {
				customer = (Customer) q.getSingleResult();
				customer.getAccounts();
			} catch(Exception ex) {
				customer = null;
			}
		}
		return customer;
	}
	
	@Override
	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	Customer getCustomerByUsername(String userName) {
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
	@RolesAllowed({ADMINISTRATOR_ROLE})
	public Portfolio getCustomerPortfolio() throws Exception {
		checkIfCustomerIsSelected();
		Query q = em.createNamedQuery("Portfolio.findByCustomer");
		q.setParameter("customer", selectedCustomer);
		Portfolio portfolio = (Portfolio) q.getSingleResult();
		portfolio.setCustomer(selectedCustomer);
		return portfolio;
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public double getIncome(Date from, Date to) throws Exception {
		List<Account> accounts = getCustomer(selectedCustomer.getCustomerId()).getAccounts();
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
	
	<T> T getManagedEntity(T entity) {
		return em.merge(entity);
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public double getNetChange(Date from, Date to) throws Exception {
		List<Account> accounts = getCustomer(selectedCustomer.getCustomerId()).getAccounts();
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

	@Override
	public long getSelectedAccountId() throws Exception {
		if (this.selectedAccount == null) {
			throw new Exception("No account selected.");
		}
		return this.selectedAccount.getAccountId();
	}
	
	@Override
	public long getSelectedCustomerId() throws Exception {
		checkIfCustomerIsSelected();
		return this.selectedCustomer.getCustomerId();
	}
	
	@Override
	@PermitAll
	public List<Account> getSelectedUserAccounts() throws Exception {
		checkIfCustomerIsSelected();
		return getCustomer(selectedCustomer.getCustomerId()).getAccounts();
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public double getTotalBalance() throws Exception {
		List<Account> accounts = getCustomer(selectedCustomer.getCustomerId()).getAccounts();
		double balance = 0;
		for(Account account : accounts) {
			if(!isLoggedInUserAccountOwnerOrClerkOrAdmin(account)) {
				throw new Exception("You are not owner of this account.");
			}
			balance += account.getBalance();
		}
		
		return balance;
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
	
	boolean loggedInUserIsAdmin() {
		return loggedInUserName().equals("admin");
	}

	String loggedInUserName() {
		return context.getCallerPrincipal().getName();
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void payOutMortgage(MortgageApplication mortgageApplication) throws Exception {
		double sum = mortgageApplication.getRequiredSum() - mortgageApplication.getAvailableFunds();
		deposit(sum);
		em.persist(new Mortgage(mortgageApplication.getCustomer(), sum));
	}
	
	@Remove
    @Override
    public void remove() {
    	System.out.println("BankApplication.remove()");
    }

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void removeShare(String symbol, long quantity) throws Exception {
		checkIfCustomerIsSelected();
		if(symbol.trim().isEmpty()) {
			throw new Exception("Invalid symbol.");
		}
		if(quantity < 0) {
			throw new Exception("Invalid quatity.");
		}
		Portfolio portfolio = getCustomerPortfolio();
		if(portfolio != null && portfolio.getCustomer().equals(selectedCustomer)) {
			Query q = em.createNamedQuery("Share.findBySymbolAndPortfolio");
			q.setParameter("symbol", symbol);
			q.setParameter("portfolio", portfolio);
			if(q.getResultList().size() > 0) {
				Share share = (Share) q.getSingleResult();
				if(share.getQuantity() < quantity) {
					throw new Exception("Customer does not have enough shares.");
				}
				share.setQuantity(share.getQuantity() - quantity);
				if(share.getQuantity() == 0) {
					em.remove(share);
				}
			} else {
				throw new Exception("Customer does not have such shares.");
			}
		} else {
			throw new Exception("Customer does not have a valid portfolio.");
		}
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
    @PermitAll
    public void selectLoggedInUser() {
    	String userName = context.getCallerPrincipal().getName();
    	this.selectedCustomer = getCustomerByUsername(userName);
    }

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void setAccountStatus(Status status) throws Exception {
		if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(selectedAccount)) {
			throw new Exception("You are not owner of this account.");
		}
		
		FinancialTransaction fta = null;
		switch(status) {
		case CLOSED: {
			fta = new FinancialTransaction(selectedAccount, new Date(), 0.0, 
					AccountHistoryUtil.HISTORY_CLOSED);
			break;
		}
		case OPEN: {
			fta = new FinancialTransaction(selectedAccount, new Date(), 0.0, 
					AccountHistoryUtil.HISTORY_OPENED);
			break;
		}
		default: {
			throw new Exception("Unknown status: " + status);
		}
		}
		getManagedEntity(selectedAccount).setAccountStatus(status);
		em.persist(fta);
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE, USER_ROLE})
	public void transfer(long targetAccountId, double value) throws Exception {
		checkIfAccountIsSelected();
		Account toAccount = getAccount(targetAccountId);
		if (toAccount == null) {
			throw new Exception("Target account does not exist.");
		}
		transfer_intern(selectedAccount, toAccount, value);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	void transfer_intern(Account fromAccount, Account toAccount, double value) throws Exception {
		if(value < 0.0) {
			context.setRollbackOnly();
			throw new Exception("Can only transfer positive values.");
		} else if (!isLoggedInUserAccountOwnerOrClerkOrAdmin(fromAccount)) {
			throw new Exception("You are not owner of this account.");
		}
		withdraw_intern(fromAccount, value);
		deposit_intern(toAccount, value);
	}
	
	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE})
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void transferShare(long buyerAccountId, String symbol,
			long quantity, double price) throws Exception {
		
		checkIfCustomerIsSelected();
		checkIfAccountIsSelected();
		Customer seller = selectedCustomer;
		if(!selectedAccount.getCustomer().equals(seller)) {
			throw new Exception("Selected account does not belong to the selected customer.");
		}
		
		Account buyerAccount = getAccount(buyerAccountId);
		if (buyerAccount == null) {
			throw new Exception("Target account does not exist.");
		}
		Customer buyer = buyerAccount.getCustomer();
		
		removeShare(symbol, quantity);
		selectCustomer(buyer.getCustomerId());
		addShare(symbol, quantity, price);
		transfer_intern(buyerAccount, selectedAccount, price);
		selectCustomer(seller.getCustomerId());
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public void updateCustomer(Customer customer) {
		if(customer != null) {
			em.merge(customer);
		}
	}

	@Override
	@RolesAllowed({ADMINISTRATOR_ROLE, CLERK_ROLE})
	public Account withdraw(double value) throws Exception {
		checkIfAccountIsSelected();
		return withdraw_intern(selectedAccount, value);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	Account withdraw_intern(Account fromAccount, double value) throws Exception {
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
}
