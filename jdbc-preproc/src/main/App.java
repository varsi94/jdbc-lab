package main;

import helpers.IDalWrapper;
import helpers.SolutionInitiator;

public class App {
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		IDalWrapper wrapper = SolutionInitiator.getWrapper(args[0]);
		wrapper.connect("h_varsanyim", "Marci94");
		
		String[] params = args[1].split("\\|\\|\\|");
		if (params[0].equals("search")) {
			String keyword = (params.length > 1) ? wrapper.search(params[1]) : "";
			System.out.println(wrapper.search(keyword));
		} else if (params[0].equals("getStatistics")) {
			System.out.println(wrapper.getStatistics());
		}
	}
}