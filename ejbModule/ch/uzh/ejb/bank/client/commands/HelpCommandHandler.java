package ch.uzh.ejb.bank.client.commands;

import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.client.CommandLineReader;

public class HelpCommandHandler extends AbstractCommandHandler {

	private CommandLineReader commandLineReader;
	
	public HelpCommandHandler(BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
		if (bankApplicationProvider instanceof CommandLineReader) {
			this.commandLineReader = (CommandLineReader) bankApplicationProvider;
		}
	}
	
	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		if (tokenizer.hasMoreTokens()) {
			String command = tokenizer.nextToken();
			this.commandLineReader.printHelpFor(command);
		} else {
			this.commandLineReader.printHelp();
		}
	}

	@Override
	public String getUsage() {
		return getCommand()+" [command]";
	}

	@Override
	public String getCommand() {
		return "help";
	}

}
