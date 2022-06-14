package application.server;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import application.client.Client;
import application.common.Logger;
import application.common.Protocol;
import application.common.SimpleEncryptDecrypt;
import application.model.Configurazione;
import application.model.Email;
import application.model.OrderState;
import io.sentry.SentryLevel;

/**
 * Instaura connessione con server email e gestisce l'invio di notifiche tramite
 * email.
 */
public class EmailSender {

	private static EmailSender instance = null;
	private String serverPosta;
	private String userEmail;
	private String encryptedPassword = "";
	private int numPorta;
	private Properties props = new Properties();

	private EmailSender() {
	}

	public static EmailSender getInstance() {
		if (instance == null)
			instance = new EmailSender();

		return instance;
	}

	/**
	 * Inizializza la configurazione iniziale
	 * 
	 * @param config - la configurazione
	 * @return true (successo), false (fallimento)
	 */
	public boolean EmailConfig(Configurazione config) {
		if (config != null) {
			serverPosta = config.getServerPosta();
			userEmail = config.getEmailPosta();
			encryptedPassword = config.getPasswordPosta();
			numPorta = config.getNumPorta();
			return true;
		}
		return false;
	}

	/**
	 * Decifra la password per l'email
	 */
	private synchronized String decryptedPassword() {
		String cipher = String.valueOf((serverPosta.length() + numPorta) + userEmail);
		SimpleEncryptDecrypt decrypt = new SimpleEncryptDecrypt(cipher, encryptedPassword, false);
		return decrypt.getDecryptedText();
	}

	private Session authentication() {
		props.put("mail.smtp.host", serverPosta);
		props.put("mail.smtp.auth", "true");
		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userEmail, decryptedPassword());
			}
		});
		return session;
	}

	public synchronized boolean sendEmail(Email emailBody) {
		try {
			MimeMessage message = new MimeMessage(authentication());
			message.setFrom(new InternetAddress(userEmail));
			String toEmail = emailBody.getRecipient();
			if (emailBody.getRecipient().isBlank())
				toEmail = userEmail;
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
			message.setSubject(emailBody.getSubject());
			message.setText(emailBody.getText());

			// invio messaggio
			Transport.send(message);
			Logger.getInstance().captureMessage("Email was sent to: " + emailBody.getRecipient());
			return true;

		} catch (MessagingException e) {
			String recipient = emailBody.getRecipient();
			if (!recipient.isBlank())
				Logger.getInstance().captureException(e, "Error while sending email to: " + recipient);
			else
				Logger.getInstance().captureException(e, "Error while sending email to ADMIN");
			return false;
		}
	}

	public synchronized boolean sendEmailOrdineModificato(int idOrdine, OrderState stato, String descrizione, String emailRecipient) {
			String subject = "Variazione Ordine #" + idOrdine;
			String text = "Il nuovo stato del tuo ordine e': " + stato.getNome() + "\nMessaggio dal negozio: "
					+ descrizione;
			
			Email email = new Email(subject, text, emailRecipient);

			boolean confirm = sendEmail(email);

			if (confirm) {
				Logger.getInstance()
						.captureMessage("Email inviata con successo per ordine: #" + idOrdine + " all'email: " + email.getRecipient());
				return true;
			}
			else {
				Logger.getInstance().captureMessage("Email NON inviata per ordine: #" + idOrdine + " all'email: " + email.getRecipient(),
						SentryLevel.WARNING);
				return false;
			}
	}
}