package application.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXToggleButton;

import application.DetectTheme;
import application.SceneHandler;
import application.Settings;
import application.Settings.THEME;
import application.client.Client;
import application.common.Protocol;
import application.model.Configurazione;
import application.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class ImpostazioniController {

	@FXML
	private JFXToggleButton toggleTema;

    @FXML
    private JFXCheckBox temaAutomatico;

	@FXML
	private JFXButton salvaPreferenzeButton;

	@FXML
	private Label nome;

	@FXML
	private Label cognome;

	@FXML
	private Label emailUtente;

	@FXML
	private Label telefono;

	@FXML
	private JFXButton modificaInfoPersonaliButton;

	@FXML
	private JFXButton modificaPasswordButton;

	@FXML
	private VBox schedaConfig;

	@FXML
	private Label titoloNegozio;

	@FXML
	private Label separatorNegozio;

	@FXML
	private Label serverPosta;

	@FXML
	private Label numeroPorta;

	@FXML
	private Label emailPosta;

	@FXML
	private JFXButton modificaConfigurazioniButton;

	@FXML
	private VBox sezioni;
	
    @FXML
    private VBox preferenzeApp;
    

    @FXML
    private ScrollPane container;

	private User user = Client.getInstance().getUser();

	private Configurazione config;

	private THEME currentTheme;

	private boolean allPermissions = user.hasRole(Settings.ALL_PERMISSIONS);

	@FXML
	void initialize() {
		//sezioni.getChildren().remove(preferenzeApp);
		permissionBasedView();
		currentTheme = Client.getInstance().getTheme(user.getIdUtente());

		if (currentTheme.compareTo(THEME.AUTO) == 0) {
			toggleTema.setDisable(true);
			temaAutomatico.setSelected(true);
			toggleTema.setSelected(DetectTheme.isDarkModeActive());
		} else {
			toggleTema.setDisable(false);
			temaAutomatico.setSelected(false);
			if (currentTheme.compareTo(THEME.LIGHT) == 0)
				toggleTema.setSelected(false);
			else
				toggleTema.setSelected(true);
		}

		nome.setText(user.getNome());
		cognome.setText(user.getCognome());
		emailUtente.setText(user.getEmail());
		telefono.setText(user.getTelefono());
		config = Client.getInstance().getConfig();
		titoloNegozio.setText(config.getTitoloNegozio());
		separatorNegozio.setText(config.getSeparatorNegozio());
		serverPosta.setText(config.getServerPosta());
		numeroPorta.setText(String.valueOf(config.getNumPorta()));
		emailPosta.setText(config.getEmailPosta());
	}

	private void permissionBasedView() {
		if (!allPermissions)
			sezioni.getChildren().remove(schedaConfig);
	}

	@FXML
	void modificaConfigurazioniAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		ChangeStoreSettingsController controller = SceneHandler.getInstance().setModificaConfigNegozioScene();
		controller.setConfig(config);

	}

	@FXML
	void modificaInfoPersonaliAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		UtenteViewController controller = SceneHandler.getInstance().setModificaUtenteScene();
		if (user == null) {
			SceneHandler.getInstance().showInfo("Problema durante il caricamento dei tuoi dati", "Riprova più tardi.");
			return;
		}
		controller.setImpostazioniScene(true);
		controller.modificaView(user);

	}

	@FXML
	void modificaPasswordAction(MouseEvent event) {
		if (user == null) {
			SceneHandler.getInstance().showInfo("Problema durante il caricamento dei tuoi dati", "Riprova più tardi.");
			return;
		}

		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		ChangePasswordController controller = SceneHandler.getInstance().setModificaPasswordScene();
		controller.setUser(user);
	}

	@FXML
	void disableToggle(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		boolean isEnable = temaAutomatico.isSelected();
		toggleTema.setDisable(isEnable);
	}

	@FXML
	void salvaPreferenzeAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		THEME tema = THEME.AUTO;
		if (!toggleTema.isDisabled())
			if (toggleTema.isSelected())
				tema = THEME.DARK;
			else
				tema = THEME.LIGHT;

		int idUtente = user.getIdUtente();
		String res = Client.getInstance().changeTheme(idUtente, tema);
		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance()
					.showError("Errore nel salvataggio delle tue preferenze.\nSi prega di riprovare più tardi");
			return;
		}

		String titoloAlert = "Tema Cambiato";
		String messaggioAlert = "La tua preferenza è stata salvata con successo!";
		SceneHandler.getInstance().setImpostazioniScene();
		SceneHandler.getInstance().showInfo(titoloAlert, messaggioAlert);
		System.out.println("Setto tema: "+tema);
		Client.getInstance().setTheme(tema);
	}

}
