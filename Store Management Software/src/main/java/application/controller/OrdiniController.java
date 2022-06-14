package application.controller;

import java.util.ArrayList;
import com.jfoenix.controls.JFXButton;
import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.common.Logger;
import application.common.Protocol;
import application.model.Address;
import application.model.Order;
import application.model.OrderState;
import application.model.User;
import io.sentry.ITransaction;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class OrdiniController {

	@FXML
	private TableColumn<Order, String> nominativoColumn;

	@FXML
	private TableColumn<Order, String> indirizzoColumn;

	@FXML
	private TableColumn<Order, String> dataOrdineColumn;

	@FXML
	private JFXButton modifica;

	@FXML
	private JFXButton rimuovi;

	@FXML
	private JFXButton refresh;

	@FXML
	private JFXButton visualizza;

	@FXML
	private TableColumn<Order, Double> prezzoTotaleColumn;

	@FXML
	private TableView<Order> ordersView;

	@FXML
	private TableColumn<Order, Integer> numOrdineColumn;

	@FXML
	private TableColumn<Order, Button> statoOrdineColumn;

	private ArrayList<Order> orders = new ArrayList<Order>();

	private User currentUser = Client.getInstance().getUser();

	private boolean allPermissions = currentUser.hasRole(Settings.ALL_PERMISSIONS);

	@FXML
	void initialize() {
		permissionBasedView();
		updateTable();
	}

	private void permissionBasedView() {
		modifica.setVisible(allPermissions);
		rimuovi.setVisible(allPermissions);
		nominativoColumn.setVisible(allPermissions);
	}

	private void updateTable() {
		ITransaction t = Logger.getInstance().startTransaction("OrdiniController.updateTable()",
				"aggiornamento dati tabella");
		
		if (allPermissions)
			orders = Client.getInstance().getAllOrders();
		else
			orders = Client.getInstance().getOrdersOf(currentUser.getIdUtente());
		
		numOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("idOrdine"));
		
		numOrdineColumn.setCellValueFactory(new PropertyValueFactory<>("idOrdine"));

		if (allPermissions) {
			nominativoColumn
					.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
						@Override
						public ObservableValue<String> call(CellDataFeatures<Order, String> param) {
							User user = param.getValue().getUtente();

							String nome = user.getNome();
							String cognome = user.getCognome();
							return new ReadOnlyStringWrapper(nome + " " + cognome);
						}
					});
		}
		indirizzoColumn.setCellValueFactory(new Callback<CellDataFeatures<Order, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<Order, String> param) {
				Address addr = param.getValue().getIndirizzo();

				String completeAddres = addr.toString();
				return new ReadOnlyStringWrapper(completeAddres);
			}
		});
		dataOrdineColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Order, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Order, String> param) {
						String date = param.getValue().getDataOrdineFormattata();
						return new ReadOnlyStringWrapper(date);
					}

				});

		statoOrdineColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Order, Button>, ObservableValue<Button>>() {

					@Override
					public ObservableValue<Button> call(CellDataFeatures<Order, Button> param) {
						OrderState state = param.getValue().getStatoOrdine();
						String stato = state.getNome();
						Button button = new Button();
						button.setStyle("-fx-background-color: " + state.getColore());

						button.setPrefWidth(statoOrdineColumn.getWidth() / 0.5);
						button.setText(stato);
						button.setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								return;
							}
						});
						return new ReadOnlyObjectWrapper<Button>(button);

					}
				});
		prezzoTotaleColumn.setCellValueFactory(new PropertyValueFactory<>("prezzoTotale"));

		ObservableList<Order> order = FXCollections.observableArrayList(orders);
		ordersView.setItems(order);
		ordersView.getSelectionModel().selectFirst();
		Logger.getInstance().closeTransaction(t);
	}

	@FXML
	private void modificaOrdine(MouseEvent event) {
		if ((event.getButton().equals(MouseButton.PRIMARY) || event.getSource().equals(modifica)) && allPermissions) {
			OrdineViewController controller = (SceneHandler.getInstance().setVisualizzaOrdineScene());
			Order currentOrder = ordersView.getSelectionModel().getSelectedItem();
			controller.fillFields(currentOrder, false);
		}
	}

	@FXML
	private void rimuoviOrdine(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY) && allPermissions)
			return;
		Order currentOrder = ordersView.getSelectionModel().getSelectedItem();
		if (currentOrder == null) {
			SceneHandler.getInstance().showInfo("Ordine non selezionato", "Devi prima selezionare un ordine.");
			return;
		}

		if (currentOrder.getStatoOrdine().getNome().equalsIgnoreCase("Completato")) {
			SceneHandler.getInstance().showInfo("Ordine completato",
					"Non puoi rimuovere un ordine con stato completato!");
			return;
		}

		int idOrdine = currentOrder.getIdOrdine();
		boolean confirm = SceneHandler.getInstance().showConfirm("Rimuovo ordine?",
				"Sei sicuro di voler cancellare l'ordine #" + idOrdine + "?");
		if (confirm) {
			String res = Client.getInstance().removeOrder(idOrdine);
			if (res.equals(Protocol.OK)) {
				
				orders.remove(currentOrder);
				//ordersView.getItems().remove(ordersView.getSelectionModel().getSelectedIndex());
				
				String titoloAlert = "Ordine cancellato";
				String messaggioAlert = "Ordine #" + idOrdine + " cancellato con successo!";
				updateTable();
				SceneHandler.getInstance().showInfo(titoloAlert, messaggioAlert);
			} else
				SceneHandler.getInstance().showError(res);
		}
	}

	@FXML
	private void refreshAction(MouseEvent event) {
		updateTable();
	}

	@FXML
	private void visualizzaOrdine(MouseEvent event) {
		if (ordersView.getItems().isEmpty())
			return;

		if ((event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
				|| event.getSource().equals(visualizza)) {

			OrdineViewController controller = (SceneHandler.getInstance().setVisualizzaOrdineScene());
			Order currentOrder = ordersView.getSelectionModel().getSelectedItem();

			if (currentOrder == null) {
				boolean clicked = SceneHandler.getInstance().showInfo("Ordine non selezionato",
						"Devi prima selezionare un ordine.");
				if (clicked)
					return;
			} else
				controller.fillFields(currentOrder, true);
		}

	}
}
