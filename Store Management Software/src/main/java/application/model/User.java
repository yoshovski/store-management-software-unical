package application.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

import application.client.Client;
import application.common.FileBlob;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class User implements Serializable {



	/**
	 * 
	 */
	private static final long serialVersionUID = 8272755204554858738L;

	private int idUtente;
	private String nome;
	private String cognome;
	private String password;
	private int idRuolo;
	private String ruolo;
	private String email;
	private String telefono;
	private Timestamp ultimoAccesso;
	private FileBlob avatar;

	public int getIdUtente() {
		return idUtente;
	}

	public void setIdUtente(int idUtente) {
		this.idUtente = idUtente;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public boolean invalidUser() {
		return invalidNames() || invalidEmail();
	}

	public boolean invalidNames() {
		return nome == null || cognome == null || nome.equals("") || cognome.equals("");
	}

	public boolean invalidEmail() {
		return email == null || email.equals("");
	}

	public String getPassword() {
		return password;
	}

	public String getNome() {
		return nome;
	}

	public String getCognome() {
		return cognome;
	}

	public String getRuolo() {
		return ruolo;
	}

	public Image getAvatar() {
		return avatar.toImage();
	}

	public FileBlob getAvatarFileBlob() {
		return avatar;
	}

	public String getEmail() {
		return email;
	}

	public Timestamp getUltimoAccesso() {
		return ultimoAccesso;
	}

	public User(String nome, String cognome, String email, String password) {
		this.nome = nome;
		this.cognome = cognome;
		this.email = email;
		this.password = password;
	}

	public User(String nome, String cognome, String email, FileBlob avatar, String ruolo, Timestamp ultimoAccesso,
			int idRuolo) {
		this.nome = nome;
		this.cognome = cognome;
		this.email = email;
		this.avatar = avatar;
		this.ruolo = ruolo;
		this.ultimoAccesso = ultimoAccesso;
		this.idRuolo = idRuolo;
	}

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public User(FileBlob avatar, String nome, String cognome, String email, String telefono, String ruolo,
			String password, int idRuolo) {
		this.avatar = avatar;
		this.nome = nome;
		this.cognome = cognome;
		this.email = email;
		this.telefono = telefono;
		this.ruolo = ruolo;
		this.password = password;
		this.idRuolo = idRuolo;
	}

	public User() {
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public void setAvatar(FileBlob avatar) {
		this.avatar = avatar;
	}

	public ObjectProperty<ImageView> getImageView() {
		ObjectProperty<ImageView> image = new SimpleObjectProperty<ImageView>();
		image.setValue(new ImageView(getAvatar()));
		return image;
	}

	public String getTelefono() {
		return this.telefono;
	}

	public int getIdRuolo() {
		HashMap<String, Integer> roles = Client.getInstance().getRoles();
		if (roles.equals(null))
			return 0;

		return roles.get(this.ruolo);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Verifica se l'utente ha il ruole "role". Case INSENSITIVE
	 * 
	 * @param role
	 * @return
	 */
	public boolean hasRole(String role) {
		return ruolo.equalsIgnoreCase(role);
	}

	@Override
	public String toString() {
		return "User [idUtente=" + idUtente + ", nome=" + nome + ", cognome=" + cognome + ", password=" + password
				+ ", idRuolo=" + idRuolo + ", ruolo=" + ruolo + ", email=" + email + ", telefono=" + telefono
				+ ", ultimoAccesso=" + ultimoAccesso + ", avatar=" + avatar + "]";
	}

}
