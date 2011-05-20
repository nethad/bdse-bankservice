package ch.uzh.ejb.bank.impl.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.uzh.ejb.bank.entities.FinancialTransaction;

public class AccountHistoryUtil {
	public static final String HISTORY_HEADER = "History: ";
	public static final String SEPERATOR = "------------------------------";
	public static final String HISTORY_WITHDRAWAL = "Withdrawal: ";
	public static final String HISTORY_DEPOSIT = "Deposit: ";
	public static final String HISTORY_OPENED = "Account opened";
	public static final String HISTORY_CLOSED = "Account closed";
	public static final String HISTORY_CREATED = "Account created";
	
	/**
	 * Filters the transactions that are between from and to (both including) and
	 * returns a new List with them.
	 */
	public static List<FinancialTransaction> filterTransactionsByTimeRange(
			List<FinancialTransaction> transactions, Date from, Date to) {
	
		List<FinancialTransaction> results = new ArrayList<FinancialTransaction>(transactions.size());
		
		if(transactions.size() > 0) {
			for(FinancialTransaction transaction : transactions) {
				if(transaction.getDate().compareTo(from) >= 0 && 
						transaction.getDate().compareTo(from) <= 0) {
					
					results.add(transaction);
				}
			}
		}
		
		return transactions;
	}
}
