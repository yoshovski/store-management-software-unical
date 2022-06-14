package application.controller;

import java.util.ArrayList;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.common.Logger;
import application.common.Protocol;
import application.model.Address;
import application.model.Order;
import application.model.OrderProduct;
import application.model.OrderState;
import application.model.User;
import io.sentry.ITransaction;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

public class OrdineViewController {

	@FXML
	private TableColumn<OrderProduct, String> categoriaColumn;

	@FXML
	private Text titolo;

	@FXML
	private JFXButton chiudiButton;

	@FXML
	private JFXTextField cognome;

	@FXML
	private JFXTextField city;

	@FXML
	private JFXTextField indirizzo;

	@FXML
	private JFXComboBox<OrderState> statoOrdine;

	@FXML
	private JFXTextField nome;

	@FXML
	private JFXTextField provincia;

	@FXML
	private TableView<OrderProduct> prodottiView;

	@FXML
	private JFXTextField dataOrdine;

	@FXML
	private TableColumn<OrderProduct, String> prezzoColumn;

	@FXML
	private TableColumn<OrderProduct, String> descrizioneColumn;

	@FXML
	private TableColumn<OrderProduct, String> quantityColumn;

	@FXML
	private TableColumn<OrderProduct, String> prezzoTotaleColumn;

	@FXML
	private JFXTextField cap;

	@FXML
	private TableColumn<OrderProduct, ImageView> fotoColumn;

	@FXML
	private JFXTextField nazione;

	@FXML
	private JFXTextArea descrizioneOrdine;

	@FXML
	private JFXButton actionButton;

	@FXML
	private JFXTextField telefono;

	@FXML
	private TableColumn<OrderProduct, String> nomeColumn;

	@FXML
	private JFXTextField email;

	@FXML
	private JFXTextField numCivico;

	@FXML
	private HBox buttonsContainer;

	private ArrayList<OrderProduct> prodottiOrdine;
	private int orderId;
	private Order currentOrder;
	private Address originalIndirizzo;

	private User currentUser = Client.getInstance().getUser();
	private boolean allPermissions = currentUser.hasRole(Settings.ALL_PERMISSIONS);

	@FXML
	void initialize() {
		ArrayList<OrderState> orderState = Client.getInstance().getAllOrderState();
		statoOrdine.setItems(FXCollections.observableList(orderState));
		statoOrdine.getSelectionModel().selectFirst(); // seleziona il primo elemento

		fotoColumn.setVisible(false);
		dataOrdine.setDisable(true);
		nome.setDisable(true);
		cognome.setDisable(true);
		email.setDisable(true);
		telefono.setDisable(true);

		permissionBasedView();
	}

	private void permissionBasedView() {
		statoOrdine.setDisable(!allPermissions);
		descrizioneOrdine.setDisable(!allPermissions);

		if (!allPermissions)
			buttonsContainer.getChildren().remove(actionButton);
	}

	private void updateTable() {
		ITransaction t = Logger.getInstance().startTransaction("OrdineViewController.updateTable()",
				"aggiorno la tabella in Ordine View");
		prodottiOrdine = Client.getInstance().getOrderProducts(orderId);

		for (OrderProduct o : prodottiOrdine)
			prodottiView.getItems().add(o);

		prezzoTotaleColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<OrderProduct, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<OrderProduct, String> param) {
						Double prezzoSingolo = param.getValue().getProdotto().getPrezzo();
						int quantity = param.getValue().getQuantita();
						Double prezzoTotaleProdotto = prezzoSingolo * quantity;
						return new ReadOnlyStringWrapper(Double.toString(prezzoTotaleProdotto));
					}
				});

		prezzoColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<OrderProduct, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<OrderProduct, String> param) {
						Double prezzoSingolo = param.getValue().getProdotto().getPrezzo();
						return new ReadOnlyStringWrapper(Double.toString(prezzoSingolo));
					}
				});

		nomeColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<OrderProduct, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<OrderProduct, String> param) {
						String nome = param.getValue().getProdotto().getNome();
						return new ReadOnlyStringWrapper(nome);
					}
				});

		descrizioneColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<OrderProduct, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<OrderProduct, String> param) {
						String descrizione = param.getValue().getProdotto().getDescrizione();
						return new ReadOnlyStringWrapper(descrizione);
					}
				});

		categoriaColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<OrderProduct, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<OrderProduct, String> param) {
						String categoria = param.getValue().getProdotto().getNomeCategoria();
						return new ReadOnlyStringWrapper(categoria);
					}
				});

		quantityColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<OrderProduct, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<OrderProduct, String> param) {
						int quantity = param.getValue().getQuantita();
						return new ReadOnlyStringWrapper(Integer.toString(quantity));
					}
				});

		prodottiView.getSelectionModel().selectFirst();
		statoOrdine.setValue(currentOrder.getStatoOrdine());
		dataOrdine.setText(currentOrder.getDataOrdineFormattata());
		descrizioneOrdine.setText(currentOrder.getDescrizioneStatoOrdine());

		Logger.getInstance().closeTransaction(t);
	}

	public void fillFields(Order order, boolean viewMode) {
		currentOrder = order;

		/**
		 * se non ha tutti i permessi, l'ordine sarà relativo all'utente corrente Si
		 * riduce il carico, in quanto i dati utente per l'ordine non vengono prelevati
		 * dal DB
		 */
		if (!allPermissions)
			currentOrder.setUser(currentUser);

		orderId = order.getIdOrdine();

		indirizzo.setDisable(viewMode);
		city.setDisable(viewMode);
		cap.setDisable(viewMode);
		nazione.setDisable(viewMode);
		provincia.setDisable(viewMode);
		numCivico.setDisable(viewMode);
		titolo.setText("Dettagli Ordine #" + currentOrder.getIdOrdine());

		User cliente = currentOrder.getUtente();

		String nome = cliente.getNome();
		String cognome = cliente.getCognome();
		String email = cliente.getEmail();
		String telefono = cliente.getTelefono();

		originalIndirizzo = currentOrder.getIndirizzo();
		String nomeVia = originalIndirizzo.getNome_via();
		String citta = originalIndirizzo.getCitta();
		String CAP = originalIndirizzo.getCAP();
		String nazione = originalIndirizzo.getNazione();
		String provincia = originalIndirizzo.getRegione();
		int numCivico = originalIndirizzo.getNum_civico();

		this.nome.setText(nome);
		this.cognome.setText(cognome);
		this.email.setText(email);
		this.telefono.setText(telefono);
		this.indirizzo.setText(nomeVia);
		this.city.setText(citta);
		this.cap.setText(CAP);
		this.nazione.setText(nazione);
		this.provincia.setText(provincia);
		this.numCivico.setText(String.valueOf(numCivico));

		updateTable();
	}

	@FXML
	private void chiudiAction(ActionEvent event) {
		Stage stage = (Stage) chiudiButton.getScene().getWindow();
		stage.close();

	}

	@FXML
	private void action(ActionEvent event) {
		editOrder();
	}

	private void editOrder() {
		String descrizione = descrizioneOrdine.getText();
		currentOrder.setStatoOrdine(statoOrdine.getValue());

		Address updatedAddress = new Address(originalIndirizzo.getIdIndirizzo(), indirizzo.getText(), cap.getText(),
				Integer.parseInt(numCivico.getText()), nazione.getText(), provincia.getText(), city.getText());
		String email = this.email.getText();
		String res = "";
		if (!(updatedAddress.equals(originalIndirizzo))) {
			res = Client.getInstance().editOrder(email, statoOrdine.getValue(), descrizione, currentOrder.getIdOrdine(),
					updatedAddress);
		} else
			res = Client.getInstance().editOrder(email, statoOrdine.getValue(), descrizione, currentOrder.getIdOrdine(),
					null);

		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showError(res);
			return;
		}

		Stage stage = (Stage) actionButton.getScene().getWindow();
		stage.close();

		String titoloAlert = "Ordine Modificato Con Successo";
		String messageAlert = "L'ordine #" + currentOrder.getIdOrdine() + " è stato modificato!";
		SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);

		SceneHandler.getInstance().setOrdiniScene();

	}

}
