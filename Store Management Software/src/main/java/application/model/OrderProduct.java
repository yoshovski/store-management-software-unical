package application.model;

import java.io.Serializable;

public class OrderProduct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3303774175564266001L;
	private int idOrdineProdotti;
	private Order ordine;
	private Product prodotto;
	private int quantita;

	public int getIdOrdineProdotti() {
		return idOrdineProdotti;
	}

	public Order getOrdine() {
		return ordine;
	}

	public Product getProdotto() {
		return prodotto;
	}

	public int getQuantita() {
		return quantita;
	}

	public OrderProduct(int idOrdineProdotti, Order ordine, Product prodotti, int quantita) {
		super();
		this.idOrdineProdotti = idOrdineProdotti;
		this.ordine = ordine;
		this.prodotto = prodotti;
		this.quantita = quantita;
	}

	@Override
	public String toString() {
		return "OrderProduct [idOrdineProdotti=" + idOrdineProdotti + ", ordine=" + ordine.toString() + ", prodotti="
				+ prodotto.toString() + ", quantita=" + quantita + "]";
	}

}
