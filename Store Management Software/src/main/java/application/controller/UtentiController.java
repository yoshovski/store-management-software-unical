package application.controller;

import java.util.ArrayList;

import com.jfoenix.controls.JFXButton;
import application.SceneHandler;
import application.client.Client;
import application.common.Protocol;
import application.model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class UtentiController {

	@FXML
	private TableView<User> utentiView;

	@FXML
	private TableColumn<User, String> nomeColumn;

	@FXML
	private TableColumn<User, String> emailColumn;

	@FXML
	private TableColumn<User, String> ultimoAccessoColumn;

	@FXML
	private TableColumn<User, Button> ruoloColumn;

	@FXML
	private TableColumn<User, String> cognomeColumn;

	@FXML
	private TableColumn<User, ImageView> avatarColumn;

	@FXML
	private JFXButton refresh;

	@FXML
	private JFXButton aggiungi;

	@FXML
	private JFXButton modifica;

	@FXML
	private JFXButton rimuovi;

	@FXML
	void initialize() {
		updateTable();
		utentiView.getSelectionModel().selectFirst();
	}

	public void updateTable() {
		// specifica le regole di popolamento delle colonne della tabella utentiView
		nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
		cognomeColumn.setCellValueFactory(new PropertyValueFactory<>("cognome"));
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
		ultimoAccessoColumn.setCellValueFactory(new PropertyValueFactory<>("ultimoAccesso"));
		ruoloColumn.setCellValueFactory(new Callback<CellDataFeatures<User, Button>, ObservableValue<Button>>() {

			@Override
			public ObservableValue<Button> call(CellDataFeatures<User, Button> param) {
				String ruolo = param.getValue().getRuolo();

				Button button = new Button();

				if (ruolo.equalsIgnoreCase("Cliente"))
					button.setStyle("-fx-background-color: #7191c7");
				else
					button.setStyle("-fx-background-color: #75a65b");
				// btn.setDisable(true);

				button.setPrefWidth(ruoloColumn.getWidth() / 0.5);
				button.setText(ruolo);
				button.setOnAction(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						return;

					}
				});

				return new ReadOnlyObjectWrapper<Button>(button);

			}

		});

		avatarColumn.setCellValueFactory(new Callback<CellDataFeatures<User, ImageView>, ObservableValue<ImageView>>() {
			@Override
			public ObservableValue<ImageView> call(CellDataFeatures<User, ImageView> userObject) {
				Image img = userObject.getValue().getAvatar();
				ImageView avatarIcon = new ImageView(img);
				avatarIcon.setPreserveRatio(true);
				avatarIcon.setFitHeight(50);
				avatarIcon.setFitWidth(50);
				return new ReadOnlyObjectWrapper<ImageView>(avatarIcon);
			}
		});
		ArrayList<User> userList = Client.getInstance().getAllUsers();
		ObservableList<User> utenti = FXCollections.observableArrayList(userList);
		utentiView.setItems(utenti);
	}

	@FXML
	void refreshAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		utentiView.getItems().clear();
		updateTable();
	}

	@FXML
	void aggiungiUtenteWindow(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setAggiungiNuovoUtenteScene();
	}

	@FXML
	void rimuoviUtente(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		User user = utentiView.getSelectionModel().getSelectedItem();
		if (user == null) {
			SceneHandler.getInstance().showInfo("Utente non selezionato", "Devi prima selezionare un utente.");
			return;
		} else if (user.getEmail().equals(Client.getInstance().getUser().getEmail())) {
			boolean errore = SceneHandler.getInstance().showInfo("Impossibile eliminare utente",
					"Non è possibile eliminare l'utente corrente!");
			if (errore)
				return;
		}

		boolean confirm = SceneHandler.getInstance().showConfirm("Rimuovi " + user.getNome() + " " + user.getCognome(),
				"Sei sicuro di voler cancellare l'utente: " + user.getEmail() + " ?");

		if (confirm) {
			String res = Client.getInstance().removeUser(user);
			if (res.equals(Protocol.OK)) {
				String titoloAlert = "Utente Rimosso Con Successo";
				String messageAlert = "L'utente " + user.getEmail() + " è stato rimosso!";
				updateTable();
				SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
			} else
				SceneHandler.getInstance().showError(res);
		}

	}

	@FXML
	void modificaUtente(MouseEvent event) {
		if (utentiView.getItems().isEmpty())
			return;
		if ((event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
				|| event.getSource().equals(modifica)) {
			UtenteViewController controller = SceneHandler.getInstance().setModificaUtenteScene();
			User user = utentiView.getSelectionModel().getSelectedItem();
			if (user == null) {
				boolean clicked = SceneHandler.getInstance().showInfo("Utente non selezionato",
						"Devi prima selezionare un utente.");
				if (clicked)
					return;
			} else
				controller.modificaView(user);
		}
	}

}
