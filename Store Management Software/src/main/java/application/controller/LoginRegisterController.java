package application.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import application.DataValidation;
import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.common.Logger;
import application.common.Protocol;
import io.sentry.SentryLevel;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class LoginRegisterController {

	@FXML
	private Hyperlink loginLink;

	@FXML
	private ImageView image;

	@FXML
	private JFXCheckBox loginShowPassword;

	@FXML
	private JFXTextField loginPasswordTextField;

	@FXML
	private AnchorPane sliderPane;

	@FXML
	private JFXTextField registerEmail;

	@FXML
	private JFXTextField registerNome;

	@FXML
	private JFXTextField registerPasswordTextField;

	@FXML
	private JFXTextField registerCognome;

	@FXML
	private Hyperlink registerLink;

	@FXML
	private Tooltip registerToolTipPassword;

	@FXML
	private JFXTextField loginEmail;

	@FXML
	private JFXButton registerButton;

	@FXML
	private JFXButton loginButton;

	@FXML
	private JFXPasswordField loginPassword;

	@FXML
	private JFXPasswordField registerPassword;

	@FXML
	private JFXCheckBox registerShowPassword;

	@FXML
	private AnchorPane loginPane;

	@FXML
	private AnchorPane registerPane;

	@FXML
	private Hyperlink forgotPasswordLink;

	@FXML
	void initialize() {
		focusLoginFields(true);
		registerToolTipPassword.setShowDelay(new Duration(Settings.TOOLTIP_DELAY_DURATION));
		registerToolTipPassword.setText("La password deve contenere almeno " + Settings.PASSWORD_LENGHT + " caratteri");
	}

	@FXML
	void forgotPassword(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		String email = loginEmail.getText();

		if (email.isBlank()) {
			SceneHandler.getInstance().showError("Il campo Email non può essere vuoto!");
			return;
		}

		if (!DataValidation.validateEmail(loginEmail)) {
			SceneHandler.getInstance().showError("Email non valida!");
			return;
		}

		String res = Client.getInstance().sendNewPassword(email);
		System.out.println(res);
		if (res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showInfo("Password Inviata",
					"La tua password nuova è stata inviata a: " + email + " \nCambiala al più presto!");
			return;
		}
		if (res.equals(Protocol.USER_ALREADY_LOGGED)) {
			SceneHandler.getInstance().showError(
					"Non puoi rigenerare la password di questo utente, perché è già collegato da un altro dispositivo.");
		} else if (res.equals(Protocol.ERROR_USER_NOT_EXISTS))
			SceneHandler.getInstance().showError("Non esiste alcun utente con l'email: " + email);
		else
			Logger.getInstance().captureMessage("Password Non Rigenerata. Email inserita: " + email,
					SentryLevel.WARNING);

	}

	@FXML
	void loginShowPass(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		showPassLogin();
	}

	private void showPassLogin() {
		if (loginShowPassword.isSelected()) {
			loginPasswordTextField.setVisible(true);
			loginPasswordTextField.setText(loginPassword.getText());
			loginPassword.setVisible(false);
			return;
		}
		loginPasswordTextField.setVisible(false);
		loginPassword.setText(loginPasswordTextField.getText());
		loginPassword.setVisible(true);

	}

	@FXML
	void registerScene(MouseEvent event) {
		focusLoginFields(false);
		SceneHandler.getInstance().setRegisterScene();
		resetRegisterFields();
		animationPane(500);
	}

	private void showPassRegister() {
		if (registerShowPassword.isSelected()) {
			registerPasswordTextField.setVisible(true);
			registerPasswordTextField.setText(registerPassword.getText());
			registerPassword.setVisible(false);
			return;
		}
		registerPasswordTextField.setVisible(false);
		registerPassword.setText(registerPasswordTextField.getText());
		registerPassword.setVisible(true);
	}

	@FXML
	void registerShowPass(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		showPassRegister();
	}

	@FXML
	void loginScene(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		focusLoginFields(true);
		SceneHandler.getInstance().setLoginScene();
		resetLoginFields();
		animationPane(0);

	}

	private void setFocusFields(AnchorPane pane, boolean focusEnabled) {
		for (Node node : pane.getChildren())
			node.setFocusTraversable(focusEnabled);

	}

	private void focusLoginFields(boolean focusEnabled) {
		setFocusFields(registerPane, !focusEnabled);
		setFocusFields(loginPane, focusEnabled);
	}

	@FXML
	void registerChangeFocus(KeyEvent event) {
		if (!event.getCode().equals(KeyCode.ENTER))
			return;

		if (registerNome.isFocused())
			registerCognome.requestFocus();
		else if (registerCognome.isFocused())
			registerEmail.requestFocus();
		else if (registerEmail.isFocused()) {
			if (registerShowPassword.isSelected())
				registerPasswordTextField.requestFocus();
			else
				registerPassword.requestFocus();
		}

	}

	@FXML
	void loginChangeFocus(KeyEvent event) {
		if (event.getCode().equals(KeyCode.ENTER) && !loginPassword.isFocused())
			loginPassword.requestFocus();
	}

	@FXML
	void registerAction(ActionEvent event) {
		if (registerShowPassword.isSelected()) {
			registerShowPassword.setSelected(false);
			showPassRegister();
		}
		if (registerNome.getText().isBlank() || registerCognome.getText().isBlank() || registerEmail.getText().isBlank()
				|| registerPassword.getText().isBlank()) {
			SceneHandler.getInstance().showError("I campi non possono essere vuoti!");
			return;
		}

		if (!DataValidation.validateEmail(registerEmail)) {
			SceneHandler.getInstance().showError("email non valida!");
			return;
		}

		if (!DataValidation.isValid(registerPassword)) {
			SceneHandler.getInstance().showError("La password non rispetta i requisiti richiesti!");
			return;
		}

		authentication(false);
	}

	@FXML
	void loginAction(ActionEvent event) {
		if (loginShowPassword.isSelected()) {
			loginShowPassword.setSelected(false);
			showPassLogin();
		}
		if (loginEmail.getText().isBlank() || loginPassword.getText().isBlank()) {
			SceneHandler.getInstance().showError("I campi non possono essere vuoti!");
			return;
		}
		if (!DataValidation.validateEmail(loginEmail)) {
			SceneHandler.getInstance().showError("email non valida!");
			return;
		}

		if (!DataValidation.isValid(loginPassword)) {
			SceneHandler.getInstance().showError("La password non rispetta i requisiti richiesti!");
			return;
		}
		authentication(true);
	}

	void resetRegisterFields() {
		registerEmail.clear();
		registerNome.clear();
		registerCognome.clear();
		registerPasswordTextField.clear();
		registerPassword.clear();
	}

	void resetLoginFields() {
		loginEmail.clear();
		loginPasswordTextField.clear();
		loginPassword.clear();
	}

	/**
	 * Trasla l'anchorPane per rivelare il form di Registrazione e coprire il form
	 * di Login. <br>
	 * 
	 * @param positionX - la posizione che il componente deve raggiungere tramite la
	 *                  translazione
	 */
	void animationPane(int positionX) {
		Timeline timeline = new Timeline();
		// Crea una keyValue. Dobbiamo spostare il sliderPane -- Cambiamo gradualmente
		// il valore di x verso positionX
		KeyValue kv = new KeyValue(sliderPane.translateXProperty(), positionX, Interpolator.EASE_IN);
		KeyFrame kf = new KeyFrame(Duration.seconds(0.3), kv); // Crea keyframe di 0.3s con keyvalue kv
		timeline.getKeyFrames().add(kf); // Aggiunge il frame alla timeline
		timeline.play(); // Avvia l'animazione
	}

	private void authentication(boolean login) {
		String res;

		if (login)
			res = Client.getInstance().authentication(loginEmail.getText(), loginPassword.getText());
		else
			res = Client.getInstance().authentication(registerNome.getText(), registerCognome.getText(),
					registerEmail.getText(), registerPassword.getText());

		if (res.equals(Protocol.OK)) {
			Client.getInstance().initializeUser();
			SceneHandler.getInstance().setMainFrameScene();
			return;
		}
		SceneHandler.getInstance().showError(res);
		Client.getInstance().reset();
	}

}
