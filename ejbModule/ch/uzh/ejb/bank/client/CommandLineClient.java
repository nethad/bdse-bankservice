package ch.uzh.ejb.bank.client;

import java.io.IOException;

import javax.naming.NamingException;

public class CommandLineClient {
	
	public static void main(String[] args) {
		System.setProperty("java.security.auth.login.config", "etc/login.config");
		try {
			CommandLineReader commandLineReader = new CommandLineReader();
			commandLineReader.start();
		} catch (IOException e) {
			System.err.println("[ERROR] Could not initialize " +
					"interactive command line client: "+e.getMessage());
		} catch (NamingException e) {
			System.err.println("[ERROR] Web service binding " +
					"not successful: "+e.getMessage());
		}
	}

}
