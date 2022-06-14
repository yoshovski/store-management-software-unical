package application.model;

import java.io.Serializable;

/**
 * Rappresenta la struttura di un email: Soggetto, Corpo Testo e Destinatario
 */
public class Email implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 248517449726489016L;

	String subject;
	String text; // body text
	String recipient; // destinatario

	/**
	 * Costruttore che rappresenta l'intera struttura di un email
	 * 
	 * @param subject
	 * @param text
	 * @param recipient
	 */
	public Email(String subject, String text, String recipient) {
		this.subject = subject;
		this.text = text;
		this.recipient = recipient;
	}

	public Email() {

	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
}
