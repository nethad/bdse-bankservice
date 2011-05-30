package ch.uzh.ejb.bank.client.commands;

import java.util.List;
import java.util.StringTokenizer;

import ch.uzh.ejb.bank.client.BankApplicationProvider;
import ch.uzh.ejb.bank.entities.Share;

public class GetPortfolioCommandHandler extends AbstractCommandHandler {

	public GetPortfolioCommandHandler(
			BankApplicationProvider bankApplicationProvider) {
		super(bankApplicationProvider);
	}

	@Override
	public void execute(StringTokenizer tokenizer) throws Exception {
		List<Share> shares = getBankApplication().getCustomerPortfolio().getShares();
		printShares(shares);
	}

	private void printShares(List<Share> shares) {
		System.out.println("=== Shares ("+shares.size()+")");
		PrintHelper.printElementsWithTab("id", "symbol", "quantity", "avg purchase price");
		for (Share share : shares) {
			PrintHelper.printElementsWithTab(share.getShareId(),
					share.getSymbol(),
					share.getQuantity(),
					share.getAveragePurchasePrice());
		}
		System.out.println("===");
	}

	@Override
	public String getUsage() {
		return getCommand();
	}

	@Override
	public String getCommand() {
		return "get_portfolio";
	}

}
