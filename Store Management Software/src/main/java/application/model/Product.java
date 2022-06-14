package application.model;

import java.io.Serializable;

import application.common.FileBlob;
import javafx.scene.image.Image;

public class Product implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8401320660265198409L;

	private int idProdotto;
	private String nome;
	private String descrizione;
	private Double prezzo;
	private int quantita;
	private FileBlob foto;
	private Category categoria;

	public int getIdProdotto() {
		return idProdotto;
	}

	public Image getFoto() {
		return foto.toImage();
	}

	public FileBlob getFotoFileBlob() {
		return foto;
	}

	public String getNome() {
		return nome;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public Double getPrezzo() {
		return prezzo;
	}

	public Integer getQuantita() {
		return quantita;
	}

	public Category getCategoria() {
		return categoria;
	}

	public String getNomeCategoria() {
		return categoria.getNome();
	}

	public Product(String nome, String descrizione, Double prezzo, Integer quantita, FileBlob foto,
			Category categoria) {
		super();
		this.nome = nome;
		this.descrizione = descrizione;
		this.prezzo = prezzo;
		this.quantita = quantita;
		this.foto = foto;
		this.categoria = categoria;
	}

	public Product(int idProdotto, String nome, String descrizione, Double prezzo, int quantita, FileBlob foto,
			Category categoria) {
		this.idProdotto = idProdotto;
		this.nome = nome;
		this.descrizione = descrizione;
		this.prezzo = prezzo;
		this.quantita = quantita;
		this.foto = foto;
		this.categoria = categoria;
	}

	@Override
	public String toString() {
		return "Product [idProdotto=" + idProdotto + ", nome=" + nome + ", descrizione=" + descrizione + ", prezzo="
				+ prezzo + ", quantita=" + quantita + ", categoria=" + categoria.getNome() + "]";
	}

}
