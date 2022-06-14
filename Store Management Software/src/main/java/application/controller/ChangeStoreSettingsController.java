package application.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import application.DataValidation;
import application.SceneHandler;
import application.client.Client;
import application.common.Protocol;
import application.common.SimpleEncryptDecrypt;
import application.model.Configurazione;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ChangeStoreSettingsController {

	@FXML
	private Text titolo;

	@FXML
	private JFXTextField storeName;

	@FXML
	private JFXTextField separator;

	@FXML
	private JFXTextField mailboxServer;

	@FXML
	private JFXTextField portNumber;

	@FXML
	private JFXTextField mailboxEmail;

	@FXML
	private JFXPasswordField mailboxPassword;

	@FXML
	private JFXTextField mailboxPasswordText;

	@FXML
	private Tooltip toolTipPassword;

	@FXML
	private JFXCheckBox showPassword;

	@FXML
	private JFXButton save;

	private Configurazione currentConfig;

	@FXML
	void salvaAction(ActionEvent event) {
		if (storeName.getText().isBlank() || separator.getText().isBlank() || mailboxServer.getText().isBlank()
				|| mailboxEmail.getText().isBlank()) {
			SceneHandler.getInstance().showError("Controlla tutti i campi!");
			return;
		}

		if (!DataValidation.validateEmail(mailboxEmail)) {
			SceneHandler.getInstance().showError("Controlla tutti i campi!");
			return;
		}

		if (!DataValidation.isValidNumber(portNumber)) {
			SceneHandler.getInstance().showError("Numero porta non valido. Puoi inserire solo numeri!");
			return;
		}

		if (!DataValidation.isValidHost(mailboxServer)) {
			SceneHandler.getInstance().showError("Server host non valido!");
			return;
		}

		String encryptedPassword = "";

		if (!mailboxPassword.getText().equals("") || !mailboxPassword.getText().isBlank()) {
			String cipher = String.valueOf((mailboxServer.getText().length() + Integer.parseInt(portNumber.getText()))
					+ mailboxEmail.getText());
			SimpleEncryptDecrypt encription = new SimpleEncryptDecrypt(cipher, mailboxPassword.getText(), true);
			encryptedPassword = encription.getEncryptedText();
		} else {
			SceneHandler.getInstance().showError("Password vuota!");
			return;
		}

		if (event.getSource() != save)
			return;
		Configurazione config = new Configurazione(currentConfig.getIdConfig(), storeName.getText(),
				separator.getText(), mailboxServer.getText(), Integer.parseInt(portNumber.getText()),
				mailboxEmail.getText(), encryptedPassword);
		String res = Client.getInstance().editConfig(config);

		if (res.equals(Protocol.OK)) {
			Stage stage = (Stage) save.getScene().getWindow();
			stage.close();

			SceneHandler.getInstance().setImpostazioniScene();

			String titoloAlert = "Configurazione Modificata Con Successo";
			String messageAlert = "Configurazioni del negozio modificato con successo!";
			SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
		} else
			SceneHandler.getInstance().showError(res);

	}

	@FXML
	void showPasswordAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		if (showPassword.isSelected()) {
			mailboxPasswordText.setVisible(true);
			mailboxPasswordText.setText(mailboxPassword.getText());
			mailboxPassword.setVisible(false);
		} else {
			mailboxPasswordText.setVisible(false);
			mailboxPassword.setText(mailboxPasswordText.getText());
			mailboxPassword.setVisible(true);
		}
	}

	public void setConfig(Configurazione config) {
		currentConfig = config;
		storeName.setText(config.getTitoloNegozio());
		separator.setText(config.getSeparatorNegozio());
		mailboxServer.setText(config.getServerPosta());
		portNumber.setText(Integer.toString(config.getNumPorta()));
		mailboxEmail.setText(config.getEmailPosta());
		mailboxPassword.setText("");
		mailboxPasswordText.setText("");
	}

}
