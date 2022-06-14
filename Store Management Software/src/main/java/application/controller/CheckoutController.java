package application.controller;

import java.util.HashMap;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import application.DataValidation;
import application.SceneHandler;
import application.client.Client;
import application.common.Logger;
import application.common.Protocol;
import application.model.Address;
import application.model.Product;
import application.model.User;
import io.sentry.SentryLevel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CheckoutController {

	@FXML
	private JFXTextField nome;

	@FXML
	private JFXTextField cognome;

	@FXML
	private JFXTextField email;

	@FXML
	private JFXTextField telefono;

	@FXML
	private JFXTextField indirizzo;

	@FXML
	private JFXTextField numCivico;

	@FXML
	private JFXTextField city;

	@FXML
	private JFXTextField cap;

	@FXML
	private JFXTextField provincia;

	@FXML
	private JFXTextField nazione;

	@FXML
	private TableView<Product> prodottiView;

	@FXML
	private HBox buttonsContainer;

	@FXML
	private JFXButton chiudiButton;

	@FXML
	private JFXButton actionButton;

	@FXML
	private Text titolo;

	private User user = Client.getInstance().getUser();

	private HashMap<Product, Integer> prodottiCarrello;

	@FXML
	void initialize() {
		fillFields();
		nome.setDisable(true);
		cognome.setDisable(true);
		email.setDisable(true);
		if (!telefono.getText().isBlank())
			telefono.setDisable(true);
	}

	public void setCarrello(HashMap<Product, Integer> cart) {
		prodottiCarrello = cart;
	}

	public void setTable(TableView<Product> tableProdotti) {
		prodottiView.getColumns().addAll(tableProdotti.getColumns());
		// rimuovo l'ultima colonna che contiene il pulsante rimuovi
		prodottiView.getColumns().remove(tableProdotti.getColumns().size() - 1);
		prodottiView.getItems().addAll(tableProdotti.getItems());

	}

	private boolean blankFields() {
		return indirizzo.getText().isBlank() || numCivico.getText().isBlank() || city.getText().isBlank()
				|| cap.getText().isBlank() || provincia.getText().isBlank() || nazione.getText().isBlank()
				|| telefono.getText().isBlank();
	}

	private void fillFields() {
		nome.setText(user.getNome());
		cognome.setText(user.getCognome());
		email.setText(user.getEmail());
		telefono.setText(user.getTelefono());
	}

	@FXML
	void action(ActionEvent event) {
		if (blankFields()) {
			SceneHandler.getInstance().showInfo("Campi vuoti", "Non possono esserci campi vuoti");
			return;
		}

		if (!telefono.isDisabled() && !DataValidation.isValid(telefono)) {
			SceneHandler.getInstance().showError("Numero di telefono non valido!");
			return;
		}

		if (!DataValidation.isValidNumber(cap, 5, 5, "CAP non valido!")) {
			SceneHandler.getInstance().showError("CAP non valido!");
			return;
		}

		if (prodottiCarrello.isEmpty()) {
			SceneHandler.getInstance().showError("Non hai prodotti nel carrello");
			Logger.getInstance().captureMessage("Carrello vuoto", SentryLevel.WARNING);
			return;
		}

		String indirizzo = this.indirizzo.getText();
		int numCivico = Integer.valueOf(this.numCivico.getText());
		String city = this.city.getText();
		String cap = this.cap.getText();
		String provincia = this.provincia.getText();
		String nazione = this.nazione.getText();

		Address address = new Address(indirizzo, cap, numCivico, nazione, provincia, city);

		String res = Client.getInstance().placeNewOrder(user.getIdUtente(), address, prodottiCarrello);

		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().setOrdiniScene();
			SceneHandler.getInstance().showError(res);
			return;
		}

		Stage stage = (Stage) actionButton.getScene().getWindow();
		stage.close();

		if (user.getTelefono().isBlank()) {
			user.setTelefono(telefono.getText());
			String res2 = Client.getInstance().editUser(user);

			if (!res2.equals(Protocol.OK))
				Logger.getInstance().captureMessage("telefono aggiunto per utente: " + user.getIdUtente());
			else
				Logger.getInstance().captureMessage("Error while updating phone number of user: " + user.getIdUtente(),
						SentryLevel.ERROR);
		}

		SceneHandler.getInstance().setOrdiniScene();

		String titoloAlert = "Ordine Effettuato Con Successo";
		String messageAlert = "L'ordine è stato effettuato con successo!";
		SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);

	}

	@FXML
	private void chiudiAction(ActionEvent event) {
		CarrelloController.setCheckoutScene(false);
		SceneHandler.getInstance().setCarrelloScene();
		Stage stage = (Stage) chiudiButton.getScene().getWindow();
		stage.close();

	}

}
