package application.model;

import java.io.Serializable;

public class OrderState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8516173768200244827L;
	private String nome;
	private int idStato;
	private String colore;

	public OrderState(int idStato, String nome, String colore) {
		this.idStato = idStato;
		this.nome = nome;
		this.colore = colore;
	}

	public OrderState(int idStato, String nome) {
		this.idStato = idStato;
		this.nome = nome;
	}

	public int getIdStato() {
		return idStato;
	}

	public String getNome() {
		return nome;
	}

	public String getColore() {
		return colore;
	}

	@Override
	public String toString() {
		return getNome();
	}

}
