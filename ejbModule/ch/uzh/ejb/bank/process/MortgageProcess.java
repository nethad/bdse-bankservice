package ch.uzh.ejb.bank.process;

import ch.uzh.ejb.bank.entities.Customer;

public abstract class MortgageProcess {

	protected Customer customer = null;
	protected MortgageApplication application = null;
	protected MortgageApplication mortgage = null;
	
	protected Thread computeRequiredFundsThread = new Thread(new Runnable() {
		@Override
		public void run() {
			computeRequiredFunds();
		}
	});
	protected Thread computeAvailableFundsThread = new Thread(new Runnable() {
		@Override
		public void run() {
			computeAvailableFunds();
		}
	});
	
	/**
	 * Retrieve customer information.
	 */
	protected abstract void collectCustomerInformation();
	
	/**
	 * Contact the customer in order to fill in missing customer information.
	 */
	protected abstract void contactCustomer();
	
	/**
	 * Compute the funds required for the mortgage.
	 */
	protected abstract void computeRequiredFunds();
	
	/**
	 * Compute the funds the customer possess.
	 */
	protected abstract void computeAvailableFunds();
	
	/**
	 * Approve the mortgage application.
	 */
	protected abstract void aproovalProcess();
	
	/**
	 * Send the mortgage application to the customer for him to sign it.
	 */
	protected abstract void sendDocuments();
	
	/**
	 * Inform the customer that the mortgage application has been rejected.
	 */
	protected abstract void sendRejectionLetter();
	
	/**
	 * Pay out the mortgage to the customer.
	 */
	protected abstract void payOut();
	
	/**
	 * Inform the customer of the cancellation of the mortgage application if he did not sign it.
	 */
	protected abstract void contactCustomerIfNotSigned();
	
	/**
	 * Close the mortgage application.
	 */
	protected abstract void closeApplication();
	
	/**
	 * Execute the mortgage process.
	 */
	public final void execute() {
		collectCustomerInformation();
		while(!isCustomerComplete()) {
			contactCustomer();
			collectCustomerInformation();
		} 
		computeRequiredFundsThread.start();
		computeAvailableFundsThread.start();
		
		try {
			computeRequiredFundsThread.join();
			computeAvailableFundsThread.join();
		} catch (InterruptedException e) {
			System.err.println("Thread interrupted. Horrible! Horrible! Help!");
		}
		
		aproovalProcess();
		if(application != null && application.isApproved()) {
			sendDocuments();
			if(application.isSigned()) {
				payOut();
				return;
			} else {
				contactCustomerIfNotSigned();
			}
		} else {
			sendRejectionLetter();
		}
		closeApplication();
	}
	
	/**
	 * Verifies that the customer information is complete.
	 * 
	 * @param customer
	 * @return
	 */
	protected abstract boolean isCustomerComplete(); 
}
