package application.controller;

import java.time.LocalDate;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TreeMap;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import application.SceneHandler;
import application.StatisticheDataEntrante;
import application.StatisticheDataProdottiVenduti;
import application.common.Logger;
import io.sentry.SentryLevel;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class StatisticheController {
	@FXML
	private VBox vBoxChart;

	@FXML
	private JFXDatePicker dataInizio = new JFXDatePicker();

	@FXML
	private JFXButton caricaButton;

	@FXML
	private JFXDatePicker dataFine = new JFXDatePicker();

	private StatisticheDataEntrante service = new StatisticheDataEntrante();
	private StatisticheDataProdottiVenduti service2 = new StatisticheDataProdottiVenduti();

	private Date fine = null;
	private Date inizio = null;
	private String chartsTitle;

	private String dateToString(Date data) {
		return new SimpleDateFormat("dd-MM-yyyy").format(data);
	}

	@FXML
	void process(ActionEvent event) {
		if (dataInizio.getValue() == null || dataFine.getValue() == null) {
			SceneHandler.getInstance().showError("Date non inserite!");
			vBoxChart.setVisible(false);
			return;
		}
		fine = Date.valueOf(dataFine.getValue());
		inizio = Date.valueOf(dataInizio.getValue());

		if (fine.before(inizio) || inizio.after(fine) || fine.after(Date.valueOf(LocalDate.now()))) {
			SceneHandler.getInstance().showError("Data inizio o data fine non valide!");
			return;
		}
		chartsTitle = " dal " + dateToString(inizio) + " al " + dateToString(fine);
		vBoxChart.setVisible(true);
		vBoxChart.getChildren().clear();
		service.restart();

	}

	private void loadBarChart() {
		service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				CategoryAxis xAxis = new CategoryAxis();
				NumberAxis yAxis = new NumberAxis();
				BarChart<String, Number> barChartEntrance = new BarChart<String, Number>(xAxis, yAxis);
				barChartEntrance.setTitle("Entrate " + chartsTitle);
				xAxis.setLabel("Data");
				yAxis.setLabel("Entrate");
				TreeMap<Date, Double> result = new TreeMap<>((HashMap<Date, Double>) event.getSource().getValue());
				Series<String, Number> entrate = new Series<String, Number>();
				entrate.setName("€");
				System.out.println(result);

				for (Date d : result.keySet())
					if (d.compareTo(inizio) >= 0 && d.compareTo(fine) <= 0) {
						String date = dateToString(d);
						Data<String, Number> data = new Data<String, Number>(date, result.get(d));
						entrate.getData().add(data);
					}
				
				barChartEntrance.setAnimated(true);

				barChartEntrance.getData().add(entrate);
				vBoxChart.getChildren().add(barChartEntrance);
				service2.restart();
			}

		});
		service.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Logger.getInstance().captureMessage("Errore caricamento Statistiche Entrate", SentryLevel.ERROR);
				service2.restart();
			}
		});
	}

	private void loadPieChart() {
		service2.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				TreeMap<String, Integer> pieChartData = new TreeMap<>(
						(HashMap<String, Integer>) event.getSource().getValue());

				PieChart pieChartSell = new PieChart();
				Label caption = new Label("");
				caption.setId("labelGraph");

				for (String s : pieChartData.keySet()) {
					PieChart.Data data = new PieChart.Data(s, pieChartData.get(s));
					pieChartSell.getData().add(data);

					data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							caption.setMouseTransparent(true);
							caption.setTranslateX(e.getX());
							caption.setTranslateY(e.getY() - 40);
							caption.setText(String.valueOf(data.getPieValue()) + "\n" + data.getName());
							caption.setVisible(true);
							caption.toFront();
						}
					});

					data.getNode().addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							caption.setTranslateX(e.getX());
							caption.setTranslateY(e.getY() - 40);
						}
					});

					data.getNode().addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							caption.setVisible(false);
						}
					});
				}

				pieChartSell.setTitle("Prodotti più venduti");
				vBoxChart.getChildren().add(pieChartSell);
				vBoxChart.getChildren().add(caption);
			}
		});

		service2.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				Logger.getInstance().captureMessage("Errore caricamento Statistiche Prodotti più venduti",
						SentryLevel.ERROR);
			}
		});

	}

	@FXML
	void initialize() {
		dataFine.setValue(LocalDate.parse(LocalDate.now().toString()));
		dataInizio.setValue(LocalDate.parse(LocalDate.now().minusMonths(1).toString()));

		Callback<DatePicker, DateCell> callB = new Callback<DatePicker, DateCell>() {
			@Override
			public DateCell call(final DatePicker param) {
				return new DateCell() {
					@Override
					public void updateItem(LocalDate item, boolean empty) {
						super.updateItem(item, empty);
						LocalDate today = LocalDate.now();
						setDisable(empty || item.compareTo(today) > 0);
					}

				};
			}

		};
		dataFine.setDayCellFactory(callB);
		dataInizio.setDayCellFactory(callB);

		loadBarChart();
		loadPieChart();
	}

}
