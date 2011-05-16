package ch.uzh.ejb.bank;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.security.auth.spi.Util;

import ch.uzh.ejb.bank.Account.Status;

/**
 * Session Bean implementation class BankApplication
 */
@Stateful
@SecurityDomain("bankapplication")
@DeclareRoles({"administrator, user"})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class BankApplication implements BankApplicationRemote, BankApplicationLocal {

	@Resource
	private SessionContext context;
	
	@PersistenceContext(unitName="BankApplication")
	private EntityManager em;
	
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
    public Role createUserRole(String userName, String role) {
    	Role userRole = new Role(userName, role);
    	em.persist(userRole);
    	
    	return userRole;
    }
    
	@Override
//	@RolesAllowed({"administrator"})
	@PermitAll
	public Customer createCustomer(String userName, String password, String firstName, String lastName,
			String address, Customer.Gender gender, String nationality) {
		
		return createCustomer_internal(userName, password, firstName, lastName,
				address, gender, nationality);
	}

	@PermitAll
	Customer createCustomer_internal(String userName, String password,
			String firstName, String lastName, String address,
			Customer.Gender gender, String nationality) {
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
	@PermitAll
	public Account createAccount(double balance,
			Account.Type accountType, float interest, double creditLimit,
			Customer customer) {
		Account account = new Account(balance, accountType, interest, creditLimit, customer);
		em.persist(account);
		
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
		getManagedEntity(account).setAccountStatus(status);
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
		
		return toAccount;
	}

	@Override
	@PermitAll
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
		
		return fromAccount;
	}

	@Override
	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void transfer(Account fromAccount, Account toAccount, double value) {
		if(value < 0.0) {
//			throw new IllegalArgumentException("Can only transfer positive values.");
			context.setRollbackOnly();
			return;
		}
		withdraw(fromAccount, value);
		deposit(toAccount, value);
	}
		
	@Override
	@PermitAll
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void withdrawFailWithRollback(Account account, double value) {
		withdraw(account, value);
		context.setRollbackOnly();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	@PermitAll
	public void clearData() {
		Query query = em.createQuery("SELECT object(a) FROM Account a");
		List<Account> accounts = query.getResultList();
		for(Account account : accounts) {
			em.remove(account);
		}
		query = em.createQuery("SELECT object(c) FROM Customer c");
		List<Customer> customers = query.getResultList();
		for(Customer customer : customers) {
			if(!customer.getUserName().equals("admin")) {
				em.remove(customer);
			}
		}
		query = em.createQuery("SELECT object(r) FROM Role r");
		List<Role> roles = query.getResultList();
		for(Role role : roles) {
			if(!role.getUserName().equals("admin")) {
				em.remove(role);
			}
		}
	}
	
	@Override
	@PermitAll
	public void populateDatabase() {
		createCustomer_internal("admin", "admin", "", "", "", Customer.Gender.OTHER, "");
    	createUserRole("admin", "administrator");
	}
	
	<T> T getManagedEntity(T entity) {
		return em.merge(entity);
	}
}
