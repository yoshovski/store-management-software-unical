package application;

import java.util.HashMap;
import application.client.Client;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class StatisticheDataProdottiVenduti extends Service<HashMap<String, Integer>> {

	@Override
	protected Task<HashMap<String, Integer>> createTask() {
		return new Task<HashMap<String, Integer>>() {

			@Override
			protected HashMap<String, Integer> call() throws Exception {
				HashMap<String, Integer> bestSellers = Client.getInstance().getStatsBestSellers();
				return bestSellers;
			}
		};
	}

}
