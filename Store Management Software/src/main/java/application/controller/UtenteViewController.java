package application.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import application.DataValidation;
import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.client.FilePicker;
import application.client.FilterType;
import application.common.FileBlob;
import application.common.Protocol;
import application.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UtenteViewController {

	@FXML
	private Tooltip toolTipPassword;

	@FXML
	private JFXTextField passwordTextField;

	@FXML
	private JFXPasswordField password;

	@FXML
	private JFXComboBox<String> ruolo;

	@FXML
	private Label error;

	@FXML
	private JFXTextField cognome;

	@FXML
	private JFXCheckBox showPassword;

	@FXML
	private JFXButton salva;

	@FXML
	private JFXButton aggiungi;

	@FXML
	private JFXTextField nome;

	@FXML
	private JFXTextField telefono;

	@FXML
	private JFXTextField email;

	@FXML
	private Text titolo;

	@FXML
	private Circle avatarContainer;

	private int idUtente;
	
	private User currentUser = Client.getInstance().getUser();

	private FileBlob avatar = new FileBlob();
	
	private boolean allPermissions = currentUser.hasRole(Settings.ALL_PERMISSIONS);
	
	private boolean impostazioniScene = false;

	@FXML
	void initialize() {	
		if(!allPermissions)
			ruolo.setDisable(true);
		
		error = new Label();
		error.setVisible(false);
		titolo.setText("AGGIUNGI NUOVO UTENTE");
		salva.setVisible(false);
		aggiungi.setVisible(true);

		// Caricamento di immagine di default nel contenitore per l'avatar
		FilePicker filePicker = new FilePicker();
		avatarContainer.setFill(new ImagePattern(filePicker.getImage(Settings.DEFAULT_ADD_USER_PROJECT_PATH)));
		ruolo.getItems().addAll(Client.getInstance().getRoles().keySet());
		ruolo.getSelectionModel().selectFirst();

		// Testo per il tooltip della password
		toolTipPassword.setShowDelay(new Duration(Settings.TOOLTIP_DELAY_DURATION));
		toolTipPassword.setText("La password deve contenere almeno " + Settings.PASSWORD_LENGHT + " caratteri");
	}

	private void showPassword() {
		if (showPassword.isSelected()) {
			passwordTextField.setVisible(true);
			passwordTextField.setText(password.getText());
			password.setVisible(false);
			return;
		}
		passwordTextField.setVisible(false);
		password.setText(passwordTextField.getText());
		password.setVisible(true);

	}

	@FXML
	void showPasswordAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		showPassword();
	}

	@FXML
	void addAvatar(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

	//	FilePicker filePicker = new FilePicker(FilterType.IMAGE_FILTER);
		FilePicker filePicker = new FilePicker();
		filePicker.chooseAvatar();
		avatar = filePicker.getFileBlob();
		avatarContainer.setFill(new ImagePattern(filePicker.getImage()));
	}

	@FXML
	void changeFocus(KeyEvent event) {

		DataValidation.validateEmail(email);

		if (!event.getCode().equals(KeyCode.ENTER))
			return;
		if (nome.isFocused())
			cognome.requestFocus();
		else if (cognome.isFocused())
			email.requestFocus();
		else if (email.isFocused())
			telefono.requestFocus();
		else if (telefono.isFocused())
			ruolo.requestFocus();
		else if (ruolo.isFocused()) {
			if (showPassword.isSelected())
				passwordTextField.requestFocus();
			else
				password.requestFocus();
		}
	}

	@FXML
	void salvaAction(ActionEvent event) {
		if (showPassword.isSelected())
			showPassword.setSelected(false);
		showPassword();
		if (checkFields()) {
			SceneHandler.getInstance().showInfo("Campi vuoti", "Devi inserire tutti i dati richiesti!");
			error.setVisible(true);
			error.setText("Devi inserire tutti i dati richiesti!");
			return;
		}
		error.setVisible(false);
		if (nome.getText().isBlank() || cognome.getText().isBlank()) {
			SceneHandler.getInstance().showInfo("Campi vuoti","Nome o Cognome vuoto!");
			error.setVisible(true);
			error.setText("Nome o Cognome vuoto!");
			return;
		}
		if (!DataValidation.validateEmail(email)) {
			SceneHandler.getInstance().showError("Email non valida!");
			error.setVisible(true);
			error.setText("Email non valida!");
			return;
		}
		if (!DataValidation.isValid(telefono)) {
			SceneHandler.getInstance().showError("Telefono non valido!");
			error.setVisible(true);
			error.setText("Telefono non valido!");
			return;
		}
		if (password.getText().isBlank() || DataValidation.isValid(password)) {
			if (event.getSource() == aggiungi)
				addUser();
			else if (event.getSource() == salva)
				editUser();
		} else {
			error.setVisible(true);
			error.setText("Password non valida!");
			return;
		}
	}

	public void editUser() {
		User user = new User(avatar, nome.getText(), cognome.getText(), email.getText(), telefono.getText(),
				ruolo.getValue(), password.getText(), Client.getInstance().getRoles().get(ruolo.getValue()));
		user.setIdUtente(idUtente);
		String res = Client.getInstance().editUser(user);

		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showError(res);
			return;
		}

		Stage stage = (Stage) salva.getScene().getWindow();
		stage.close();
		
		if(allPermissions && !impostazioniScene)
			SceneHandler.getInstance().setUtentiScene();
		else
			SceneHandler.getInstance().setImpostazioniScene();

		String titoloAlert = "Utente Modificato Con Successo";
		String messageAlert = "L'utente " + nome.getText() + " " + cognome.getText() + " è stato modificato!";
		SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);

	}

	public boolean checkFields() {
		return nome.getText().isBlank() || cognome.getText().isBlank() || telefono.getText().isBlank()
				|| email.getText().isBlank();
	}

	private void addUser() {
		String res = Client.getInstance()
				.addNewUser(new User(avatar, nome.getText(), cognome.getText(), email.getText(), telefono.getText(),
						ruolo.getValue(), password.getText(), Client.getInstance().getRoles().get(ruolo.getValue())));

		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showError(res);
			return;
		}

		Stage stage = (Stage) aggiungi.getScene().getWindow();
		stage.close(); // chiude la finestra

			SceneHandler.getInstance().setUtentiScene();

		String titoloAlert = "Utente Aggiunto Con Successo";
		String messageAlert = "L'utente " + nome.getText() + " " + cognome.getText() + " è stato aggiunto!";
		SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
	}

	void resetFields() {
		email.clear();
		nome.clear();
		cognome.clear();
		telefono.clear();
		passwordTextField.clear();
		password.clear();
		showPassword.setSelected(false);
	}

	public void modificaView(User user) {
		if(currentUser.getIdUtente()==user.getIdUtente())
			ruolo.setDisable(true);
		
		if(impostazioniScene) {
			password.setDisable(true);
			passwordTextField.setDisable(true);
			showPassword.setDisable(true);
		}
		
		idUtente = user.getIdUtente();
		titolo.setText("MODIFICA UTENTE");
		email.setDisable(true);
		email.setText(user.getEmail());
		nome.setText(user.getNome());
		cognome.setText(user.getCognome());

		avatar = user.getAvatarFileBlob();
		System.out.println("UtenteViewController.modificaView() avatar: "+user.getAvatarFileBlob().toImage().getHeight());
		Image image = (avatar.toImage());
		avatarContainer.setFill(new ImagePattern(image));

		telefono.setText(user.getTelefono());
		ruolo.setValue(user.getRuolo());
		password.setText("");
		salva.setVisible(true);
		aggiungi.setVisible(false);
	}
	
	public void setImpostazioniScene(boolean b) {
		impostazioniScene = b;
	}

}