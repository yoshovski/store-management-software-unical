package application.controller;

import com.jfoenix.controls.JFXButton;
import application.SceneHandler;
import application.Settings;
import application.Settings.THEME;
import application.client.Client;
import application.model.User;
import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class SliderMenuController {

	@FXML
	private AnchorPane dashboard;

	@FXML
	private JFXButton buttonDashboard;

	@FXML
	private AnchorPane prodotti;

	@FXML
	private JFXButton buttonProdotti;

	@FXML
	private AnchorPane carrello;

	@FXML
	private JFXButton buttonCarrello;

	@FXML
	private AnchorPane magazzino;

	@FXML
	private JFXButton buttonMagazzino;

	@FXML
	private AnchorPane categorie;

	@FXML
	private JFXButton buttonCategorie;

	@FXML
	private AnchorPane ordini;

	@FXML
	private JFXButton buttonOrdini;

	@FXML
	private AnchorPane statistiche;

	@FXML
	private JFXButton buttonStatistiche;

	@FXML
	private AnchorPane utenti;

	@FXML
	private JFXButton buttonUtenti;

	@FXML
	private AnchorPane impostazioni;

	@FXML
	private JFXButton buttonImpostazioni;

	@FXML
	private AnchorPane logout;

	@FXML
	private JFXButton buttonLogout;

	@FXML
	private VBox menu;

	private User currentUser = Client.getInstance().getUser();

	private boolean allPermissions = currentUser.hasRole(Settings.ALL_PERMISSIONS);

	@FXML
	void initialize() {
		permissionBasedView();
	}

	private void permissionBasedView() {
		if (!allPermissions) {
			menu.getChildren().remove(categorie);
			menu.getChildren().remove(magazzino);
			menu.getChildren().remove(utenti);
			menu.getChildren().remove(statistiche);
		}
		else {
			menu.getChildren().remove(carrello);
			menu.getChildren().remove(prodotti);
		}
	}

	@FXML
	void dashboardAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		SceneHandler.getInstance().setDashboardScene(allPermissions);
	}

	@FXML
	void prodottiAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setProdottiScene();
	}

	@FXML
	void carrelloAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setCarrelloScene();
	}

	@FXML
	void magazzinoAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setMagazzinoScene();
	}

	@FXML
	void categorieAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setCategorieScene();
	}

	@FXML
	void ordiniAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setOrdiniScene();
	}

	@FXML
	void statisticheAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setStatisticheScene();
	}

	@FXML
	void utentiAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setUtentiScene();
	}

	@FXML
	void impostazioniAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setImpostazioniScene();
	}

	@FXML
	void logoutAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		boolean confirm = SceneHandler.getInstance().showConfirm("Logout", "Sei sicuro di voler uscire?");

		if (confirm && Client.getInstance().logout()) {
			SceneHandler.getInstance().changeThemeUI(THEME.LIGHT);
			SceneHandler.getInstance().setLogoutScene();
		}
	}

}
