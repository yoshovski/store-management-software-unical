package application.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Date;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import application.SceneHandler;
import application.Settings.THEME;
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

public class Client implements Runnable { // implementa Runnable perché userà i thread

	private static Client instance = null; // singleton perché vogliamo solo un oggetto Client
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private User user = null;

	private ArrayList<OrderState> states = new ArrayList<OrderState>();
	private boolean edit = false;
	private boolean themeChanged = false;
	public static HashMap<String, Integer> roles = new HashMap<String, Integer>();
	public static HashMap<Product, Integer> cartItems = new HashMap<Product, Integer>();

	private THEME currentTheme = THEME.LIGHT;

	private Client() {
		try {
			socket = new Socket("localhost", 8000);
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			reset();
			String errorMessage = "Cannot connect to the server";
			Logger.getInstance().captureException(e, errorMessage);
			SceneHandler.getInstance().showError(errorMessage);
		}
	}

	public static Client getInstance() {
		if (instance == null)
			instance = new Client();
		return instance;
	}

	@Override
	public void run() {
		while (out != null && in != null) {
			try {

				// String mess = (String) in.readObject(); Messages.addMessage(mess);
			}

			catch (Exception e) {
				reset();
				// out = null;
				String errorMessage = "Lost Connection";
				Logger.getInstance().captureException(e, errorMessage);
				SceneHandler.getInstance().showError(errorMessage);
			}
		}
	}

	public void refreshUser() {
		sendMessage(Protocol.REFRESH_USER);
		initializeUser();
		edit = true;
	}

	public boolean userEdited() {
		return edit;
	}

	public boolean addToCart(Product p, int quantity) {
		for (Product prod : cartItems.keySet()) {
			if (p.getIdProdotto() == prod.getIdProdotto()) {
				int oldQuantity = cartItems.remove(prod);
				cartItems.put(p, quantity + oldQuantity);
				return false;
			}
		}
		cartItems.put(p, quantity);
		return true;

	}

	public void reset() {
		try {
			user = null;
			instance = null;

			if (out != null)
				out.close();
			out = null;
			if (in != null)
				in.close();
			in = null;

			// if (socket != null) socket.close(); socket = null;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while resetting client variables");
		}
	}

	public String authentication(String nome, String cognome, String email, String password) {

		sendMessagePrivate(Protocol.REGISTER);
		sendMessagePrivate(new User(nome, cognome, email, password));
		Logger.getInstance().setUser(email);
		try {
			// in = new ObjectInputStream(socket.getInputStream());
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			// out = null;
			Logger.getInstance().captureException(e, "error authentication Client while registering user: " + email);
			return Protocol.ERROR;
		}
	}

	public String authentication(String email, String password) {

		sendMessagePrivate(Protocol.LOGIN);
		sendMessagePrivate(new User(email, password));
		Logger.getInstance().setUser(email);

		try {
			// in = new ObjectInputStream(socket.getInputStream());
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			// out = null;
			Logger.getInstance().captureException(e, "error authentication Client while logging user: " + email);
			return Protocol.ERROR;
		}
	}

	private boolean sendMessagePrivate(Object message) {
		if (out == null)
			return false;
		try {

			if (in == null)
				in = new ObjectInputStream(socket.getInputStream());

			out.writeObject(message);
			out.flush();
		} catch (Exception e) {
			SceneHandler.getInstance().showError("Server has been closed!");
			SceneHandler.getInstance().setLogoutScene();
			reset();
			// out = null;
			Logger.getInstance().captureException(e);
			return false;
		}
		return true;
	}

	public boolean sendMessage(String message) {
		return sendMessagePrivate(message);
	}

	public boolean logout() {
		try {
			sendMessagePrivate(Protocol.LOGOUT);
			sendMessage(user.getEmail());
			boolean result = in.readObject().toString().equals(Protocol.OK);
			if (result)
				reset();
			return result;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e);
			return false;
		}
	}

	public User getUser() {
		return user;
	}

	public String addNewUser(User user) {
		sendMessagePrivate(Protocol.ADD_USER);
		sendMessagePrivate(user);

		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			// out = null;
			Logger.getInstance().captureException(e, "Error while adding user: " + user.getEmail());
			return Protocol.ERROR;
		}
	}

	public String editUser(User user) {
		sendMessage(Protocol.EDIT_USER);
		sendMessagePrivate(user);
		try {
			String res = (String) in.readObject();
			refreshUser();
			return res;
		} catch (Exception e) {
			reset();
			// out = null;
			Logger.getInstance().captureException(e, "Error while editing user: " + user.getEmail());
			return Protocol.ERROR;
		}
	}

	public String removeUser(User user) {
		sendMessagePrivate(Protocol.REMOVE_USER);
		sendMessagePrivate(user.getEmail());

		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			// out = null;
			Logger.getInstance().captureException(e, "Error while removing user: " + user.getEmail());
			return Protocol.ERROR;
		}
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Product> getAllProducts() {
		sendMessagePrivate(Protocol.SEND_ALL_PRODUCTS);
		ArrayList<Product> products = new ArrayList<Product>();
		try {
			products = ((ArrayList<Product>) in.readObject());
			return products;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all products");
			return products;
		}
	}

	public ArrayList<Product> getAllMagazzinoProducts() {
		sendMessagePrivate(Protocol.SEND_ALL_MAGAZZINO_PRODUCTS);
		ArrayList<Product> products = new ArrayList<Product>();
		try {
			products = ((ArrayList<Product>) in.readObject());
			return products;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all magazzino products");
			return products;
		}
	}

	public ArrayList<User> getAllUsers() {
		sendMessagePrivate(Protocol.SEND_ALL_USERS);
		ArrayList<User> users = new ArrayList<User>();
		try {

			users = (ArrayList<User>) in.readObject();
			return users;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all users");
			return users;
		}
	}

	public HashMap<String, Integer> getRoles() {
		if (!roles.isEmpty())
			return roles;

		sendMessagePrivate(Protocol.SEND_ALL_ROLES);
		try {
			roles = (HashMap<String, Integer>) in.readObject();
			return roles;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while retriving all roles");
			return roles;
		}
	}

	public String removeProduct(int idProdotto) {
		sendMessagePrivate(Protocol.REMOVE_PRODUCT);
		sendMessagePrivate(idProdotto);

		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			// out = null;
			Logger.getInstance().captureException(e, "Error while removing product: " + idProdotto);
			return Protocol.ERROR;
		}
	}

	public ArrayList<Category> getAllCategories() {
		sendMessagePrivate(Protocol.SEND_ALL_CATEGORIES);
		ArrayList<Category> categories = new ArrayList<Category>();
		try {
			categories = (ArrayList<Category>) in.readObject();
			return categories;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all categories");
			return categories;
		}
	}

	public String addNewCategory(Category category) {
		sendMessagePrivate(Protocol.ADD_CATEGORY);
		sendMessagePrivate(category);

		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e,
					"Error while adding category: " + category.getIdCategoria() + " - " + category.getNome());
			return Protocol.ERROR;
		}
	}

	/**
	 * Manda richiesta di cancellazione della categoria di prodotto "category"
	 * 
	 * @param category - Categoria da cancellare
	 * @return res - risposta del Server
	 */
	public String removeCategory(Category category) {
		sendMessagePrivate(Protocol.REMOVE_CATEGORY);
		sendMessagePrivate(category);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "Error while removing Category");
			return Protocol.ERROR;
		}
	}

	public ArrayList<Order> getAllOrders() {
		sendMessagePrivate(Protocol.SEND_ALL_ORDERS);
		ArrayList<Order> orders = new ArrayList<Order>();
		try {
			orders = (ArrayList<Order>) in.readObject();
			return orders;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all orders");
			return orders;
		}
	}

	public ArrayList<OrderState> getAllOrderState() {
		if (!states.isEmpty())
			return states;

		sendMessagePrivate(Protocol.SEND_ALL_ORDERS_STATE);
		try {
			states = (ArrayList<OrderState>) in.readObject();
			return states;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all orders state");
			return states;
		}
	}

	public ArrayList<OrderProduct> getAllOrderProducts() {
		sendMessagePrivate(Protocol.SEND_ALL_ORDERS_PRODUCT);
		ArrayList<OrderProduct> orderProduct = new ArrayList<OrderProduct>();

		try {
			orderProduct = (ArrayList<OrderProduct>) in.readObject();
			return orderProduct;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all orders products");
			return orderProduct;
		}
	}

	public String editOrder(String email, OrderState stato, String descrizione, int idOrdine, Address indirizzo) {
		sendMessage(Protocol.EDIT_ORDER);
		sendMessage(email);
		sendMessagePrivate(stato);
		sendMessagePrivate(descrizione);
		sendMessagePrivate(idOrdine);
		sendMessagePrivate(indirizzo);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			// out = null;
			Logger.getInstance().captureException(e, "Error while editing order: " + idOrdine);
			return Protocol.ERROR;
		}
	}

	public ArrayList<OrderProduct> getOrderProducts(int orderId) {
		sendMessagePrivate(Protocol.SEND_ALL_ORDERS_PRODUCT_ORDERID);
		sendMessage(Integer.toString(orderId));
		ArrayList<OrderProduct> orderProducts = new ArrayList<OrderProduct>();
		try {
			orderProducts = (ArrayList<OrderProduct>) in.readObject();
			return orderProducts;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all orders product of order: " + orderId);
			return orderProducts;
		}
	}

	/**
	 * Invia richiesta al server per modificare la categoria.
	 * 
	 * @param category - la categoria con i dati aggiornati
	 * @return res - risposta dal server
	 */
	public String editCategory(Category category) {
		sendMessagePrivate(Protocol.EDIT_CATEGORY);
		sendMessagePrivate(category);

		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "Error while editing category: " + category.getIdCategoria());
			return Protocol.ERROR;
		}
	}

	public String addNewProduct(Product product) {
		sendMessagePrivate(Protocol.ADD_PRODUCT);
		sendMessagePrivate(product);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "Error while adding product: " + product.getNome());
			return Protocol.ERROR;
		}
	}

	public String editProduct(Product product) {
		sendMessagePrivate(Protocol.EDIT_PRODUCT);
		sendMessagePrivate(product);

		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "Error while editing product: " + product.getNome());
			return Protocol.ERROR;
		}
	}

	public void initializeUser() {
		try {
			user = (User) in.readObject();
			currentTheme = getTheme(user.getIdUtente());

			if (user!=null && user.hasRole("cliente")) {
				refreshCurrentCartItems();
			}

		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while initializing user: " + user.getIdUtente());
		}
	}

	public void setUserEdited(boolean b) {
		edit = b;
	}

	public Configurazione getConfig() {
		sendMessagePrivate(Protocol.SEND_CONFIG);
		Configurazione config = null;
		try {
			config = (Configurazione) in.readObject();
			return config;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving shop configuration");
			return config;
		}
	}

	public String editConfig(Configurazione config) {
		sendMessage(Protocol.EDIT_CONFIG);
		sendMessagePrivate(config);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "Error while editing shop configuration");
			return Protocol.ERROR;
		}
	}

	public String checkPassword(int id, String currentPassword) {
		sendMessagePrivate(Protocol.CHECK_PASSWORD);
		sendMessage(Integer.toString(id));
		sendMessage(currentPassword);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while checking password of user: " + id);
			return Protocol.ERROR_CHECKING_PASSWORD;
		}
	}

	public String changePassword(int id, String newPassword) {
		sendMessagePrivate(Protocol.CHANGE_PASSWORD);
		sendMessage(Integer.toString(id));
		sendMessage(newPassword);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while checking password of user: " + id);
			return Protocol.ERROR_CHANGING_PASSWORD;
		}
	}

	/**
	 * Richiede l'invio di un email.
	 * 
	 * @return true (inviata con successo), false (fallimento)
	 */
	public String sendEmail(Email emailBody) {
		sendMessagePrivate(Protocol.SEND_EMAIL);
		sendMessagePrivate(emailBody);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while sending email");
			return Protocol.ERROR_EMAIL_NOT_SENT;
		}
	}

	public String sendNewPassword(String email) {
		sendMessagePrivate(Protocol.SEND_NEW_PASSWORD);
		sendMessagePrivate(email);
		try {
			String res = (String) in.readObject();
			reset();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().setUser(email);
			Logger.getInstance().captureException(e, "error while sending new password to: " + email);
			return Protocol.ERROR_EMAIL_NOT_SENT;
		}
	}

	public String removeOrder(int idOrdine) {
		sendMessagePrivate(Protocol.REMOVE_ORDER);
		sendMessagePrivate(idOrdine);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while removing order #" + idOrdine);
			return Protocol.ERROR_ORDER_NOT_REMOVED;
		}
	}

	public THEME getTheme(int idUtente) {
		sendMessagePrivate(Protocol.SEND_THEME);
		sendMessagePrivate(idUtente);
		try {
			String res = (String) in.readObject();
			THEME theme = THEME.valueOf(res);
			return theme;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while retriving theme info for user " + idUtente);
			return THEME.LIGHT;
		}
	}

	public String changeTheme(int idUtente, THEME tema) {
		sendMessagePrivate(Protocol.CHANGE_THEME);
		sendMessagePrivate(idUtente);
		sendMessagePrivate(tema.toString());
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while setting theme for user " + idUtente);
			return Protocol.ERROR;
		}
	}

	public HashMap<Date, Double> getStatsIncomings() {
		sendMessagePrivate(Protocol.SEND_STATS_INCOMING);
		HashMap<Date, Double> data = new HashMap<Date, Double>();
		try {
			data = (HashMap<Date, Double>) in.readObject();
			return data;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retrieving stats");
			return data;
		}
	}

	public HashMap<String, Integer> getStatsBestSellers() {
		sendMessagePrivate(Protocol.SEND_STATS_BEST_SELLERS);
		HashMap<String, Integer> seller = new HashMap<String, Integer>();
		try {
			seller = (HashMap<String, Integer>) in.readObject();
			return seller;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retrieving best seller stats");
			return seller;
		}
	}

	public THEME getCurrentTheme() {
		return currentTheme;
	}

	public void setTheme(THEME theme) {
		this.currentTheme = theme;
		this.themeChanged = true;
	}

	public void setThemeEdited(boolean b) {
		this.themeChanged = b;
	}

	public boolean themeChanged() {
		return themeChanged;
	}

	public DashboardData getDashboardData(Month month, int year) {
		sendMessage(Protocol.SEND_DASHBOARD_DATA);
		int monthInt = month.getValue();
		sendMessagePrivate(monthInt);
		sendMessagePrivate(year);
		DashboardData dashboard = new DashboardData();
		try {
			dashboard = (DashboardData) in.readObject();
			return dashboard;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retrieving Dashboard Data for Shop Manager");
			return dashboard;
		}
	}

	public DashboardData getDashboardDataCliente(int idUtente, Month month, int year) {
		sendMessage(Protocol.SEND_DASHBOARD_DATA_CLIENT);
		sendMessagePrivate(idUtente);
		int monthInt = month.getValue();
		sendMessagePrivate(monthInt);
		sendMessagePrivate(year);
		DashboardData dashboard = new DashboardData();
		try {
			dashboard = (DashboardData) in.readObject();
			return dashboard;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while retrieving Dashboard Data for Client " + idUtente);
			return dashboard;
		}
	}

	public ArrayList<Order> getOrdersOf(int idUtente) {
		sendMessagePrivate(Protocol.SEND_ORDERS_OF);
		sendMessagePrivate(idUtente);
		ArrayList<Order> orders = new ArrayList<Order>();
		try {
			orders = (ArrayList<Order>) in.readObject();
			return orders;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e, "error while receiving all orders of user with id: " + idUtente);
			return orders;
		}
	}

	public String reserveProduct(int idProdotto, int value, int idUtente) {
		sendMessagePrivate(Protocol.RESERVE_PRODUCTS);
		sendMessagePrivate(idProdotto);
		sendMessagePrivate(value);
		sendMessagePrivate(idUtente);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			reset();
			Logger.getInstance().captureException(e,
					"error while reserving product " + idProdotto + " for user: " + idUtente);
			return Protocol.ERROR_RESERVATION_PRODUCT;
		}
	}

	public String placeNewOrder(int idUtente, Address indirizzo, HashMap<Product, Integer> itemsToBuy) {
		sendMessage(Protocol.PLACE_ORDER);
		sendMessagePrivate(idUtente);
		sendMessagePrivate(indirizzo);
		sendMessagePrivate(itemsToBuy);
		try {
			String res = (String) in.readObject();

			if (res.equals(Protocol.OK))
				refreshCurrentCartItems();

			return res;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while placing new order for user: " + idUtente);
			return Protocol.ERROR_NEW_ORDER;
		}
	}

	public void removeProductCart(Product prod) {
		for (Product p : cartItems.keySet())
			if (p.getIdProdotto() == prod.getIdProdotto()) {
				if (cartItems.get(p) == 1)
					cartItems.remove(p);
				else {
					int oldQuantity = cartItems.remove(p);
					cartItems.put(p, oldQuantity - 1);
				}
				break;
			}
	}

	public String removeProductCart(int idUtente, int idProdotto, int quantity) {
		sendMessagePrivate(Protocol.REMOVE_PRODUCT_CART);
		sendMessagePrivate(idUtente);
		sendMessagePrivate(idProdotto);
		sendMessagePrivate(quantity);
		try {
			String res = (String) in.readObject();
			return res;
		} catch (Exception e) {
			Logger.getInstance().captureException(e,
					"error while removing product(" + idProdotto + ") from cart items");
			return Protocol.ERROR_REMOVE_CART_ITEM;
		}
	}

	/**
	 * Asks server if the currentCartItems still exist on DataBase. If any changes
	 * ha occured, it changes users' cartItems.
	 * 
	 * @return true - cartItems refreshed <br>
	 *         false - cartItems NOT refreshed due to error <br>
	 */
	public boolean refreshCurrentCartItems() {
		sendMessagePrivate(Protocol.SEND_CART_OF);
		sendMessagePrivate(user.getIdUtente());
		try {
			HashMap<Product, Integer> itemsToConfront = (HashMap<Product, Integer>) in.readObject();
			cartItems = itemsToConfront;
			return true;
		} catch (Exception e) {
			Logger.getInstance().captureException(e, "error while Client.refreshCurrentCartItems()");
			return false;
		}
	}

}
