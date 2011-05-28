package ch.uzh.ejb.bank.client.commands;

public class PrintHelper {
	
	public static void printElementsWithTab(Object... elements) {
		StringBuilder sb = new StringBuilder();
		for (Object element : elements) {
			sb.append(element+"\t");
		}
		System.out.println(sb.toString().trim());
	}

}
