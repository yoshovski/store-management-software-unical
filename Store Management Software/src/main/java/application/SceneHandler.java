package application;

import application.controller.UtenteViewController;
import application.model.Configurazione;
import application.controller.CarrelloController;
import application.controller.CategoriaViewController;
import application.controller.ChangePasswordController;
import application.controller.ChangeStoreSettingsController;
import application.controller.CheckoutController;
import application.controller.DashboardController;
import application.controller.HelpController;
import application.controller.MagazzinoViewController;
import application.controller.MainFrameController;
import application.controller.OrdineViewController;
import application.controller.ProdottoViewController;

import java.io.IOException;
import java.util.Optional;

import application.Settings.THEME;
import application.client.Client;
import application.common.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SceneHandler {
	private Scene scene;
	private Stage stage;
	private static MainFrameController mainFrameController;
	private static SceneHandler instance = null;
	private Configurazione configurazione;
	private static DashboardController dashboardController;
	private static CarrelloController carrelloController;
	private boolean allPermissions = false;
	private THEME theme = THEME.LIGHT;
	private String tema = Settings.LIGHT_THEME;

	private SceneHandler() {
	}

	/**
	 * Genera un FXMLLoader a partire dal nome di un file di tipo .fxml <br>
	 * Il file deve risiedere obbligatoriamente nella cartella del progetto
	 * <i>"application/view"
	 * 
	 * @param fileName - <i>Nome del file senza l'estensione. Accettati solo di tipo
	 *                 .fxml</i>
	 * @return FXMLLoader
	 */
	public FXMLLoader loadFXML(String fileName) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/view/" + fileName + ".fxml"));
		return loader;
	}

	public void init(Stage stage) {
		try {
			this.stage = stage;
			FXMLLoader loader = loadFXML(Settings.LOGINREGISTER_FXML);
			AnchorPane root = (AnchorPane) loader.load();
			scene = new Scene(root);
			stage.getIcons().add(new Image(getClass().getResourceAsStream(Settings.DEFAULT_DASHBOARD_BG_PHOTO_PATH)));
			// scene.getStylesheets().add(getClass().getResource("/application/css/LoginRegister.css").toExternalForm());
			// scene.getStylesheets().add(getClass().getResource("/application/css/"+"LightTheme"+".css").toExternalForm());
			stage.setScene(scene);
			changeThemeUI(scene);
			setLoginScene();
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return;
		}
	}

	public void setLoginScene() {
		setSceneTitle("Login");
		stage.setHeight(Settings.LOGIN_HEIGHT);
		stage.setWidth(Settings.LOGIN_WIDTH);
		stage.setResizable(false);
		stage.setMaximized(false);
		stage.centerOnScreen();
		stage.show();
	}

	public void setLogoutScene() {
		if (carrelloController != null)
			carrelloController.stop();
		
		if (dashboardController != null)
			dashboardController.stop();
		
		stage.hide();
		stage.setHeight(Settings.LOGIN_HEIGHT);
		stage.setWidth(Settings.LOGIN_WIDTH);
		init(stage);
	}

	public void setRegisterScene() {
		setSceneTitle("Register");
		stage.setResizable(false);
		stage.show();
	}

	public static SceneHandler getInstance() {
		if (instance == null)
			instance = new SceneHandler();
		return instance;
	}

	/**
	 * Mostra un Alert di Errore
	 * 
	 * @param message
	 */
	public void showError(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("");
		alert.setContentText(message);
		alert.show();
	}

	/**
	 * Mostra un Alert di Informazione con un pulsante (OK)
	 * 
	 * @param title
	 * @param message
	 * @return <b>true</b> - se hai premuto "ok" <br>
	 *         <b>false</b> - altrimenti
	 */
	public boolean showInfo(String title, String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(message);
		alert.showAndWait();
		ButtonType risposta = alert.getResult();
		if (risposta.equals(ButtonType.OK))
			return true;
		return false;
	}

	/**
	 * Mostra un Alert di conferma con due pulsanti. ( Si / Annulla )
	 * 
	 * @param title
	 * @param message
	 * @return <b>true</b> - se hai premuto "si" <br>
	 *         <b>false</b> - se hai premuto "Annulla"
	 */
	public boolean showConfirm(String title, String message) {
		ButtonType yes = new ButtonType("Sì", ButtonBar.ButtonData.YES);
		ButtonType cancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, yes, cancel);
		alert.setTitle(title);
		Optional<ButtonType> result = alert.showAndWait();
		return result.orElse(yes) == yes;
	}

	/**
	 * Mostra un Alert di conferma con tre pulsanti ( button1 / button2 / Annulla )
	 * 
	 * @param title   - Titolo dell'Alert
	 * @param message - Messaggio dell'Alert
	 * @param button1 - Text shown in button 1
	 * @param button2 - Text shown in button 2
	 * @return 0 (on Cancel pressed) <br>
	 *         1 (on button1 pressed) <br>
	 *         2 (on button2 pressed)
	 */
	public int showConfirmThreeChoices(String title, String message, String button1, String button2) {
		ButtonType btn1 = new ButtonType(button1, ButtonBar.ButtonData.YES);
		ButtonType btn2 = new ButtonType(button2, ButtonBar.ButtonData.YES);
		ButtonType cancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, btn1, btn2, cancel);
		alert.setTitle(title);
		Optional<ButtonType> result = alert.showAndWait();

		if (result.orElse(btn1) == btn1)
			return 1;
		if (result.orElse(btn2) == btn2)
			return 2;

		return 0;
	}

	public void setMainFrameScene() {
		try {
			configurazione = Client.getInstance().getConfig();
			FXMLLoader loader = loadFXML(Settings.MAINFRAME_FXML);
			Parent root = (Parent) loader.load();
			mainFrameController = loader.getController();
			Thread t = new Thread(Client.getInstance());
			t.setDaemon(true);
			t.start();
			mainFrameController.start();
			scene = new Scene(root);
			stage.setMinHeight(Settings.MIN_HEIGHT_SIZE);
			stage.setMinWidth(Settings.MIN_WIDTH_SIZE);
			stage.setScene(scene);
			setDashboardScene(mainFrameController.getPermissions());
			stage.setResizable(true);
			stage.setMaximized(true);
			stage.show();
		} catch (Exception e) {
			Logger.getInstance().captureException(e, e.getMessage());
		}
	}

	/**
	 * Imposta il titola della scena
	 * 
	 * @param sceneTitle
	 */
	public void setSceneTitle(String sceneTitle) {
		String applicationTitle = Settings.APPLICATION_TITLE;
		String separatorTitle = Settings.APPLICATION_SEPARATOR_TITLE;
		if (configurazione != null) {
			applicationTitle = configurazione.getTitoloNegozio();
			separatorTitle = configurazione.getSeparatorNegozio();
		}

		stage.setTitle(applicationTitle + " " + separatorTitle + " " + sceneTitle);
	}

	public FXMLLoader loadMainPane(String filename, String title) {
		try {
			if (dashboardController != null)
				dashboardController.stop();

			FXMLLoader loader = loadFXML(filename);
			AnchorPane pane = (AnchorPane) loader.load();
			AnchorPane.setBottomAnchor(pane, 0d);
			AnchorPane.setRightAnchor(pane, 0d);
			AnchorPane.setLeftAnchor(pane, 0d);
			AnchorPane.setTopAnchor(pane, 0d);
			setSceneTitle(title);
			changeThemeUI(scene);
			mainFrameController.setMainPane(pane);

			return loader;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, e.getMessage());
			return null;
		}
	}

	public void setDashboardScene(boolean allPermissions) {
		FXMLLoader loader = null;
		if (allPermissions)
			loader = loadMainPane(Settings.DASHBOARD_FXML, Settings.DASHBOARD_TITLE);
		else
			loader = loadMainPane(Settings.DASHBOARD_CLIENT_FXML, Settings.DASHBOARD_TITLE);
		dashboardController = loader.getController();
		dashboardController.start();
	}

	public void setProdottiScene() {
		loadMainPane(Settings.PRODOTTI_FXML, Settings.PRODOTTI_TITLE);
	}

	public void setCarrelloScene() {
		FXMLLoader loader = loadMainPane(Settings.CARRELLO_FXML, Settings.CARRELLO_TITLE);
		carrelloController = loader.getController();
		carrelloController.start();
	}

	public void setMagazzinoScene() {
		loadMainPane(Settings.MAGAZZINO_FXML, Settings.MAGAZZINO_TITLE);
	}

	public void setOrdiniScene() {
		loadMainPane(Settings.ORDINI_FXML, Settings.ORDINI_TITLE);
	}

	public void setStatisticheScene() {
		loadMainPane(Settings.STATISTICHE_FXML, Settings.STATISTICHE_TITLE);
	}

	public void setUtentiScene() {
		loadMainPane(Settings.UTENTI_FXML, Settings.UTENTI_TITLE);
	}

	public void setImpostazioniScene() {
		loadMainPane(Settings.IMPOSTAZIONI_FXML, Settings.IMPOSTAZIONI_TITLE);
	}

	public UtenteViewController setAggiungiNuovoUtenteScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.UTENTE_VIEW_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			UtenteViewController utenteController = loader.getController();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			// scene.getStylesheets()
			// .add(getClass().getResource("/application/css/AggiungiNuovoUtente.css").toExternalForm());
			stage.setTitle(Settings.AGGIUNGI_UTENTE_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return utenteController;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public UtenteViewController setModificaUtenteScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.UTENTE_VIEW_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			UtenteViewController utenteController = loader.getController();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			// scene.getStylesheets()
			// .add(getClass().getResource("/application/css/AggiungiNuovoUtente.css").toExternalForm());
			stage.setTitle(Settings.MODIFICA_UTENTE_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return utenteController;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public synchronized void setCategorieScene() {
		loadMainPane(Settings.CATEGORIE_FXML, Settings.CATEGORIE_TITLE);
	}

	public CategoriaViewController setAggiungiNuovaCategoriaScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.CATEGORIA_VIEW_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			CategoriaViewController categoriaController = loader.getController();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			stage.setTitle(Settings.AGGIUNGI_CATEGORIA_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return categoriaController;
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public CategoriaViewController setModificaCategoriaScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.CATEGORIA_VIEW_FXML);
			stage = new Stage();
			Node root = loader.load();
			CategoriaViewController categoriaController = loader.getController();
			// Scene
			scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			stage.setTitle(Settings.MODIFICA_CATEGORIA_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return categoriaController;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public OrdineViewController setVisualizzaOrdineScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.ORDINE_VIEW_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			OrdineViewController ordineViewController = loader.getController();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			stage.setTitle(Settings.VISUALIZZA_ORDINE_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setMinHeight(Settings.MIN_HEIGHT_SIZE);
			stage.setMinWidth(Settings.MIN_WIDTH_SIZE);
			stage.setResizable(true);
			stage.show();
			return ordineViewController;
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public void setAggiungiProdottoScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.MAGAZZINO_VIEW_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			stage.setTitle(Settings.AGGIUNGI_PRODOTTO_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
		}
	}

	public MagazzinoViewController setModificaProdottoScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.MAGAZZINO_VIEW_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			MagazzinoViewController magazzinoViewController = loader.getController();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			stage.setTitle(Settings.MODIFICA_PRODOTTO_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return magazzinoViewController;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public ChangePasswordController setModificaPasswordScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.PASSWORD_CHANGE_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			ChangePasswordController passwordController = loader.getController();
			Scene scene = new Scene((Parent) root);
			changeThemeUI(scene);
			stage.setScene(scene);
			stage.setTitle(Settings.MODIFICA_PASSWORD_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return passwordController;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public ChangeStoreSettingsController setModificaConfigNegozioScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.STORE_SETTINGS_CHANGE_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			ChangeStoreSettingsController storeController = loader.getController();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			stage.setTitle(Settings.MODIFICA_CONFIGURAZIONI_NEGOZIO_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return storeController;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public void changeThemeUI(THEME theme) {
		this.theme = theme;
		if (theme == THEME.LIGHT)
			tema = Settings.LIGHT_THEME;
		else
			tema = Settings.DARK_THEME;
		 changeThemeUI(scene);
	}

	private void changeThemeUI(Scene sc) {
		sc.getStylesheets().clear();
		if (theme.equals(THEME.AUTO)) {
			if (DetectTheme.isDarkModeActive()) {
				theme = THEME.DARK;
				tema = Settings.DARK_THEME;
			} else {
				theme = THEME.LIGHT;
				tema = Settings.LIGHT_THEME;
			}
		}
		sc.getStylesheets().add(getClass().getResource("/application/css/" + tema + ".css").toExternalForm());
	}

	public ProdottoViewController setVisualizzaProdottoScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.PRODOTTO_VIEW_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			ProdottoViewController controller = loader.getController();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			stage.setTitle(Settings.PRODOTTO_VIEW_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return controller;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public CheckoutController setCheckoutScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.CHECKOUT_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			CheckoutController controller = loader.getController();
			Scene scene = new Scene((Parent) root);
			changeThemeUI(scene);
			stage.setScene(scene);
			stage.setTitle(Settings.CHECKOUT_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return controller;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}

	public HelpController setHelpScene() {
		try {
			FXMLLoader loader = loadFXML(Settings.HELP_FXML);
			Stage stage = new Stage();
			Node root = loader.load();
			HelpController controller = loader.getController();
			Scene scene = new Scene((Parent) root);
			stage.setScene(scene);
			changeThemeUI(scene);
			stage.setTitle(Settings.HELP_TITLE);
			stage.initModality(Modality.APPLICATION_MODAL); // finestra principale bloccata
			stage.setResizable(false);
			stage.show();
			return controller;
		} catch (IOException e) {
			Logger.getInstance().captureException(e);
			return null;
		}
	}
}
