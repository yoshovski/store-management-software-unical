package application.controller;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;

import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.client.FilePicker;
import application.common.Logger;
import application.model.DashboardData;
import application.model.OrderState;
import application.model.User;
import io.sentry.ITransaction;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DashboardController extends AnimationTimer {

	@FXML
	private Label nomeUtente;

	@FXML
	private Label cognomeUtente;

	@FXML
	private Label ruoloUtente;

	@FXML
	private Label numProdotti;

	@FXML
	private Label numCategorie;

	@FXML
	private Label prodottiEsaurimento;

	@FXML
	private Label prodottiEsauriti;

	@FXML
	private Label ordiniLavorazione;

	@FXML
	private Label ordiniSospeso;

	@FXML
	private Label utentiOnline;

	@FXML
	private Label tuttiClienti;

	@FXML
	private Label venditeNette;

	@FXML
	private Label numVendite;

	@FXML
	private ImageView imageContainer;

	@FXML
	private VBox schede;

	@FXML
	private GridPane schedeDatiCliente;

	@FXML
	private Label numProdottiVisibili;

	@FXML
	private Label prodottiEsaurimentoClient;

	@FXML
	private Label ordiniLavorazioneClient;

	@FXML
	private Label ordiniCompletatiClient;

	@FXML
	private Label spese;

	@FXML
	private Label ordiniEffettuatiClient;

	private Month currentMonth;
	private int currentYear;
	private User currentUser;

	private long previousTime = 0;

	@FXML
	void initialize() {
		FilePicker filePicker = new FilePicker();
		imageContainer.setImage(filePicker.getImage(Settings.DEFAULT_DASHBOARD_BG_PHOTO_PATH));
		currentMonth = LocalDate.now().getMonth(); // mese corrente
		currentYear = LocalDate.now().getYear();
		currentUser = Client.getInstance().getUser();
		nomeUtente.setText(currentUser.getNome().toUpperCase());
		cognomeUtente.setText(currentUser.getCognome().toUpperCase());
		ruoloUtente.setText(currentUser.getRuolo());
		updateData();
	}

	/**
	 * Aggiorna i dati numerici mostrati da Label
	 */
	private void updateData() {
		ITransaction t = Logger.getInstance().startTransaction("DashboardController.updateData()",
				"aggiorno i dati numerici");
		if (currentUser.hasRole("Cliente")) {
			int idUtente = currentUser.getIdUtente();
			DashboardData data = Client.getInstance().getDashboardDataCliente(idUtente, currentMonth, currentYear);

			refreshContainer(spese, data.getSpent());
			refreshContainer(ordiniEffettuatiClient, data.getNumOrders());
			refreshContainer(numProdottiVisibili, data.getNumProductsAll());
			refreshContainer(prodottiEsaurimentoClient, data.getProductsEsaurimento());
			refreshContainer(ordiniLavorazioneClient, data.getNumOrderLavorazione());
			refreshContainer(ordiniCompletatiClient, data.getNumOrderCompletati());
		} else {
			DashboardData data = Client.getInstance().getDashboardData(currentMonth, currentYear);

			refreshContainer(venditeNette, data.getEarnings());
			refreshContainer(numVendite, data.getNumCompletedOrders());
			refreshContainer(numProdotti, data.getNumProductsAll());
			refreshContainer(numCategorie, data.getNumCategories());
			refreshContainer(prodottiEsaurimento, data.getProductsEsaurimento());
			refreshContainer(prodottiEsauriti, data.getProductsEsauriti());
			refreshContainer(utentiOnline, data.getUsersOnline());
			refreshContainer(tuttiClienti, data.getAllClients());
			refreshContainer(ordiniLavorazione, data.getNumOrdersInLavorazione());
			refreshContainer(ordiniSospeso, data.getNumOrdersInSospeso());
		}
		Logger.getInstance().closeTransaction(t);
	}

	/**
	 * Aggiorna il contenitore con il dato
	 * 
	 * @param container - Contine il dato da mostrare attraverso una Label
	 * @param number    - il dato che dev'essere mostrato dal contenitore
	 */
	private void refreshContainer(Label container, double number) {
		if ((number >= 0.0)) {
			if ((number % 1) == 0) { // se il numero è intero
				int num = (int) number; // converti in intero
				container.setText(String.valueOf(num));
			} else
				container.setText(String.valueOf(number));
		}
	}

	@FXML
	void goToPreferenzeApp(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setImpostazioniScene();
	}

	@FXML
	void sendEmailAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setHelpScene();
	}

	@Override
	public void handle(long now) {
		if ((now - previousTime) / Settings.CONVERT_NANO_TO_SECONDS >= Settings.FREQUENCY) {
			updateData();
			previousTime = now;
		}
	}
}