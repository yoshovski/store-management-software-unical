package application.server;

import org.springframework.security.crypto.bcrypt.BCrypt;

import application.Settings;
import application.common.FileBlob;
import application.common.Logger;
import application.model.Address;
import application.model.Category;
import application.model.Configurazione;
import application.model.DashboardData;
import application.model.Email;
import application.model.Order;
import application.model.OrderProduct;
import application.model.OrderState;
import application.model.Product;
import application.model.User;
import io.sentry.ITransaction;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHandler {

	private static DatabaseHandler instance = null;

	private ArrayList<OrderState> orderState = null;
	public HashMap<String, Integer> roles = null;
	private Configurazione configurazione = null;

	private DatabaseHandler() throws SQLException {
		Configurazione config = initConfig();
		configurazione = config;
		EmailSender.getInstance().EmailConfig(config); // configurazione più recente, salvata nel DB
	}

	public static DatabaseHandler getInstance() throws SQLException {
		if (instance == null)
			instance = new DatabaseHandler();
		return instance;
	}

	/**
	 * Apertura della connessione col DB
	 * 
	 * @return true (se la connessione è avvenuta con success) <br>
	 *         false (altrimenti)
	 */

	/**
	 * Scarica le impostazioni scelte dall'utente
	 * 
	 * @param user
	 */
	public synchronized void loadSettings(User user) {
		if (!existsUser(user))
			return;

		int idUtente = user.getIdUtente();
		try {
			Connection con = DataSource.getConnection();
			PreparedStatement p = con.prepareStatement("SELECT * FROM Impostazioni WHERE id_utente = ?");
			p.setInt(1, idUtente);
			p.execute();

			p.close();
			con.close();
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "error while retrieving settings of user " + idUtente);
		}

	}

	/**
	 * Inserisce un nuovo utente nel DB dalla fase di registrazione
	 * 
	 * @param user
	 * @return true - se inserito con successo <br>
	 *         false - altrimenti
	 */
	public synchronized boolean insertUser(User user) {

		if (user.invalidUser() || existsUser(user))
			return false;
		String email = user.getEmail();
		try {
			Connection con = DataSource.getConnection();
			PreparedStatement p = con.prepareStatement(
					"INSERT INTO `Utente` (`nome`, `cognome`, `email`, `password`,`ultimo_accesso`,`avatar`) VALUES(?, ?, ?, ?, ?, ?);");

			p.setString(1, user.getNome());
			p.setString(2, user.getCognome());
			p.setString(3, email);
			p.setString(4, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
			p.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

			// carica l'avatar di deafault
			String path = getClass().getResource(Settings.DEFAULT_AVATAR_PROJECT_PATH).getPath();

			FileBlob image = new FileBlob();
			Blob blobImage = image.toBlob(path);

			p.setBlob(6, blobImage);
			int rs = p.executeUpdate();
			boolean result = rs >= 1;
			p.close();

			/*
			 * String query2 = "INSERT INTO Impostazioni (id_utente) VALUES(?);";
			 * PreparedStatement p2 = con.prepareStatement(query2); p2.setInt(1,
			 * user.getIdUtente()); p2.executeUpdate(); p2.close();
			 */
			con.close();
			return result;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "error while adding user " + email);
			return false;
		}
	}

	/**
	 * Verifica se l'utente esiste già
	 * 
	 * @param email
	 * @return true - l'utente esiste <br>
	 *         false - l'utente NON esiste
	 */
	public synchronized boolean existsUser(String email) {
		try {
			Connection con = DataSource.getConnection();

			String query = "SELECT * FROM Utente WHERE email= ? AND isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, email);

			ResultSet rs = p.executeQuery();
			boolean result = rs.next(); // se rs non è vuoto,
			p.close();
			con.close();

			return result;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error while checking if user " + email + " exists.");
			return false;
		}
	}

	/**
	 * Verifica se l'utente esiste già
	 * 
	 * @param user
	 * @return true - l'utente esiste <br>
	 *         false - l'utente NON esiste
	 */
	public synchronized boolean existsUser(User user) {
		String email = user.getEmail();
		return existsUser(email);
	}

	/**
	 * Controlla se le credenziali dell'utente sono corrette
	 * 
	 * @param user
	 * @return true - credenziali corrette <br>
	 *         false - credenziali NON corrette
	 * @throws SQLException
	 */
	public synchronized boolean checkUser(User user) {
		if (user.invalidEmail())
			return false;

		int idUtente = user.getIdUtente();
		String email = user.getEmail();
		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM Utente WHERE email=? AND isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);

			// p.setInt(1, idUtente);
			p.setString(1, email);
			ResultSet rs = p.executeQuery();
			boolean result = false;
			if (rs.next()) {
				String password = rs.getString("password");
				result = BCrypt.checkpw(user.getPassword(), password); // controllo se le password corrispondono
			}
			p.close();
			con.close();
			if (result)
				updateLastAccess(email);

			return result;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "error while checking user " + email);
			return false;
		}
	}

	/**
	 * Aggiorna l'ultimo accesso dell'utente
	 * 
	 * @param email
	 */

	public synchronized void updateLastAccess(String email) {
		try {
			Connection con = DataSource.getConnection();
			String query = "UPDATE Utente SET ultimo_accesso = ? WHERE email = ? AND isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);
			p.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			p.setString(2, email);
			p.executeUpdate();
			p.close();
			con.close();
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "error while updating last access of user: " + email);
		}
	}

	/**
	 * Carica un avatar legato all'utente
	 * 
	 * @param email    - indirizzo email dell'utente ({@link User}))
	 * @param filePath - il path dell'immagine da caricare
	 * @return true - caricato con successo <br>
	 *         false - caricamento fallito
	 */
	public synchronized boolean uploadAvatar(String email, String filePath) {
		if (filePath.isBlank() || !existsUser(email))
			return false;

		try {
			Connection con = DataSource.getConnection();
			String query = "UPDATE Utente " + "SET avatar = ? " + "WHERE email=? AND isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);

			FileBlob image = new FileBlob();
			Blob blobImage = image.toBlob(filePath);

			p.setBlob(1, blobImage);
			p.setString(2, email);
			int rs = p.executeUpdate();
			boolean result = rs >= 1;
			p.close();
			con.close();

			return result;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "error while uploading avatar of user: " + email);
			return false;
		}
	}

	/**
	 * Prende l'avatar di un utente e lo converte in {@link FileBlob}
	 * 
	 * @param email - dell'utente
	 * @return {@link FileBlob}
	 */
	public synchronized FileBlob getAvatar(String email) {
		if (!existsUser(email))
			return null;
		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT avatar " + "FROM Utente " + "WHERE email=? AND isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, email);
			ResultSet rs = p.executeQuery();
			Blob avatarBlob = null;

			if (rs.next())
				avatarBlob = rs.getBlob(1);

			FileBlob avatar = new FileBlob(avatarBlob);
			rs.close();
			p.close();
			con.close();

			return avatar;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "error while getting avatar of user: " + email);
			return null;
		}
	}

	/**
	 * Preleva i dati dell'utente che possiede l'email specificata
	 * 
	 * @param email
	 * @return {@link User}
	 */
	public synchronized User getUser(String email) {
		if (email.isBlank())
			return null;

		try {
			Connection con = DataSource.getConnection();
			User user = null;
			String query = "SELECT id_utente,nome, cognome, Ruolo.tipo, ultimo_accesso, avatar, Utente.id_ruolo,telefono FROM Utente, Ruolo WHERE Utente.id_ruolo = Ruolo.id_ruolo AND email = ? AND isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, email);
			ResultSet rs = p.executeQuery();

			if (rs.next()) {
				int idUtente = rs.getInt(1);
				String nome = rs.getString(2);
				String cognome = rs.getString(3);
				String ruolo = rs.getString(4);
				Timestamp ultimoAccesso = rs.getTimestamp(5);
				FileBlob avatar = new FileBlob(rs.getBlob(6));
				int idRuolo = rs.getInt("id_ruolo");
				String telefono = rs.getString("telefono");
				user = new User(nome, cognome, email, avatar, ruolo, ultimoAccesso, idRuolo);
				user.setTelefono(telefono);
				user.setIdUtente(idUtente);
			}

			rs.close();
			p.close();
			con.close();

			return user;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retriving user: " + email);
			return null;
		}
	}

	/**
	 * Preleva i dati di tutti gli utenti.
	 * 
	 * @return una lista di tutti gli utenti ({@link User}) presenti nel DB
	 */
	public synchronized ArrayList<User> getAllUsers() {
		ArrayList<User> utenti = new ArrayList<User>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT id_utente,nome, cognome, email, ultimo_accesso, Ruolo.tipo, avatar, Utente.id_ruolo, telefono FROM Utente, Ruolo WHERE Utente.id_ruolo = Ruolo.id_ruolo AND isVisibile = 1 ORDER BY id_utente";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();

			while (rs.next()) {
				int idUtente = rs.getInt("id_utente");
				String nome = rs.getString("nome");
				String cognome = rs.getString("cognome");
				String ruolo = rs.getString("tipo");
				String email = rs.getString("email");
				Timestamp ultimoAccesso = rs.getTimestamp("ultimo_accesso");
				FileBlob avatar = new FileBlob(rs.getBlob("avatar"));
				int idRuolo = rs.getInt("id_ruolo");
				String telefono = rs.getString("telefono");
				User utente = new User(nome, cognome, email, avatar, ruolo, ultimoAccesso, idRuolo);
				utente.setTelefono(telefono);
				utente.setIdUtente(idUtente);
				utenti.add(utente);
			}
			rs.close();
			p.close();
			con.close();

		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "error while retriving all users");
		}
		return utenti;
	}

	/**
	 * Preleva tutti gli oggetti di tipo {@link Role}
	 * 
	 * @return un HashMap contenente id_ruolo e tipo del ruolo
	 */
	public synchronized HashMap<String, Integer> getAllRoles() {
		if (roles != null)
			return roles;

		roles = new HashMap<String, Integer>();
		try {
			Connection con = DataSource.getConnection();

			String query = "SELECT * FROM Ruolo";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();

			while (rs.next()) {
				Integer idRuolo = rs.getInt("id_ruolo");
				String tipo = rs.getString("tipo");

				roles.put(tipo, idRuolo);
			}
			p.close();
			con.close();

			return roles;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error while getin all roles");
			return roles;
		}

	}

	/**
	 * Aggiunge un nuovo {@link User} nel DB
	 * 
	 * @param user - l'utente da aggiungere nel database
	 * @return true - Aggiunto con successo <br>
	 *         false - Fallimento
	 */
	public synchronized boolean addUser(User user) {
		if (existsUser(user) || user.invalidUser())
			return false;
		String email = user.getEmail();

		try {
			Connection con = DataSource.getConnection();
			String query = "INSERT INTO Utente (nome, cognome, email, telefono, password, id_ruolo, avatar) VALUES(?,?,?,?,?,?,?);";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, user.getNome());
			p.setString(2, user.getCognome());
			p.setString(3, email);
			p.setString(4, user.getTelefono());
			p.setString(5, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12)));
			p.setInt(6, getAllRoles().get(user.getRuolo()));

			// p.setInt(6, user.getIdRuolo());

			String defaultAvatarPath;
			Blob blobImage;

			if (user.getAvatarFileBlob() == null) {
				defaultAvatarPath = getClass().getResource(Settings.DEFAULT_AVATAR_PROJECT_PATH).getPath();
				FileBlob image = new FileBlob();
				blobImage = image.toBlob(defaultAvatarPath);
			} else {
				FileBlob image = new FileBlob(user.getAvatarFileBlob());
				blobImage = image.toBlob();
			}

			p.setBlob(7, blobImage);
			int result = p.executeUpdate();
			p.close();

			/*
			 * String query2 = "INSERT INTO Impostazioni (id_utente) VALUES(?);";
			 * PreparedStatement p2 = con.prepareStatement(query2); p2.setInt(1,
			 * user.getIdUtente()); p2.executeQuery(); p2.close();
			 */
			con.close();

			return result >= 1;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while adding user: " + email);
			return false;
		}
	}

	/**
	 * Rimuove dal database, l'utente specificato
	 * 
	 * @param email - dell'utente ({@link User})
	 * @return true - rimosso con successo <br>
	 *         false - rimozione fallita
	 */
	public synchronized boolean removeUser(String email) {
		try {
			Connection con = DataSource.getConnection();
			if (email.isBlank() || UsersHandler.contains(email))
				return false;

			String query = "UPDATE Utente SET isVisibile = 0 WHERE email = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, email);
			int rs = p.executeUpdate();
			boolean result = rs >= 1;
			p.close();
			con.close();

			return result;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error while deleting " + email + " from database");
			return false;
		}
	}

	/**
	 * Rimuove dal database, il prodotto specificato
	 * 
	 * @param idProdotto - del Prodotto ({@link Product})
	 * @return true - rimosso con successo <br>
	 *         false - rimozione fallita
	 */
	public synchronized boolean removeProduct(int idProdotto) {
		try {
			Connection con = DataSource.getConnection();
			if (idProdotto <= 0)
				return false;

			String query = "UPDATE Prodotto SET isVisibile = 0 WHERE id_prodotto=?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idProdotto);
			int rs = p.executeUpdate();
			boolean result = rs >= 1;
			p.close();
			con.close();
			return result;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e,
					"Error while setting isVisibile to 0 of product with id: " + idProdotto + " from database");
			return false;
		}
	}

	/**
	 * Preleva dal database i dati i tutti i prodotti
	 * 
	 * @param showDeleted <br>
	 *                    <li><b> true </b> - Preleva anche i prodotti dallo
	 *                    storico, che sono stati precedentemente cancellati; utile
	 *                    per controllare prodotti già cancellati ma acquistati in
	 *                    ordini vecchi<br>
	 *                    <li><b> false </b> - Non considerare lo storico dei
	 *                    prodotti
	 * @return una lista di {@link Product}
	 */
	public synchronized ArrayList<Product> getAllProducts() {
		ArrayList<Product> product = new ArrayList<Product>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM Prodotto WHERE isVisibile = 1 AND quantita > 0 ORDER BY id_prodotto";

			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idProdotto = rs.getInt("id_prodotto");
				String nome = rs.getString("nome");
				String descrizione = rs.getString("descrizione");
				Double prezzo = rs.getDouble("prezzo");
				int quantita = rs.getInt("quantita");
				int categoria = rs.getInt("id_categoria");
				FileBlob foto = new FileBlob(rs.getBlob("foto"));
				Category cat = null;
				for (Category c : getAllCategories())
					if (c.getIdCategoria() == categoria) {
						cat = c;
						break;
					}
				Product prod = new Product(idProdotto, nome, descrizione, prezzo, quantita, foto, cat);
				product.add(prod);
			}
			rs.close();
			p.close();
			con.close();

			return product;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retrieving all products from database");
			return product;
		}
	}

	public synchronized ArrayList<Product> getAllMagazzinoProducts(boolean showDeleted) {
		ArrayList<Product> product = new ArrayList<Product>();

		try {
			Connection con = DataSource.getConnection();
			String query;
			if (showDeleted)
				query = "SELECT * FROM Prodotto ORDER BY id_prodotto";
			else
				query = "SELECT * FROM Prodotto WHERE isVisibile = 1 ORDER BY id_prodotto";

			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idProdotto = rs.getInt("id_prodotto");
				String nome = rs.getString("nome");
				String descrizione = rs.getString("descrizione");
				Double prezzo = rs.getDouble("prezzo");
				int quantita = rs.getInt("quantita");
				int categoria = rs.getInt("id_categoria");
				FileBlob foto = new FileBlob(rs.getBlob("foto"));
				Category cat = null;
				for (Category c : getAllCategories())
					if (c.getIdCategoria() == categoria) {
						cat = c;
						break;
					}
				Product prod = new Product(idProdotto, nome, descrizione, prezzo, quantita, foto, cat);
				product.add(prod);
			}
			rs.close();
			p.close();
			con.close();

			return product;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retrieving all products from database");
			return product;
		}
	}

	/**
	 * Preleva dal database i dati di tutti gli stati di ordine
	 * 
	 * @return una lista di {@link OrderState}
	 */
	public synchronized ArrayList<OrderState> getAllOrderState() {
		if (orderState != null)
			return orderState;

		orderState = new ArrayList<OrderState>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM StatoOrdine";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idStato = rs.getInt("id_stato");
				String nome = rs.getString("nome");
				String colore = rs.getString("colore");
				OrderState statusOrdine = new OrderState(idStato, nome, colore);
				orderState.add(statusOrdine);
			}
			rs.close();
			p.close();
			con.close();

			return orderState;
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return orderState;
		}
	}

	/**
	 * Preleva dal database i dati di tutti gli ordini
	 * 
	 * @return una lista di {@link Order}
	 */
	/*
	 * public synchronized ArrayList<Order> getAllOrders() { ArrayList<Order> order
	 * = new ArrayList<Order>();
	 * 
	 * try { Connection con = DataSource.getConnection();
	 * 
	 * ITransaction t = Logger.getInstance().startTransaction("getAllOrders()",
	 * "prendo tutti gli ordini");
	 * 
	 * String query =
	 * "SELECT * FROM Ordine, Utente WHERE Ordine.id_utente=Utente.id_utente AND Ordine.isVisibile = 1;"
	 * ; PreparedStatement p = con.prepareStatement(query); ResultSet rs =
	 * p.executeQuery(); while (rs.next()) { int idOrdine = rs.getInt("id_ordine");
	 * String email = rs.getString("email"); int idIndirizzo =
	 * rs.getInt("id_indirizzo"); Timestamp dataOrdine =
	 * rs.getTimestamp("data_ordine"); int idStatoOrdine = rs.getInt("id_stato");
	 * String descrizioneStatoOrdine = rs.getString("stato_ordine"); Double
	 * prezzoTotale = rs.getDouble("prezzo_totale");
	 * 
	 * Address indirizzo = null;
	 * 
	 * for (Address i : getAllAddress()) if (i.getIdIndirizzo() == idIndirizzo) {
	 * indirizzo = i; break; }
	 * 
	 * User user = getUser(email);
	 * 
	 * OrderState orderStatus = null;
	 * 
	 * for (OrderState o : getAllOrderState()) if (o.getIdStato() == idStatoOrdine)
	 * { orderStatus = o; break; }
	 * 
	 * Order ordine = new Order(idOrdine, user, indirizzo, dataOrdine, prezzoTotale,
	 * orderStatus, descrizioneStatoOrdine); order.add(ordine); } rs.close();
	 * p.close(); con.close();
	 * 
	 * Logger.getInstance().closeTransaction(t); return order; } catch (Exception e)
	 * { Logger.getInstance().captureException(e); return order; } }
	 */

	/**
	 * Preleva dal database i dati di tutti i prodotti ordinati
	 * 
	 * @return una lista di {@link Product}
	 */
	public synchronized ArrayList<OrderProduct> getAllOrderProducts() {
		ArrayList<OrderProduct> orderProduct = new ArrayList<OrderProduct>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM OrdineProdotti WHERE isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idOrdineProdotti = rs.getInt("id_ordine_prodotti");
				int idOrdine = rs.getInt("id_ordine");
				int idProdotto = rs.getInt("id_prodotto");
				int quantita = rs.getInt("quantita");

				Order order = null;
				for (Order o : getAllOrders())
					if (o.getIdOrdine() == idOrdine) {
						order = o;
						break;
					}

				Product product = null;

				for (Product prod : getAllMagazzinoProducts(true))
					if (prod.getIdProdotto() == idProdotto) {
						product = prod;
						break;
					}

				OrderProduct orderProd = new OrderProduct(idOrdineProdotti, order, product, quantita);
				orderProduct.add(orderProd);
			}
			rs.close();
			p.close();
			con.close();

			return orderProduct;
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return orderProduct;
		}
	}

	/**
	 * Preleva dal database i dati di tutti gli indirizzi
	 * 
	 * @return una lista di {@link Address}
	 */
	private ArrayList<Address> getAllAddress() {
		ArrayList<Address> address = new ArrayList<Address>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM Indirizzo";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idIndirizzo = rs.getInt("id_indirizzo");
				String nomeVia = rs.getString("nome_via");
				String CAP = rs.getString("CAP");
				int numCivico = rs.getInt("num_civico");
				String citta = rs.getString("citta");
				String nazione = rs.getString("nazione");
				String regione = rs.getString("regione");

				Address addr = new Address(idIndirizzo, nomeVia, CAP, numCivico, nazione, regione, citta);
				address.add(addr);
			}
			rs.close();
			p.close();
			con.close();

			return address;
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return address;
		}
	}

	/**
	 * Modifica i dati dell'utente specificato. La passowrd viene moificata solo se
	 * NON vuota.
	 * 
	 * @param user     - I dati dell'utente già modificati da cambiare sul database.
	 * @param password - la password nuova dell'utente ({@link User}. Viene
	 *                 modificato solo se NON vuota.
	 * @return true - modifiche avvenute con successo <br>
	 *         false - fallimento modifiche
	 */
	public synchronized boolean editUser(User user, String password) {
		if (!existsUser(user.getEmail()) || user.invalidUser())
			return false;

		int idUtente = user.getIdUtente();

		try {
			Connection con = DataSource.getConnection();
			String query = "UPDATE Utente SET nome = ?, cognome = ?, telefono = ?, id_ruolo = ?, avatar = ? WHERE id_utente = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, user.getNome());
			p.setString(2, user.getCognome());
			p.setString(3, user.getTelefono());
			p.setInt(4, getAllRoles().get(user.getRuolo()));

			FileBlob image = new FileBlob(user.getAvatarFileBlob());
			Blob blobImage = image.toBlob();
			p.setBlob(5, blobImage);
			p.setInt(6, idUtente);

			int result = p.executeUpdate();
			p.close();
			con.close();
			if (!password.isBlank())
				updatePassword(idUtente, password);

			return result >= 1;
		} catch (Exception e) {
			e.getStackTrace();
			Logger.getInstance().captureException(e, "error while editing user: " + idUtente);
			return false;
		}
	}

	/**
	 * Modifica la password dell'utente specificato e aggiorna il database
	 * 
	 * @param email    - Appartenente all'utente per il quale si vuole cambiare la
	 *                 password.
	 * @param password - la password nuova
	 * @return
	 */

	public synchronized boolean updatePassword(int idUtente, String password) {

		try {
			Connection con = DataSource.getConnection();
			String query = "UPDATE Utente SET password = ? WHERE id_utente = ? AND isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, BCrypt.hashpw(password, BCrypt.gensalt(12)));
			p.setInt(2, idUtente);
			p.executeUpdate();
			p.close();
			con.close();
			return true;
		} catch (Exception e) {
			e.getStackTrace();
			Logger.getInstance().captureException(e, "error while editing password for user: " + idUtente);
			return false;
		}
	}

	/**
	 * Preleva dal database i dati di tutte le categorie
	 * 
	 * @return una lista di {@link Category}
	 */
	public synchronized ArrayList<Category> getAllCategories() {
		ArrayList<Category> categories = new ArrayList<Category>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM Categoria WHERE isVisibile = 1 ORDER BY id_categoria";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idCategoria = rs.getInt("id_categoria");
				String nome = rs.getString("nome");
				String descrizione = rs.getString("descrizione");
				int idParent = rs.getInt("id_parent");
				Category category = new Category(idCategoria, nome, descrizione, idParent);
				categories.add(category);
			}
			rs.close();
			p.close();
			con.close();

			return categories;
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return categories;
		}
	}

	/**
	 * Inserisce una nuova categoria nel database
	 * 
	 * @param category - la categoria da inserire nel database
	 * @return true - inserita con successo <br>
	 *         false - fallimento
	 */
	public synchronized boolean insertCategory(Category category) {
		try {
			Connection con = DataSource.getConnection();

			String query = "INSERT INTO Categoria (nome, descrizione, id_parent) VALUES(?,?,?);";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, category.getNome());
			p.setString(2, category.getDescrizione());
			int idParent = category.getIdParentCategoria();
			p.setInt(3, idParent);
			int result = p.executeUpdate();
			p.close();
			con.close();
			return result >= 1;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while adding category: " + category.getNome());
			return false;
		}
	}

	/**
	 * Cancella la categoria di prodotto "category". <br>
	 * Trasformare le sottocategorie di primo livello in Categorie Genitore) <br>
	 * Imposta i prodotti appartenenti a quella categoria alla categoria superore o
	 * "Senza Categoria"
	 * 
	 * @param category - La categoria da cancellare
	 * @return true - categoria rimossa con successo <br>
	 *         false - fallimento durante la rimozione
	 */
	public synchronized boolean removeCategory(Category category) {
		try {
			Connection con = DataSource.getConnection();
			int idCategory = category.getIdCategoria();
			if (idCategory == 1) // se idCategory == 1, allora è "Senza Categoria"
				return false;

			con.setAutoCommit(false);

			String query2 = "UPDATE `Categoria` SET id_parent = ? WHERE id_parent = ?;";
			PreparedStatement p2 = con.prepareStatement(query2);
			p2.setInt(1, category.getIdParentCategoria());
			p2.setInt(2, idCategory);
			p2.executeUpdate();
			p2.close();

			String query3 = "UPDATE Prodotto SET id_categoria = ? WHERE id_categoria = ?;";
			PreparedStatement p3 = con.prepareStatement(query3);
			p3.setInt(1, category.getIdParentCategoria());
			p3.setInt(2, idCategory);
			p3.executeUpdate();
			p3.close();

			String query4 = "UPDATE Categoria SET isVisibile = 0 WHERE id_categoria = ?;";
			PreparedStatement p4 = con.prepareStatement(query4);
			p4.setInt(1, idCategory);
			p4.executeUpdate();
			p4.close();

			con.commit();
			con.close();

			return true;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error while removing category " + category.getIdCategoria());
			try {
				Connection con = DataSource.getConnection();
				con.rollback();
				con.close();
				Logger.getInstance().captureMessage("Rollback eseguito");
			} catch (SQLException e1) {
				Logger.getInstance().captureException(e1, "Error while rollbacking");
			}
		}

		return false;
	}

	/**
	 * Modifica l'ordine e aggiornalo nel database.
	 * 
	 * @param stato       - il nuovo stato dell'ordine
	 * @param descrizione - dettagi relativi all'ordine / allo stato dell'ordine
	 * @param idOrdine    - il codice univoco dell'ordine ({@link Order})
	 * @return true - modifica avvenuta con successo <br>
	 *         false - fallimento durante la modifica
	 */
	public synchronized boolean editOrder(OrderState stato, String descrizione, int idOrdine, Address indirizzo) {
		Connection con = DataSource.getConnection();
		try {
			con.setAutoCommit(false);

			String query = "UPDATE Ordine SET id_stato = ?, stato_ordine = ? WHERE id_ordine = ?;";

			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, stato.getIdStato());
			p.setString(2, descrizione);
			p.setInt(3, idOrdine);
			int result = p.executeUpdate();
			p.close();
			if (!indirizzo.equals(null)) {
				String query2 = "UPDATE Indirizzo SET nome_via = ?, CAP = ?, num_civico = ?, citta = ?, nazione = ?, regione = ? WHERE id_indirizzo = ?";
				PreparedStatement p2 = con.prepareStatement(query2);
				p2.setString(1, indirizzo.getNome_via());
				p2.setString(2, indirizzo.getCAP());
				p2.setInt(3, indirizzo.getNum_civico());
				p2.setString(4, indirizzo.getCitta());
				p2.setString(5, indirizzo.getNazione());
				p2.setString(6, indirizzo.getRegione());
				p2.setInt(7, indirizzo.getIdIndirizzo());
				result = p2.executeUpdate();
				p2.close();
			}
			con.commit();
			con.close();
			return result >= 1;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while editing order #" + idOrdine);
			try {
				con.rollback();
				Logger.getInstance().captureMessage("Rollback succesfull for order: " + idOrdine);
				con.close();
				return false;
			} catch (SQLException e1) {
				Logger.getInstance().captureException(e,
						"Error while rollbacking changes of ordee with id: " + idOrdine);
				return false;
			}
		}
	}

	public synchronized ArrayList<OrderProduct> getOrderProductsId(int idOrdine) {
		ArrayList<OrderProduct> orderProduct = new ArrayList<OrderProduct>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM OrdineProdotti WHERE id_ordine = ?";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idOrdine);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idOrdineProdotti = rs.getInt("id_ordine_prodotti");
				int idProdotto = rs.getInt("id_prodotto");
				int quantita = rs.getInt("quantita");

				Order order = null;
				for (Order o : getAllOrders())
					if (o.getIdOrdine() == idOrdine) {
						order = o;
						break;
					}

				Product product = null;

				for (Product prod : getAllMagazzinoProducts(true))
					if (prod.getIdProdotto() == idProdotto) {
						product = prod;
						break;
					}

				OrderProduct orderProd = new OrderProduct(idOrdineProdotti, order, product, quantita);
				orderProduct.add(orderProd);
			}

			rs.close();
			p.close();
			con.close();

			return orderProduct;
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return orderProduct;
		}
	}

	public synchronized boolean editCategory(Category category) {

		try {
			Connection con = DataSource.getConnection();
			String query = "UPDATE Categoria SET nome = ?, descrizione = ?, id_parent = ? WHERE id_categoria = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, category.getNome());
			p.setString(2, category.getDescrizione());
			int idParent = category.getIdParentCategoria();

			p.setInt(3, idParent);
			p.setInt(4, category.getIdCategoria());
			int result = p.executeUpdate();
			p.close();
			con.close();

			return result >= 1;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while editing category: " + category.getNome());
			return false;
		}
	}

	/**
	 * Verifica se la Categoria esiste già. Il confronto è Case INSENSITIVIE
	 * 
	 * @param cat - la categoria da controllare se esiste
	 * @return true - la categoria esiste <br>
	 *         false - la categoria NON esiste
	 */
	public synchronized boolean existsCategory(Category cat) {
		try {
			Connection con = DataSource.getConnection();

			String nomeCategoria = cat.getNome();
			String query = "SELECT nome FROM Categoria WHERE isVisibile=1 AND UPPER(Categoria.nome) = UPPER( ? ) AND NOT id_categoria = ?";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, nomeCategoria);
			p.setInt(2, cat.getIdCategoria());
			ResultSet rs = p.executeQuery();
			boolean result = rs.next(); // se true, allora la categoria esiste
			p.close();
			con.close();
			return result;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error while checking if Category: " + cat.getNome() + " exists.");
			return false;
		}
	}

	/**
	 * Aggiunge un nuovo prodotto nel database
	 * 
	 * @param product - il prodotto da inserire nel database
	 * @return true - inserito con successo <br>
	 *         false - fallimento
	 */
	public synchronized boolean addProduct(Product product) {

		try {
			Connection con = DataSource.getConnection();
			String query = "INSERT INTO Prodotto (nome,descrizione,prezzo,quantita,id_categoria,foto) VALUES(?,?,?,?,?,?);";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, product.getNome());
			p.setString(2, product.getDescrizione());
			p.setDouble(3, product.getPrezzo());
			p.setInt(4, product.getQuantita());
			p.setInt(5, product.getCategoria().getIdCategoria());

			String defaultPhotoPath;
			Blob blobImage;

			if (product.getFotoFileBlob() == null) {
				defaultPhotoPath = getClass().getResource(Settings.DEFAULT_PRODUCT_PHOTO_PATH).getPath();
				FileBlob image = new FileBlob();
				blobImage = image.toBlob(defaultPhotoPath);
			} else {
				FileBlob image = new FileBlob(product.getFotoFileBlob());
				blobImage = image.toBlob();
			}
			p.setBlob(6, blobImage);
			int result = p.executeUpdate();
			p.close();
			con.close();
			return result >= 1;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while adding product: " + product.getNome());
			return false;
		}
	}

	/**
	 * Modifica il prodotto e lo aggirna nel database. Se qualche utente ha
	 * cancellato il prodotto prima dell'apportazione della modifica. Il prodotto
	 * viene ripristinato e le modifiche vengono apportate comunque.
	 * 
	 * @param product - il prodotto
	 * @return true - modifica avvenuta con successo <br>
	 *         false - fallimento durante la modifica
	 */
	public synchronized boolean editProduct(Product product) {

		try {
			Connection con = DataSource.getConnection();
			String query = "UPDATE Prodotto SET isVisibile = 1, nome = ?, descrizione = ?, prezzo = ?, quantita = ?, id_categoria = ?, foto = ? WHERE id_prodotto = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, product.getNome());
			p.setString(2, product.getDescrizione());
			p.setDouble(3, product.getPrezzo());
			p.setInt(4, product.getQuantita());
			p.setInt(5, product.getCategoria().getIdCategoria());
			p.setBlob(6, product.getFotoFileBlob().toBlob());
			p.setInt(7, product.getIdProdotto());
			int result = p.executeUpdate();
			p.close();
			con.close();
			return result >= 1;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while editing product: [" + product.getIdProdotto() + "] " + product.getNome());
			return false;
		}
	}

	/**
	 * Prende dal Serve la configurazione più aggiornata
	 * 
	 * @return
	 */
	public synchronized Configurazione getConfig() {
		return configurazione;
	}

	/**
	 * Prende dal DB, l'ultima configurazione impostata per il negozio
	 * 
	 * @return true - preliveo dati avvenuto con successo <br>
	 *         false - fallimento durante il prelievo
	 */
	private Configurazione initConfig() {

		Configurazione config = null;
		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT id_config,titolo_negozio,separator_negozio,server_posta,numero_porta,nome_accesso,password_posta FROM Configurazione ORDER BY id_config DESC LIMIT 1;";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();

			if (rs.next()) {
				int idConfig = rs.getInt(1);
				String titoloNegozio = rs.getString(2);
				String separatorNegozio = rs.getString(3);
				String serverPosta = rs.getString(4);
				int numPorta = rs.getInt(5);
				String emailAccesso = rs.getString(6);
				String password = rs.getString(7);
				config = new Configurazione(idConfig, titoloNegozio, separatorNegozio, serverPosta, numPorta,
						emailAccesso, password);
			}
			rs.close();
			p.close();
			con.close();
			return config;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retriving configuration");
			return null;
		}
	}

	/**
	 * Modifica la configurazione. Aggiunge nel DB la nuova configurazione (tenendo
	 * come storico le precedenti configurazioni). <br>
	 * Aggiorna i dati dell'ultima configurazione, nel server, per un accesso più
	 * veloce ai dati.
	 * 
	 * @param product - il prodotto
	 * @return true - modifica avvenuta con successo <br>
	 *         false - fallimento durante la modifica
	 */
	public synchronized boolean editConfig(Configurazione configurazione) {

		try {
			Connection con = DataSource.getConnection();
			String query = "UPDATE Configurazione SET titolo_negozio = ?, separator_negozio = ?, server_posta = ?, numero_porta = ?, nome_accesso = ?";

			if (!configurazione.getPasswordPosta().isBlank())
				query += ",password_posta = ?";

			query += " WHERE id_config = ? ;";

			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, configurazione.getTitoloNegozio());
			p.setString(2, configurazione.getSeparatorNegozio());
			p.setString(3, configurazione.getServerPosta());
			p.setInt(4, configurazione.getNumPorta());
			p.setString(5, configurazione.getEmailPosta());

			if (!configurazione.getPasswordPosta().isBlank()) {
				p.setString(6, configurazione.getPasswordPosta());
				p.setInt(7, configurazione.getIdConfig());
			} else
				p.setInt(6, configurazione.getIdConfig());

			int result = p.executeUpdate();
			p.close();
			con.close();

			this.configurazione = configurazione;

			return result >= 1;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while editing configurazione: " + configurazione.getIdConfig());
			return false;
		}
	}

	/**
	 * Verifica se le credenziali inserite in fase di login corrispondono con quelle
	 * archiviate nel server.
	 * 
	 * @param id
	 * @param password
	 * @return true - se le credenziali coincidono <br>
	 *         false - altrimenti
	 */
	public synchronized boolean checkUserPassword(String id, String password) {

		int idUtente = Integer.parseInt(id);
		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM Utente WHERE id_utente = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idUtente);
			ResultSet rs = p.executeQuery();

			boolean result = false;
			if (rs.next()) {
				String passwordDatabase = rs.getString("password");
				result = BCrypt.checkpw(password, passwordDatabase);
			}
			rs.close();
			p.close();
			con.close();
			return result;

		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while checking user password: " + idUtente);
			return false;
		}
	}

	/**
	 * Metodo che setta una nuova password nel database e la invia tramite email
	 * all'utente, previa verifica che lo stesso sia già registrato
	 * 
	 * @param emailRecovery
	 * @return true - se la password è stata reimpostata con successo <br>
	 *         false - altrimenti
	 */

	public synchronized boolean passwordRecovery(String emailRecovery) {
		if (!existsUser(emailRecovery))
			return false;

		User user = getUser(emailRecovery);
		String randomPassword = PasswordGenerator.generatePassword(10);
		if (updatePassword(user.getIdUtente(), randomPassword)) {
			Email emailBody = new Email("Passowrd Cambiata", "La tua nuova password e':\n" + randomPassword,
					emailRecovery);
			return EmailSender.getInstance().sendEmail(emailBody);
		}

		return false;
	}

	/**
	 * Rimuove l'ordine con corrispondente all'id passato come parametro
	 * 
	 * @param idOrdine
	 * @return true - se l'ordine viene rimosso con successo <br>
	 *         false - se ci sono eventuali problemi durante la rimozione
	 */

	public synchronized boolean removeOrder(int idOrdine) {
		try {
			Connection con = DataSource.getConnection();

			con.setAutoCommit(false);

			String query = "UPDATE OrdineProdotti SET isVisibile = 0 WHERE id_ordine = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idOrdine);
			p.executeUpdate();
			p.close();

			String query2 = "UPDATE Ordine SET isVisibile = 0 WHERE id_ordine = ?;";
			PreparedStatement p2 = con.prepareStatement(query2);
			p2.setInt(1, idOrdine);
			p2.executeUpdate();
			p2.close();

			con.commit();
			con.close();
			return true;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error while removing order #" + idOrdine);
			try {
				Connection con = DataSource.getConnection();
				con.rollback();
				con.close();
				Logger.getInstance().captureMessage("Rollback cancellazione ordine eseguito");
			} catch (SQLException e1) {
				Logger.getInstance().captureException(e1, "Error while rollbacking");
			}
		}

		return false;
	}

	/**
	 * Restituisce le preferenze di tema dell'utente che sono archiviate nel
	 * Database
	 * 
	 * @param idUtente
	 * @return LIGHT - se il tema è quello CHIARO <br>
	 *         DARK - se il tema è settato a SCRURO <br>
	 *         AUTO - Determina in automatico in base alla preferenza del sistema
	 */

	public synchronized String getTheme(int idUtente) {
		String theme = "LIGHT";

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM Impostazioni WHERE id_utente = ? LIMIT 1;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idUtente);
			ResultSet rs = p.executeQuery();

			if (rs.next())
				theme = rs.getString("Theme");

			rs.close();
			p.close();
			con.close();
			return theme;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retriving theme for user " + idUtente);
			return theme;
		}
	}

	public synchronized boolean changeTheme(int idUtente, String theme) {

		try {
			Connection con = DataSource.getConnection();
			String query = "UPDATE Impostazioni SET Theme = ? WHERE id_utente = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setString(1, theme);
			p.setInt(2, idUtente);
			p.executeUpdate();
			p.close();
			con.close();
			return true;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while changing theme for user " + idUtente);
			return false;
		}
	}

	/**
	 * Calcola statistiche raggruppate per data
	 * 
	 * @return Restituisce un'Hashmap con chiave la data e come key la somma dei
	 *         prodotti venduti in quella data
	 */

	public synchronized HashMap<Date, Double> sendStatsEntrance() {
		HashMap<Date, Double> data = new HashMap<Date, Double>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT SUM(prezzo_totale), data_ordine FROM Ordine GROUP BY CAST(data_ordine AS DATE) ORDER BY data_ordine ASC;";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				Double total = rs.getDouble(1);
				Timestamp d = rs.getTimestamp("data_ordine");
				Date date = new Date(d.getTime());
				data.put(date, total);
			}
			rs.close();
			p.close();
			con.close();
			return data;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retriving entrance stats");
			return data;
		}
	}

	/**
	 * Calcola tutti i prodotti Visibili con quantità almeno minQuantity
	 * 
	 * @param minQuantity - la quanità minima
	 * @return numero di prodotti presenti sul database
	 */

	private synchronized int getAllNumProducts(int minQuantity) {
		int num = -1;

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT COUNT(*) FROM Prodotto WHERE isVisibile = 1 AND quantita >= ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, minQuantity);
			ResultSet rs = p.executeQuery();
			if (rs.next())
				num = rs.getInt(1);

			rs.close();
			p.close();
			con.close();
			return num;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while getting count of all visible products");
			return num;
		}
	}

	/**
	 * Prende dal DB tutti i prodotti che hanno una quantità uguale o inferiore a
	 * maxQuantity. <br>
	 * Se <i>maxQuantity > 0</i> allora i prodotti con quantità = 0 non verranno
	 * considerati. <br>
	 * Se <i>maxQuantity = 0</i> allora solo i prodotti con quantità = 0 verranno
	 * considerati
	 * 
	 * @param maxQuantity - il numero di quantità massimo che può avere il prodotto
	 * @return
	 */
	private synchronized int getNumProducts(int maxQuantity) {
		int num = -1;

		try {
			Connection con = DataSource.getConnection();
			String query = "";

			if (maxQuantity != 0)
				query = "SELECT COUNT(*) FROM Prodotto WHERE isVisibile = 1 AND quantita <= ? AND quantita > 0;";
			else
				query = "SELECT COUNT(*) FROM Prodotto WHERE isVisibile = 1 AND quantita <= ?;";

			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, maxQuantity);
			ResultSet rs = p.executeQuery();
			if (rs.next())
				num = rs.getInt(1);

			rs.close();
			p.close();
			con.close();
			return num;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while getting count of visible products with maxQuantity: " + maxQuantity);
			return num;
		}
	}

	/**
	 * Prende dal DB il guadagno totlae da tutti gli ordini visibili (non
	 * cancellati), che sono stati completati nel mese specificato
	 * 
	 * @param month
	 * @param year
	 * @return guadagno totale
	 */
	private synchronized double getEarnings(int month, int year) {
		double somma = -1;

		try {
			Connection con = DataSource.getConnection();
			// La somma di tutti i totali (prezzoTotale) degli ordini completati nel mese
			// specificato
			String query = "SELECT SUM(prezzo_totale) FROM Ordine, StatoOrdine WHERE EXTRACT(MONTH FROM data_ordine) = ? "
					+ "AND EXTRACT(YEAR FROM data_ordine) = ? AND StatoOrdine.id_stato = Ordine.id_stato AND "
					+ "StatoOrdine.nome = 'completato' AND isVisibile = 1";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, month);
			p.setInt(2, year);
			ResultSet rs = p.executeQuery();
			if (rs.next())
				somma = rs.getDouble(1);

			rs.close();
			p.close();
			con.close();
			return somma;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while getting earnings of all orders in month: " + month + " and year: " + year);
			return somma;
		}

	}

	/**
	 * Prende dal DB il num di tutte le categorie visibii
	 * 
	 * @return conteggio delle categorie
	 */
	private synchronized int getNumCategories() {
		int num = -1;

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT COUNT(*) FROM Categoria WHERE isVisibile = 1;";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			if (rs.next())
				num = rs.getInt(1);

			rs.close();
			p.close();
			con.close();
			return num;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while getting count of visible categories");
			return num;
		}
	}

	/**
	 * Prende dal DB il numero di tutti gli ordini per mese specificato e stato
	 * dell'ordine specificato
	 * 
	 * @param month
	 * @param year
	 * @param state - stato dell'ordine
	 * @return conteggio degli ordini per le condizioni specificate
	 */
	private synchronized int getNumOrders(int month, int year, OrderState state, int idUtente) {
		int num = -1;

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT COUNT(*) FROM Ordine WHERE isVisibile = 1 AND id_stato = ? AND EXTRACT(MONTH FROM data_ordine) = ? AND EXTRACT(YEAR FROM data_ordine) = ? ";
			if (idUtente != 0)
				query += "AND id_utente = ?";
			query += ";";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, state.getIdStato());
			p.setInt(2, month);
			p.setInt(3, year);
			if (idUtente != 0)
				p.setInt(4, idUtente);
			ResultSet rs = p.executeQuery();
			if (rs.next())
				num = rs.getInt(1);

			rs.close();
			p.close();
			con.close();
			return num;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while getting count of all orders in month: " + month
					+ " and year: " + year + " of state: " + state.getNome());
			return num;
		}
	}

	/**
	 * Prende dal database il numero di utenti registrati con ruolo "Cliente"
	 * 
	 * @return numero di Clienti Registrati
	 */

	private synchronized int getAllNumClients() {
		int num = -1;
		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT COUNT(*) FROM Utente WHERE isVisibile = 1 AND id_ruolo = 1;";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			if (rs.next())
				num = rs.getInt(1);

			rs.close();
			p.close();
			con.close();
			return num;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while getting count of all clients (customers)");
			return num;
		}
	}

	/**
	 * Prende il numero di ordini con stato passato per argomento
	 * 
	 * @param state
	 * @return numero di ordini con stato <b>state</b>
	 */

	private synchronized int getNumOrders(OrderState state) {
		int num = -1;

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT COUNT(*) FROM Ordine WHERE isVisibile = 1 AND id_stato = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, state.getIdStato());
			ResultSet rs = p.executeQuery();
			if (rs.next())
				num = rs.getInt(1);

			rs.close();
			p.close();
			con.close();
			return num;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while getting count of all orders with state: " + state.getNome());
			return num;
		}
	}

	/**
	 * Prende dal DB il valore speso per gli ordini nel mese e anno specificato
	 * specificato
	 * 
	 * @param idUtente - id dell'Utente
	 * @param month    - mese per la quale si vuola ricavare la spesa
	 * @param year     - anno per la quale si vuola ricavare la spesa
	 * @return numero della spesa effettuata nel periodo prescelto
	 */

	private synchronized double getSpentOrders(int idUtente, int month, int year) {
		double num = -1;

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT SUM(prezzo_totale) FROM Ordine WHERE isVisibile = 1 AND id_utente = ? AND EXTRACT(MONTH FROM data_ordine) = ? AND EXTRACT(YEAR FROM data_ordine) = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idUtente);
			p.setInt(2, month);
			p.setInt(3, year);
			ResultSet rs = p.executeQuery();
			if (rs.next())
				num = rs.getDouble(1);
			rs.close();
			p.close();
			con.close();
			return num;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while getting spent info about user: " + idUtente);
			return num;
		}
	}

	/**
	 * Effettua il calcolo delle varie statistiche relative ai Clienti
	 * 
	 * @param idUtente
	 * @param month
	 * @param year
	 * @return statistiche riguardanti l'utente Specifico
	 */

	public synchronized DashboardData getDashboardDataCliente(int idUtente, int month, int year) {
		DashboardData dash = new DashboardData();

		// count totale spese degli ordini nel periodo "month - year"
		double spent = getSpentOrders(idUtente, month, year);
		dash.setSpent(spent);

		// count numero totale di ordine effettuati nel periodo "month - year"
		int numOrders = getNumOrders(month, year, idUtente);
		dash.setNumOrders(numOrders);

		// numero ordini in lavorazione per il l'utente corrente
		int numOrderLavorazione = getNumOrders(month, year, getState("in lavorazione"), idUtente);
		dash.setNumOrderLavorazione(numOrderLavorazione);

		// numero ordini completati per l'utente corrente
		int numOrderCompletati = getNumOrders(month, year, getState("completato"), idUtente);
		dash.setNumOrderCompletati(numOrderCompletati);

		// count di tutti i prodotti in esaurimento
		int numProdottiInEsaurimento = getNumProducts(dash.getNumOrdersToRequest());
		dash.setProductsEsaurimento(numProdottiInEsaurimento);

		// count numero di tutti i prodotti
		int numProducts = getAllNumProducts(1);
		dash.setNumProductsAll(numProducts);

		return dash;
	}

	/**
	 * Prende dal database gli ordini effettuati nel mese ed anno specificati per
	 * l'utente corrente
	 * 
	 * @param month
	 * @param year
	 * @param idUtente
	 * @return numero di ordini del periodo scelto
	 */

	private synchronized int getNumOrders(int month, int year, int idUtente) {
		int num = -1;

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT COUNT(*) FROM Ordine WHERE isVisibile = 1 AND EXTRACT(MONTH FROM data_ordine) = ? AND EXTRACT(YEAR FROM data_ordine) = ? AND id_utente = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, month);
			p.setInt(2, year);
			p.setInt(3, idUtente);
			ResultSet rs = p.executeQuery();
			if (rs.next())
				num = rs.getInt(1);

			rs.close();
			p.close();
			con.close();
			return num;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while getting count of all orders in month: " + month
					+ " and year: " + year + " for user: " + idUtente);
			return num;
		}
	}

	/**
	 * Prende dal database gli ordini effettuati nel mese ed anno specificati per lo
	 * Shop Manager
	 * 
	 * @param month
	 * @param year
	 * @return statistiche varie l'utente con ruolo Shop Manager
	 */
	public synchronized DashboardData getDashboardData(int month, int year) {
		DashboardData dash = new DashboardData();

		// count numero di tutto il guadagno nel periodo "month - year"
		double earnings = getEarnings(month, year);
		dash.setEarnings(earnings);

		// count numero di tutti i prodotti
		int numProducts = getAllNumProducts(0);
		dash.setNumProductsAll(numProducts);

		// count ordini completati nel periodo "month - year"
		int numCompletedOrders = getNumOrders(month, year, getState("completato"), 0);
		dash.setNumCompletedOrders(numCompletedOrders);

		// count ordini in lavorazione
		int numOrdersInLavorazione = getNumOrders(getState("in lavorazione"));
		dash.setNumOrdersInLavorazione(numOrdersInLavorazione);

		// count ordini in sospeso
		int numOrdersInSospeso = getNumOrders(month, year, getState("in sospeso"), 0);
		dash.setNumOrdersInSospeso(numOrdersInSospeso);

		// count di tutte le categorie
		int numCategories = getNumCategories();
		dash.setNumCategories(numCategories);

		// count di tutti i prodotti in esaurimento
		int numProductsEsaurimento = getNumProducts(dash.getNumOrdersToRequest());
		dash.setProductsEsaurimento(numProductsEsaurimento);

		// count di tutti i prodotti esauriti
		int numProdottiEsauriti = getNumProducts(0);
		dash.setProductsEsauriti(numProdottiEsauriti);

		// count tutti gli utenti online
		int numUsersOnline = UsersHandler.getNumOnlineUsers();
		dash.setUsersOnline(numUsersOnline);

		// counti di tutti i clienti
		int allClients = getAllNumClients();
		dash.setAllClients(allClients);

		return dash;
	}

	/**
	 * Prende l'oggetto OrderState ("Stato Ordine")
	 * 
	 * @param nameState - il nome dello stato che viene cercato, case INSENSITIVE
	 *                  <br>
	 *                  list - la lista di tutti gli stati
	 * @return OrderState
	 */
	private synchronized OrderState getState(String nameState) {
		for (OrderState o : orderState)
			if (o.getNome().equalsIgnoreCase(nameState))
				return o;
		return null;
	}

	public synchronized HashMap<String, Integer> sendStatsBestSellers() {
		HashMap<String, Integer> product = new HashMap<String, Integer>();

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT Prodotto.nome, SUM(OrdineProdotti.quantita) AS bs FROM Ordine, OrdineProdotti, Prodotto WHERE Ordine.id_ordine = OrdineProdotti.id_ordine AND OrdineProdotti.id_prodotto = Prodotto.id_prodotto GROUP BY Prodotto.id_prodotto ORDER BY bs DESC LIMIT 5;";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();

			while (rs.next()) {
				String nomeProdotto = rs.getString(1);
				int num = rs.getInt(2);
				product.put(nomeProdotto, num);
			}
			rs.close();
			p.close();
			con.close();
			return product;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while sending best seller stats");
			return product;
		}

	}

	/**
	 * Prende dal database tutti gli ordini
	 * 
	 * @param userId
	 * @return ArrayList di ordini
	 */

	public synchronized ArrayList<Order> getAllOrders() {
		ArrayList<Order> order = new ArrayList<Order>();

		try {
			Connection con = DataSource.getConnection();

			String query = "SELECT Ordine.id_ordine, Indirizzo.id_indirizzo, Indirizzo.nome_via, "
					+ "Indirizzo.CAP, Indirizzo.num_civico, Indirizzo.nazione, Indirizzo.regione, Indirizzo.citta, "
					+ "Ordine.data_ordine, Ordine.prezzo_totale, StatoOrdine.id_stato, StatoOrdine.nome, Ordine.stato_ordine, "
					+ "StatoOrdine.colore, Utente.id_utente, Utente.nome AS nomeUtente, Utente.cognome, Utente.email, "
					+ "Utente.telefono, Utente.ultimo_accesso, Utente.id_ruolo, Utente.avatar, Ruolo.tipo FROM Ordine, Utente, "
					+ "Indirizzo, StatoOrdine, Ruolo WHERE Ordine.id_utente = Utente.id_utente AND Ordine.id_indirizzo = Indirizzo.id_indirizzo "
					+ "AND StatoOrdine.id_stato = Ordine.id_stato AND Ruolo.id_ruolo = Utente.id_ruolo AND Ordine.isVisibile = 1";
			PreparedStatement p = con.prepareStatement(query);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idOrdine = rs.getInt("id_ordine");
				int idIndirizzo = rs.getInt("id_indirizzo");
				String nomeVia = rs.getString("nome_via");
				String CAP = rs.getString("CAP");
				int numCivico = rs.getInt("num_civico");
				String nazione = rs.getString("nazione");
				String regione = rs.getString("regione");
				String citta = rs.getString("citta");
				Timestamp dataOrdine = rs.getTimestamp("data_ordine");
				Double prezzoTotale = rs.getDouble("prezzo_totale");
				int idStatoOrdine = rs.getInt("id_stato");
				String nomeStato = rs.getString("nome");
				String descrizioneStatoOrdine = rs.getString("stato_ordine");
				String colore = rs.getString("colore");

				int idUtente = rs.getInt("id_utente");
				String nomeUtente = rs.getString("nomeUtente");
				String cognomeUtente = rs.getString("cognome");
				String emailUtente = rs.getString("email");
				String telefonoUtente = rs.getString("telefono");
				Timestamp ultimoAccessoUtente = rs.getTimestamp("ultimo_accesso");
				int idRuolo = rs.getInt("id_ruolo");
				FileBlob avatar = new FileBlob(rs.getBlob("avatar"));
				String tipoRuolo = rs.getString("tipo");

				User user = new User(nomeUtente, cognomeUtente, emailUtente, avatar, tipoRuolo, ultimoAccessoUtente,
						idRuolo);
				user.setTelefono(telefonoUtente);
				user.setIdUtente(idUtente);

				Address indirizzo = new Address(idIndirizzo, nomeVia, CAP, numCivico, nazione, regione, citta);
				OrderState orderStatus = new OrderState(idStatoOrdine, nomeStato, colore);
				Order ordine = new Order(idOrdine, user, indirizzo, dataOrdine, prezzoTotale, orderStatus,
						descrizioneStatoOrdine);

				order.add(ordine);
			}
			rs.close();
			p.close();
			con.close();
			return order;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while getting all orders");
			return order;
		}
	}

	/**
	 * Prende dal database gli ordini dell'utente specificato
	 * 
	 * @param userId
	 * @return ArrayList di ordini effettuati dall'utente corrente
	 */

	public synchronized ArrayList<Order> getOrdersOf(int userId) {
		ArrayList<Order> order = new ArrayList<Order>();

		try {
			Connection con = DataSource.getConnection();

			String query = "SELECT Ordine.id_ordine, Indirizzo.id_indirizzo,"
					+ "Indirizzo.nome_via,Indirizzo.CAP,Indirizzo.num_civico,"
					+ "Indirizzo.nazione,Indirizzo.regione,Indirizzo.citta,"
					+ "Ordine.data_ordine,Ordine.prezzo_totale,StatoOrdine.id_stato,"
					+ "StatoOrdine.nome,Ordine.stato_ordine, StatoOrdine.colore FROM Ordine, "
					+ "Utente, Indirizzo, StatoOrdine WHERE Ordine.id_utente = Utente.id_utente "
					+ "AND Ordine.id_indirizzo = Indirizzo.id_indirizzo AND StatoOrdine.id_stato = Ordine.id_stato "
					+ "AND Ordine.isVisibile = 1 and Utente.id_utente = ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, userId);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idOrdine = rs.getInt("id_ordine");
				int idIndirizzo = rs.getInt("id_indirizzo");
				String nomeVia = rs.getString("nome_via");
				String CAP = rs.getString("CAP");
				int numCivico = rs.getInt("num_civico");
				String nazione = rs.getString("nazione");
				String regione = rs.getString("regione");
				String citta = rs.getString("citta");
				Timestamp dataOrdine = rs.getTimestamp("data_ordine");
				Double prezzoTotale = rs.getDouble("prezzo_totale");
				int idStatoOrdine = rs.getInt("id_stato");
				String nomeStato = rs.getString("nome");
				String descrizioneStatoOrdine = rs.getString("stato_ordine");
				String colore = rs.getString("colore");

				Address indirizzo = new Address(idIndirizzo, nomeVia, CAP, numCivico, nazione, regione, citta);
				OrderState orderStatus = new OrderState(idStatoOrdine, nomeStato, colore);
				Order ordine = new Order(idOrdine, indirizzo, dataOrdine, prezzoTotale, orderStatus,
						descrizioneStatoOrdine);
				order.add(ordine);
			}
			rs.close();
			p.close();
			con.close();
			return order;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while getting all orders of user with id: " + userId);
			return order;
		}
	}

	/**
	 * Verifica sul database se la quantità desiderata del prodotto che si vuole
	 * acquistare
	 * 
	 * @param idProdotto
	 * @param quantita
	 * @return true - se la quantità desiderata del prodotto specificato è
	 *         disponibile <br>
	 *         false - se la quantità del prodotto presente sul server è inferiore a
	 *         quella che si vuole acquistare
	 */
	public synchronized boolean ProductAvailable(int idProdotto, int quantita) {
		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM Prodotto WHERE id_prodotto = ? AND quantita >= ?;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idProdotto);
			p.setInt(2, quantita);
			ResultSet rs = p.executeQuery();
			boolean available = rs.next();
			rs.close();
			p.close();
			con.close();
			return available;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while checking if product " + idProdotto + " is available");
			return false;
		}
	}

	/**
	 * Fa una richiesta al database per "prenotare" la quantità richiesta del
	 * prodotto desiderato
	 * 
	 * @param idProdotto
	 * @param quantita
	 * @param idUtente
	 * @return true - se la richiesta è andata a buon fine <br>
	 *         false - se c'è stato un problema durante la prenotazione del prodotto
	 */

	public synchronized boolean reservation(int idProdotto, int quantita, int idUtente) {

		if (!ProductAvailable(idProdotto, quantita))
			return false;

		try {
			Connection con = DataSource.getConnection();
			String query = "SELECT * FROM Carrello WHERE idUtente = ? AND idProdotto = ? AND selled = 0;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idUtente);
			p.setInt(2, idProdotto);
			ResultSet rs = p.executeQuery();
			int result = 0;
			if (rs.next()) {
				int oldQuantity = rs.getInt("quantita");
				String query2 = "UPDATE Carrello as c SET quantita = ? WHERE idUtente = ? AND idProdotto = ? AND selled = 0;";
				PreparedStatement p2 = con.prepareStatement(query2);
				p2.setInt(1, oldQuantity + quantita);
				p2.setInt(2, idUtente);
				p2.setInt(3, idProdotto);
				result = p2.executeUpdate();
				p2.close();
			} else {
				String query2 = "INSERT INTO Carrello (Carrello.idUtente, Carrello.idProdotto, Carrello.quantita) VALUES (?,?,?);";
				PreparedStatement p2 = con.prepareStatement(query2);
				p2.setInt(1, idUtente);
				p2.setInt(2, idProdotto);
				p2.setInt(3, quantita);
				result = p2.executeUpdate();
				p2.close();
			}
			String decreaseQuery = "UPDATE Prodotto SET quantita = (quantita-?) WHERE id_prodotto = ?";
			PreparedStatement pDecrease = con.prepareStatement(decreaseQuery);
			pDecrease.setInt(1, quantita);
			pDecrease.setInt(2, idProdotto);
			pDecrease.executeUpdate();
			pDecrease.close();

			p.close();
			rs.close();
			con.close();
			return result != 0;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while updating already reserved product (" + idProdotto + ") for user: " + idUtente);
			return false;
		}
	}

	/**
	 * Invia una richiesta al database per effettuare l'ordine per l'utente e
	 * all'indirizzo specificato
	 * 
	 * @param idUtente
	 * @param indirizzo
	 * @param cartItems
	 * @return true - se l'ordine è stato inserito con successo <br>
	 *         false - se la richiesta è fallita
	 */
	public synchronized boolean insertOrder(int idUtente, Address indirizzo, HashMap<Product, Integer> cartItems) {
		Connection con = DataSource.getConnection();
		try {
			con.setAutoCommit(false);
			Double prezzoTotale = 0.00;

			String insertAddress = "INSERT INTO `Indirizzo`(`nome_via`, `CAP`, `num_civico`, `citta`, `nazione`, `regione`) VALUES (?,?,?,?,?,?)";
			PreparedStatement pAddress = con.prepareStatement(insertAddress, PreparedStatement.RETURN_GENERATED_KEYS);
			pAddress.setString(1, indirizzo.getNome_via());
			pAddress.setString(2, indirizzo.getCAP());
			pAddress.setInt(3, indirizzo.getNum_civico());
			pAddress.setString(4, indirizzo.getCitta());
			pAddress.setString(5, indirizzo.getNazione());
			pAddress.setString(6, indirizzo.getRegione());
			int result = pAddress.executeUpdate();

			ResultSet rsIndirizzo = pAddress.getGeneratedKeys();
			rsIndirizzo.next();
			int idIndirizzo = rsIndirizzo.getInt(1);
			pAddress.close();
			if (result < 1)
				throw new Exception(new Throwable());

			for (Product p : cartItems.keySet())
				prezzoTotale += p.getPrezzo() * cartItems.get(p);

			String query = "INSERT INTO `Ordine`(`id_utente`, `id_indirizzo`, `prezzo_totale`) VALUES (?,?,?)";
			PreparedStatement p = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			p.setInt(1, idUtente);
			p.setInt(2, idIndirizzo);
			p.setDouble(3, prezzoTotale);
			result = p.executeUpdate();

			ResultSet rs = p.getGeneratedKeys();
			rs.next();
			int idOrdine = rs.getInt(1);

			String query2 = "INSERT INTO `OrdineProdotti`(`id_ordine`, `id_prodotto`, `quantita`) VALUES (?,?,?);";
			String query3 = "UPDATE Carrello SET selled=1 WHERE idProdotto = ? AND idUtente = ?;";
			PreparedStatement p2 = null;
			PreparedStatement p3 = null;
			for (Product prod : cartItems.keySet()) {
				int idProdotto = prod.getIdProdotto();
				p2 = con.prepareStatement(query2);
				p2.setInt(1, idOrdine);
				p2.setInt(2, idProdotto);
				p2.setInt(3, cartItems.get(prod));

				p3 = con.prepareStatement(query3);
				p3.setInt(1, idProdotto);
				p3.setInt(2, idUtente);

				result = p2.executeUpdate() + p3.executeUpdate();
			}

			p2.close();
			p3.close();
			p.close();

			if (result < 2)
				throw new Exception();
			con.commit();

			con.close();
			return result >= 2;
		} catch (Exception e) {
			try {
				con.rollback();
				Logger.getInstance().captureException(e, "Rollback fatto per DatabaseHandler.insertOrder()");
				con.close();
				return false;
			} catch (Exception e1) {
				Logger.getInstance().captureException(e1, "error while rollbacking from insertOrder");
				return false;
			}

		}

	}

	/**
	 * Effettua una richiesta di rimozione del prodtto in questione dal carrello del
	 * Database
	 * 
	 * @param idUtente
	 * @param idProdotto
	 * @param quantity
	 * @return true - se il prodotto è stato rimosso <br>
	 *         false - altrimenti
	 */
	public boolean removeProductCart(int idUtente, int idProdotto, int quantity) {
		if (quantity > 1)
			return reduceProductCart(idUtente, idProdotto, quantity);

		return deleteProductCart(idUtente, idProdotto);

	}

	private boolean reduceProductCart(int idUtente, int idProdotto, int quantity) {
		try {
			Connection con = DataSource.getConnection();
			String query0 = "UPDATE Carrello SET quantita = ? WHERE idUtente = ? AND idProdotto = ? AND selled = 0;";
			PreparedStatement p = con.prepareStatement(query0);
			p.setInt(1, quantity - 1);
			p.setInt(2, idUtente);
			p.setInt(3, idProdotto);
			int rs = p.executeUpdate();

			int resultNested = 0;
			if (rs != 0) {
				String query = "UPDATE Prodotto SET quantita = (quantita + 1) WHERE id_prodotto = ?;";
				PreparedStatement p2 = con.prepareStatement(query);
				p2.setInt(1, idProdotto);
				resultNested = p2.executeUpdate();
				p2.close();
			}
			p.close();
			con.close();
			return rs != 0 && resultNested != 0;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while reducing product" + idProdotto + " user: " + idUtente);
			return false;
		}
	}

	private boolean deleteProductCart(int idUtente, int idProdotto) {
		try {
			Connection con = DataSource.getConnection();
			String query = "DELETE FROM Carrello WHERE idUtente = ? AND idProdotto = ? AND selled = 0;";
			PreparedStatement p = con.prepareStatement(query);
			p.setInt(1, idUtente);
			p.setInt(2, idProdotto);
			int rs = p.executeUpdate();
			p.close();
			con.close();
			return rs >= 1;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while removing product" + idProdotto + " user: " + idUtente);
			return false;
		}

	}

	/**
	 * @param userId - l'identificatore univoco dell'utente
	 * @return cartItems dell'utente con userId
	 */
	public HashMap<Product, Integer> getCartOf(int userId) {
		HashMap<Product, Integer> cartItems = new HashMap<Product, Integer>();
		Connection con = DataSource.getConnection();
		try {
			String query = "SELECT P.id_prodotto, P.nome, P.descrizione, P.prezzo, P.quantita, P.foto, Cat.id_categoria, Cat.nome, Cat.descrizione,Cat.id_parent, "
					+ "C.quantita FROM Prodotto AS P, Carrello AS C, Categoria AS Cat WHERE C.idProdotto = P.id_prodotto AND "
					+ "Cat.id_categoria = P.id_categoria AND C.selled = 0 AND C.idUtente = ?";

			PreparedStatement p;
			p = con.prepareStatement(query);
			p.setInt(1, userId);
			ResultSet rs = p.executeQuery();
			while (rs.next()) {
				int idProdotto = rs.getInt(1);
				String nomeProdotto = rs.getString(2);
				String descrizionProdotto = rs.getString(3);
				Double prezzoProdotto = rs.getDouble(4);
				int quantityProdotto = rs.getInt(5);
				FileBlob fileBlob = new FileBlob(rs.getBlob(6));
				int idCategroy = rs.getInt(7);
				String nomeCategory = rs.getString(8);
				String descrizioneCategory = rs.getString(9);
				int idParentCategory = rs.getInt(10);
				Integer quantityInCart = rs.getInt(11);

				Category category = new Category(idCategroy, nomeCategory, descrizioneCategory, idParentCategory);
				Product product = new Product(idProdotto, nomeProdotto, descrizionProdotto, prezzoProdotto,
						quantityProdotto, fileBlob, category);
				cartItems.put(product, quantityInCart);
			}

			rs.close();
			p.close();
			con.close();
			return cartItems;
		} catch (SQLException e) {
			Logger.getInstance().captureException(e, "Error while getting cartItems for user: " + userId);
			return cartItems;
		}
	}
}
