package application.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import application.client.Client;
import application.common.Logger;
import application.common.Protocol;
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
import io.sentry.SentryLevel;

public class ManagerHandler extends Thread {

	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String email = "";

	private boolean authenticated = false;

	private ArrayList<OrderState> states;
	public HashMap<String, Integer> roles;

	public ManagerHandler(Socket socket) throws Exception {
		this.socket = socket;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		states = DatabaseHandler.getInstance().getAllOrderState();
		roles = DatabaseHandler.getInstance().getAllRoles();
	}

	public boolean login(User user) throws SQLException {
		return DatabaseHandler.getInstance().checkUser(user);
	}

	public boolean register(String email) throws SQLException {
		return DatabaseHandler.getInstance().existsUser(email);
	}

	@Override
	public void run() {
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			String input = "";

			while (true) {
				input = (String) in.readObject();
				if (!input.equals(""))
					if (authenticated) {
						switch (input) {

						case Protocol.SEND_EMAIL_TO_ADMIN: {
							Email email = (Email) in.readObject();
							if (EmailSender.getInstance().sendEmail(email))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_EMAIL_NOT_SENT);
							break;
						}

						case Protocol.SEND_CART_OF: {
							int userId = (int) in.readObject();
							HashMap<Product, Integer> cartItems = DatabaseHandler.getInstance().getCartOf(userId);
							sendMessage(cartItems);
							break;
						}
						case Protocol.REMOVE_PRODUCT_CART: {
							int idUtente = (int) in.readObject();
							int idProdotto = (int) in.readObject();
							int quantity = (int) in.readObject();
							if (DatabaseHandler.getInstance().removeProductCart(idUtente, idProdotto, quantity))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_REMOVE_CART_ITEM);
							break;
						}

						case Protocol.PLACE_ORDER: {
							int idUtente = (int) in.readObject();
							Address indirizzo = (Address) in.readObject();
							HashMap<Product, Integer> cartItems = (HashMap<Product, Integer>) in.readObject();
							if (DatabaseHandler.getInstance().insertOrder(idUtente, indirizzo, cartItems))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_NEW_ORDER);
							break;
						}

						case Protocol.SEND_STATS_INCOMING: {
							sendMessage(DatabaseHandler.getInstance().sendStatsEntrance());
							break;
						}

						case Protocol.SEND_STATS_BEST_SELLERS: {
							sendMessage(DatabaseHandler.getInstance().sendStatsBestSellers());
							break;
						}

						case Protocol.EDIT_CONFIG: {
							Configurazione config = (Configurazione) in.readObject();
							if (DatabaseHandler.getInstance().editConfig(config)) {
								sendMessage(Protocol.OK);
								EmailSender.getInstance().EmailConfig(config);
							} else
								sendMessage(Protocol.ERROR_EDIT_CONFIG);
							break;
						}

						case Protocol.CHANGE_THEME: {
							int idUtente = (int) in.readObject();
							String theme = (String) in.readObject();
							if (DatabaseHandler.getInstance().changeTheme(idUtente, theme))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR);
							break;
						}

						case Protocol.REMOVE_ORDER: {
							int idOrdine = (int) in.readObject();
							if (DatabaseHandler.getInstance().removeOrder(idOrdine))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_ORDER_NOT_REMOVED);
							break;
						}

						case Protocol.SEND_THEME: {
							int idUtente = (int) in.readObject();
							String theme = DatabaseHandler.getInstance().getTheme(idUtente);
							sendMessage(theme);
							break;
						}

						case Protocol.CHANGE_PASSWORD: {
							String id = (String) in.readObject();
							String newPassword = (String) in.readObject();

							int idUtente = Integer.parseInt(id);
							if (DatabaseHandler.getInstance().updatePassword(idUtente, newPassword))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_CHANGING_PASSWORD);
							break;
						}

						case Protocol.CHECK_PASSWORD: {
							String id = (String) in.readObject();
							String password = (String) in.readObject();
							if (DatabaseHandler.getInstance().checkUserPassword(id, password))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_CHECKING_PASSWORD);
							break;
						}

						case Protocol.SEND_CONFIG: {
							Configurazione config = DatabaseHandler.getInstance().getConfig();
							sendMessage(config);
							break;
						}
						case Protocol.REFRESH_USER: {
							User user = DatabaseHandler.getInstance().getUser(email);
							sendMessage(user);
							break;
						}
						case Protocol.LOGOUT: {
							String emailUser = (String) in.readObject();
							UsersHandler.removeUser(emailUser);
							email = "";
							authenticated = false;
							sendMessage(Protocol.OK);
							out = null;
							return;
						}
						case Protocol.EDIT_USER: {
							User utente = (User) in.readObject();
							String password = utente.getPassword();

							if (DatabaseHandler.getInstance().editUser(utente, password))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_USER_EDIT);
							break;
						}

						case Protocol.ADD_USER: {
							User utente = (User) in.readObject();
							if (DatabaseHandler.getInstance().existsUser(utente.getEmail())) {
								sendMessage(Protocol.ERROR_USER_EXISTS);
								Logger.getInstance().captureMessage(Protocol.ERROR_USER_EXISTS);
							} else {
								if (DatabaseHandler.getInstance().addUser(utente))
									sendMessage(Protocol.OK);
								else
									sendMessage(Protocol.ERROR_USER_NOT_ADDED);
							}
							break;
						}
						case Protocol.REMOVE_USER: {
							String email = (String) in.readObject();
							if (!DatabaseHandler.getInstance().existsUser(email)) {
								sendMessage(Protocol.ERROR_USER_NOT_EXISTS);
								Logger.getInstance().captureMessage(Protocol.ERROR_USER_NOT_EXISTS);
								break;
							} else {
								if (DatabaseHandler.getInstance().removeUser(email))
									sendMessage(Protocol.OK);
								else
									sendMessage(Protocol.ERROR_USER_NOT_REMOVED);
							}
							break;
						}
						case Protocol.EDIT_ORDER: {
							String userEmail = (String) in.readObject();
							OrderState stato = (OrderState) in.readObject();
							String descrizione = (String) in.readObject();
							int idOrdine = (int) in.readObject();
							Address indirizzo = (Address) in.readObject();
							if (DatabaseHandler.getInstance().editOrder(stato, descrizione, idOrdine, indirizzo)) {
								sendMessage(Protocol.OK);
								EmailSender.getInstance().sendEmailOrdineModificato(idOrdine, stato, descrizione,
										userEmail);
							} else
								sendMessage(Protocol.ERROR_ORDER_EDIT);
							break;
						}
						case Protocol.EDIT_CATEGORY: {
							Category cat = (Category) in.readObject();
							if (DatabaseHandler.getInstance().existsCategory(cat)) {
								sendMessage(Protocol.ERROR_CATEGORY_EXISTS);
								Logger.getInstance()
										.captureMessage(cat.getNome() + ": " + Protocol.ERROR_CATEGORY_EXISTS);
							} else if (DatabaseHandler.getInstance().editCategory(cat))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_CATEGORY_EDIT);
							break;
						}

						case Protocol.SEND_ALL_ROLES: {
							sendMessage(roles);
							break;
						}
						case Protocol.SEND_ALL_ORDERS_PRODUCT_ORDERID: {
							int idOrdine = Integer.parseInt((String) in.readObject());
							ArrayList<OrderProduct> prodotti = DatabaseHandler.getInstance()
									.getOrderProductsId(idOrdine);
							sendMessage(prodotti);
							break;
						}
						case Protocol.SEND_ALL_PRODUCTS: {
							ArrayList<Product> prodotti = DatabaseHandler.getInstance().getAllProducts();
							sendMessage(prodotti);
							break;
						}
						case Protocol.SEND_ALL_MAGAZZINO_PRODUCTS: {
							ArrayList<Product> prodotti = DatabaseHandler.getInstance().getAllMagazzinoProducts(false);
							sendMessage(prodotti);
							break;
						}
						case Protocol.SEND_ALL_USERS: {
							ArrayList<User> users = DatabaseHandler.getInstance().getAllUsers();
							sendMessage(users);
							break;
						}
						case Protocol.SEND_ALL_ORDERS: {
							ArrayList<Order> orders = DatabaseHandler.getInstance().getAllOrders();
							sendMessage(orders);
							break;
						}
						case Protocol.SEND_ALL_ORDERS_STATE: {
							sendMessage(states);
							break;
						}

						case Protocol.RESERVE_PRODUCTS: {
							int idProdotto = (int) in.readObject();
							int quantita = (int) in.readObject();
							int idUtente = (int) in.readObject();
							if (DatabaseHandler.getInstance().reservation(idProdotto, quantita, idUtente))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_RESERVATION_PRODUCT);
							break;
						}

						case Protocol.SEND_ALL_ORDERS_PRODUCT: {
							ArrayList<OrderProduct> ordersProducts = DatabaseHandler.getInstance()
									.getAllOrderProducts();
							sendMessage(ordersProducts);
							break;
						}

						case Protocol.REMOVE_PRODUCT: {
							int idProdotto = (int) in.readObject();
							if (DatabaseHandler.getInstance().removeProduct(idProdotto))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_PRODUCT_NOT_REMOVED);

							break;
						}
						case Protocol.SEND_ALL_CATEGORIES: {
							ArrayList<Category> categories = DatabaseHandler.getInstance().getAllCategories();
							sendMessage(categories);
							break;
						}
						case Protocol.ADD_CATEGORY: {
							Category cat = (Category) in.readObject();
							if (DatabaseHandler.getInstance().existsCategory(cat)) {
								sendMessage(Protocol.ERROR_CATEGORY_EXISTS);
								Logger.getInstance()
										.captureMessage(cat.getNome() + ": " + Protocol.ERROR_CATEGORY_EXISTS);
							} else if (DatabaseHandler.getInstance().insertCategory(cat))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_CATEGORY_NOT_ADDED);
							break;
						}
						case Protocol.REMOVE_CATEGORY: {
							Category cat = (Category) in.readObject();
							if (DatabaseHandler.getInstance().removeCategory(cat))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_CATEGORY_NOT_REMOVED);
							break;
						}
						case Protocol.ADD_PRODUCT: {
							Product p = (Product) in.readObject();
							if (DatabaseHandler.getInstance().addProduct(p))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_PRODUCT_NOT_ADDED);
							break;
						}
						case Protocol.EDIT_PRODUCT: {
							Product p = (Product) in.readObject();
							if (DatabaseHandler.getInstance().editProduct(p))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_PRODUCT_EDIT);
							break;
						}
						case Protocol.SEND_EMAIL: {
							Email e = (Email) in.readObject();
							if (EmailSender.getInstance().sendEmail(e))
								sendMessage(Protocol.OK);
							else
								sendMessage(Protocol.ERROR_EMAIL_NOT_SENT);
							break;
						}
						case Protocol.SEND_DASHBOARD_DATA: {
							int month = (int) in.readObject();
							int year = (int) in.readObject();
							DashboardData data = DatabaseHandler.getInstance().getDashboardData(month, year);
							sendMessage(data);
							break;
						}
						case Protocol.SEND_DASHBOARD_DATA_CLIENT: {
							int idUtente = (int) in.readObject();
							int month = (int) in.readObject();
							int year = (int) in.readObject();
							DashboardData data = DatabaseHandler.getInstance().getDashboardDataCliente(idUtente, month,
									year);
							sendMessage(data);
							break;
						}
						case Protocol.SEND_ORDERS_OF: {
							int userId = (int) in.readObject();
							ArrayList<Order> orders = DatabaseHandler.getInstance().getOrdersOf(userId);
							sendMessage(orders);
							break;
						}
						}
					} else {
						if (input.equals(Protocol.SEND_NEW_PASSWORD)) {
							String emailRecovery = (String) in.readObject();
							if (!UsersHandler.contains(emailRecovery)) {
								if (!DatabaseHandler.getInstance().existsUser(emailRecovery)) {
									sendMessage(Protocol.ERROR_USER_NOT_EXISTS);
								} else if (DatabaseHandler.getInstance().passwordRecovery(emailRecovery)) {
									sendMessage(Protocol.OK);
								} else
									sendMessage(Protocol.ERROR_EMAIL_NOT_SENT);
							} else
								sendMessage(Protocol.USER_ALREADY_LOGGED);

							break;
						}
						// #1 BEGIN controlli di autenticazione Login/Register
						User user = (User) in.readObject();
						if (input.equals(Protocol.LOGIN)) {
							if (!login(user)) {
								sendMessage(Protocol.ERROR_AUTHENTICATION);
								Logger.getInstance().captureMessage(Protocol.ERROR_AUTHENTICATION);
								break;
							}
						} else if (input.equals(Protocol.REGISTER)) {
							if (DatabaseHandler.getInstance().existsUser(user.getEmail())) {
								sendMessage(Protocol.ERROR_USER_EXISTS);
								Logger.getInstance().captureMessage(Protocol.ERROR_USER_EXISTS);
								break;
							} else if (!DatabaseHandler.getInstance().insertUser(user)) {
								sendMessage(Protocol.ERROR_USER_NOT_ADDED);
								Logger.getInstance().captureMessage(Protocol.ERROR_USER_NOT_ADDED);
								break;
							}

						} else {
							sendMessage(Protocol.ERROR);
							Logger.getInstance().captureMessage(Protocol.ERROR);
							break;
						}

						email = user.getEmail();
						if (!UsersHandler.insertUser(email, this)) {
							sendMessage(Protocol.USER_ALREADY_LOGGED);
							Logger.getInstance().captureMessage(Protocol.USER_ALREADY_LOGGED);
							email = "";
							break;
						}
						sendMessage(Protocol.OK);
						user = DatabaseHandler.getInstance().getUser(email);
						sendMessage(user);
						authenticated = true;
					}
				// #1 END controlli di autenticazione Login/Register
			}

		} catch (Exception e) {
			Logger.getInstance().captureException(e, "CLOSING WINDOWS");
			if (!email.equals("")) {
				UsersHandler.removeUser(email);
				email = "";
			} else {
				sendMessage(Protocol.ERROR);
				Logger.getInstance().captureException(e, Protocol.ERROR);
			}
			closeStreams();
			return;
		}
	}

	public void closeStreams() {
		try {
			if (out != null)
				out.close();
			out = null;
			if (in != null)
				in.close();
			in = null;
			if (socket != null)
				socket.close();
			socket = null;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while closing streams");
		}
	}

	public void sendMessage(Object message) {
		if (out == null)
			return;
		try {
			out.writeObject(message);
			out.flush();
		} catch (IOException e) {
			if (!email.equals("")) {
				UsersHandler.removeUser(email);
				System.out.println("[SERVER] rimuovo :" + email);
			}
			closeStreams();
		}
	}

}
