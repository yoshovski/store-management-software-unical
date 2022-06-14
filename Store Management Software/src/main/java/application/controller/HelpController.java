package application.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;

import application.SceneHandler;
import application.client.Client;
import application.common.Protocol;
import application.model.Email;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HelpController {

	@FXML
	private Text titolo;

	@FXML
	private JFXTextArea descrizione;

	@FXML
	private JFXButton invia;

	@FXML
	void sendAction(ActionEvent event) {
		int idUtente = Client.getInstance().getUser().getIdUtente();
		String subject = "Richiesta di supporto per utente " + idUtente;
		String body = descrizione.getText();
		String res = Client.getInstance().sendEmail(new Email(subject, body, ""));
		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance()
					.showError("Errore nell'invio della tua richiesta. Si prega di riprovare più tardi");
			return;
		}
		
		Stage stage = (Stage) invia.getScene().getWindow();
		stage.close();
		boolean result = SceneHandler.getInstance().showInfo("Richiesta inviata!",
				"La tua richiesta d'aiuto è stata inviata con successo");
	}

}
