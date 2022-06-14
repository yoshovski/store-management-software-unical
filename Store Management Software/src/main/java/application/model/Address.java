package application.model;

import java.io.Serializable;

public class Address implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 345789284463622853L;

	private String nome_via;
	private String CAP;
	private int num_civico;
	private String nazione;
	private String regione;
	private String citta;
	private int idIndirizzo;

	public String getNome_via() {
		return nome_via;
	}

	public String getCAP() {
		return CAP;
	}

	public int getNum_civico() {
		return num_civico;
	}

	public String getNazione() {
		return nazione;
	}

	public String getRegione() {
		return regione;
	}

	public String getCitta() {
		return citta;
	}

	public int getIdIndirizzo() {
		return idIndirizzo;
	}

	public Address(String nome_via, String CAP, int num_civico, String nazione, String regione, String citta) {
		super();
		this.nome_via = nome_via;
		this.CAP = CAP;
		this.num_civico = num_civico;
		this.nazione = nazione;
		this.regione = regione;
		this.citta = citta;
	}

	public Address(int idIndirizzo, String nome_via, String CAP, int num_civico, String nazione, String regione,
			String citta) {
		super();
		this.nome_via = nome_via;
		this.CAP = CAP;
		this.num_civico = num_civico;
		this.nazione = nazione;
		this.regione = regione;
		this.citta = citta;
		this.idIndirizzo = idIndirizzo;
	}

	@Override
	public String toString() {
		return nome_via + ", " + num_civico + ", " + citta + " " + CAP + " " + regione + ", " + nazione;
	}

}
