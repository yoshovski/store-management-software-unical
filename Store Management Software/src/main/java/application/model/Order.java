package application.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Order implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7595415526739297843L;
	private User utente;
	private Address indirizzo;
	private Timestamp dataOrdine;
	private Double prezzoTotale;
	private OrderState statoOrdine;
	private int idOrdine;
	private String descrizioneStatoOrdine;

	public User getUtente() {
		return utente;
	}

	public Address getIndirizzo() {
		return indirizzo;
	}

	public String getDataOrdineFormattata() {
		String data = new SimpleDateFormat("dd/MM/yyyy").format(dataOrdine);
		return data;
	}

	public OrderState getStatoOrdine() {
		return statoOrdine;
	}

	public Double getPrezzoTotale() {
		return prezzoTotale;
	}

	public Order(int idOrdine, User utente, Address indirizzo, Timestamp dataOrdine, Double prezzoTotale,
			OrderState statoOrdine, String descrizioneStatoOrdine) {
		super();
		this.idOrdine = idOrdine;
		this.utente = utente;
		this.indirizzo = indirizzo;
		this.dataOrdine = dataOrdine;
		this.prezzoTotale = prezzoTotale;
		this.statoOrdine = statoOrdine;
		this.descrizioneStatoOrdine = descrizioneStatoOrdine;
	}

	public Order(int idOrdine, Address indirizzo, Timestamp dataOrdine, Double prezzoTotale, OrderState statoOrdine,
			String descrizioneStatoOrdine) {
		super();
		this.idOrdine = idOrdine;
		this.indirizzo = indirizzo;
		this.dataOrdine = dataOrdine;
		this.prezzoTotale = prezzoTotale;
		this.statoOrdine = statoOrdine;
		this.descrizioneStatoOrdine = descrizioneStatoOrdine;
	}

	public void setUser(User utente) {
		this.utente = utente;
	}

	public int getIdOrdine() {
		return idOrdine;
	}

	public String getDescrizioneStatoOrdine() {
		return descrizioneStatoOrdine;
	}

	public void setStatoOrdine(OrderState statoOrdine) {
		this.statoOrdine = statoOrdine;
	}

	public void setDescrizioneStatoOrdine(String descrizioneStatoOrdine) {
		this.descrizioneStatoOrdine = descrizioneStatoOrdine;
	}

	@Override
	public String toString() {
		return "Order [utente=" + utente + ", indirizzo=" + indirizzo + ", dataOrdine=" + dataOrdine + ", prezzoTotale="
				+ prezzoTotale + ", statoOrdine=" + statoOrdine + ", idOrdine=" + idOrdine + ", descrizioneStatoOrdine="
				+ descrizioneStatoOrdine + "]";
	}

}
