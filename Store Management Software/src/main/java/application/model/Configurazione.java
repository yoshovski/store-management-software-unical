package application.model;

import java.io.Serializable;

public class Configurazione implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7556286117066103049L;

	private int idConfig;
	private String titoloNegozio;
	private String separatorNegozio;
	private String serverPosta;
	private int numPorta;
	private String emailPosta;
	private String passwordPosta;

	public Configurazione(int idConfig, String titoloNegozio, String separatorNegozio, String serverPosta, int numPorta,
			String emailAccesso, String passwordPosta) {
		this.idConfig = idConfig;
		this.titoloNegozio = titoloNegozio;
		this.separatorNegozio = separatorNegozio;
		this.serverPosta = serverPosta;
		this.numPorta = numPorta;
		this.emailPosta = emailAccesso;
		this.passwordPosta = passwordPosta;
	}

	public Configurazione(String titoloNegozio, String separatorNegozio, String serverPosta, int numPorta,
			String emailAccesso, String passwordPosta) {
		this.titoloNegozio = titoloNegozio;
		this.separatorNegozio = separatorNegozio;
		this.serverPosta = serverPosta;
		this.numPorta = numPorta;
		this.emailPosta = emailAccesso;
		this.passwordPosta = passwordPosta;
	}

	public int getIdConfig() {
		return idConfig;
	}

	public void setIdConfig(int idConfig) {
		this.idConfig = idConfig;
	}

	public String getTitoloNegozio() {
		return titoloNegozio;
	}

	public void setTitoloNegozio(String titoloNegozio) {
		this.titoloNegozio = titoloNegozio;
	}

	public String getSeparatorNegozio() {
		return separatorNegozio;
	}

	public void setSeparatorNegozio(String separatorNegozio) {
		this.separatorNegozio = separatorNegozio;
	}

	public String getServerPosta() {
		return serverPosta;
	}

	public void setServerPosta(String serverPosta) {
		this.serverPosta = serverPosta;
	}

	public int getNumPorta() {
		return numPorta;
	}

	public void setNumPorta(int numPorta) {
		this.numPorta = numPorta;
	}

	public String getEmailPosta() {
		return emailPosta;
	}

	public void setEmailPosta(String emailAccesso) {
		this.emailPosta = emailAccesso;
	}

	public String getPasswordPosta() {
		return passwordPosta;
	}

	public void setPasswordPosta(String passwordPosta) {
		this.passwordPosta = passwordPosta;
	}
}
