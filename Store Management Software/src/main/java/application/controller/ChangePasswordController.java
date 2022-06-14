package application.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import application.DataValidation;
import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.common.Protocol;
import application.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ChangePasswordController {

	@FXML
	private Text titolo;

	@FXML
	private JFXPasswordField currentPassword;

	@FXML
	private JFXTextField currentPasswordField;

	@FXML
	private JFXPasswordField newPassword;

	@FXML
	private JFXTextField newPasswordField;

	@FXML
	private Tooltip toolTipPassword;

	@FXML
	private JFXPasswordField confirmNewPassword;

	@FXML
	private JFXTextField confirmNewPasswordField;

	@FXML
	private Tooltip toolTipPassword1;

	@FXML
	private JFXCheckBox showPassword;

	@FXML
	private JFXButton save;

	User currentUser;

	void showPass() {
		if (showPassword.isSelected()) {
			currentPassword.setVisible(false);
			currentPasswordField.setText(currentPassword.getText());
			currentPasswordField.setVisible(true);

			newPassword.setVisible(false);
			newPasswordField.setText(newPassword.getText());
			newPasswordField.setVisible(true);

			confirmNewPassword.setVisible(false);
			confirmNewPasswordField.setText(confirmNewPassword.getText());
			confirmNewPasswordField.setVisible(true);
		} else {
			currentPassword.setVisible(true);
			currentPassword.setText(currentPasswordField.getText());
			currentPasswordField.setVisible(false);

			newPassword.setVisible(true);
			newPassword.setText(newPasswordField.getText());
			newPasswordField.setVisible(false);

			confirmNewPassword.setVisible(true);
			confirmNewPassword.setText(confirmNewPasswordField.getText());
			confirmNewPasswordField.setVisible(false);

		}
	}

	@FXML
	void salvaAction(ActionEvent event) {
		if (showPassword.isSelected())
			showPassword.setSelected(false);
		showPass();
		if (!DataValidation.isValid(confirmNewPassword) || !DataValidation.isValid(newPassword)) {
			SceneHandler.getInstance()
					.showError("La nuova password deve contenere almeno " + Settings.PASSWORD_LENGHT + " caratteri");
			return;
		}

		if (!newPassword.getText().equals(confirmNewPassword.getText())) {
			SceneHandler.getInstance().showError("Le password non corrispondono!");
			return;
		}

		int id = currentUser.getIdUtente();
		String password = currentPassword.getText();

		String resCheck = Client.getInstance().checkPassword(id, password);
		if (resCheck.equals(Protocol.OK)) {
			String newUpdatePassword = confirmNewPassword.getText();
			String res = Client.getInstance().changePassword(id, newUpdatePassword);
			if (res.equals(Protocol.OK)) {
				Stage stage = (Stage) save.getScene().getWindow();
				stage.close();

				SceneHandler.getInstance().setImpostazioniScene();

				String titoloAlert = "Password Modificata Con Successo";
				String messageAlert = "La password è stata modificata con successo!";
				SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
			} else
				SceneHandler.getInstance().showError(res);

		} else
			SceneHandler.getInstance().showError("password corrente non corretta!");

	}

	@FXML
	void showPasswordAction(MouseEvent event) {
			showPass();
	}

	public void setUser(User user) {
		currentUser = user;
	}

}
