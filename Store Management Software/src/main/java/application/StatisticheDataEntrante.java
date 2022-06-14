package application;

import java.sql.Date;
import java.util.HashMap;

import application.client.Client;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class StatisticheDataEntrante  extends Service<HashMap<Date, Double>>{

	@Override
	protected Task<HashMap<Date, Double>> createTask() {
		return new Task<HashMap<Date, Double>>() {

			@Override
			protected HashMap<Date, Double> call() throws Exception {
				HashMap<Date, Double> data = Client.getInstance().getStatsIncomings();
				return data;
			}
		};
	}

}
