package application.model;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Category implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3168867067845030876L;

	private int idCategoria;
	private String nome;
	private String descrizione;
	private int idParentCategoria;

	public int getIdCategoria() {
		return idCategoria;
	}

	public String getNome() {
		return nome;
	}

	public StringProperty getNomeProperty() {
		StringProperty nomeProperty = new SimpleStringProperty(nome);
		return nomeProperty;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public Category(int idCategoria, String nome, String descrizione, int idParentCategoria) {
		this.idCategoria = idCategoria;
		this.nome = nome;
		this.descrizione = descrizione;
		this.idParentCategoria = idParentCategoria;
	}

	public Category(String nome, String descrizione, int idParentCategoria) {
		this.nome = nome;
		this.descrizione = descrizione;
		this.idParentCategoria = idParentCategoria;
	}

	public int getIdParentCategoria() {
		return idParentCategoria;
	}

	@Override
	public String toString() {
		return getNome();
	}

}
