package application;

public class Settings {

	public static final int PASSWORD_LENGHT = 8;

	public static final String APPLICATION_TITLE = "Gestore Negozio";
	public static final String APPLICATION_SEPARATOR_TITLE = "-";

	public static final String DEFAULT_AVATAR_PROJECT_PATH = "/application/images/blank_avatar.png";
	public static final String DEFAULT_ADD_USER_PROJECT_PATH = "/application/images/add_user.png";
	public static final String DEFAULT_PRODUCT_PHOTO_PATH = "/application/images/product_photo_not_available.jpg";

	public static final String MAINFRAME_FXML = "MainFrame";
	public static final String DASHBOARD_FXML = "Dashboard";
	public static final String DASHBOARD_CLIENT_FXML = "DashboardClient";
	public static final String LOGINREGISTER_FXML = "LoginRegister";
	public static final String IMPOSTAZIONI_FXML = "Impostazioni";
	public static final String MAGAZZINO_FXML = "Magazzino";
	public static final String MAGAZZINO_VIEW_FXML = "MagazzinoView";
	public static final String CATEGORIE_FXML = "Categorie";
	public static final String ORDINI_FXML = "Ordini";
	public static final String PRODOTTI_FXML = "Prodotti";
	public static final String SLIDERMENU_FXML = "SliderMenu";
	public static final String STATISTICHE_FXML = "Statistiche";
	public static final String UTENTI_FXML = "Utenti";
	public static final String UTENTE_VIEW_FXML = "UtenteView";
	public static final String CATEGORIA_VIEW_FXML = "CategoriaView";
	public static final String ORDINE_VIEW_FXML = "OrdineView";
	public static final String PASSWORD_CHANGE_FXML = "ChangePassword";
	public static final String STORE_SETTINGS_CHANGE_FXML = "ChangeStoreSettings";
	public static final String CARRELLO_FXML = "Carrello";
	public static final String PRODOTTO_VIEW_FXML = "ProdottoView";
	public static final String CHECKOUT_FXML = "Checkout";
	public static final String HELP_FXML = "Help";

	public static final String MAINFRAME_TITLE = "MainFrame";
	public static final String DASHBOARD_TITLE = "Dashboard";
	public static final String DASHBOARD_CLIENTI_TITLE = "Dashboard";
	public static final String LOGINREGISTER_TITLE = "LoginRegister";
	public static final String IMPOSTAZIONI_TITLE = "Impostazioni";
	public static final String MAGAZZINO_TITLE = "Magazzino";
	public static final String CATEGORIE_TITLE = "Categorie";
	public static final String ORDINI_TITLE = "Ordini";
	public static final String PRODOTTI_TITLE = "Prodotti";
	public static final String SLIDERMENU_TITLE = "SliderMenu";
	public static final String STATISTICHE_TITLE = "Statistiche";
	public static final String UTENTI_TITLE = "Utenti";
	public static final String AGGIUNGI_UTENTE_TITLE = "Aggiungi Utente";
	public static final String MODIFICA_UTENTE_TITLE = "Modifica Utente";
	public static final String AGGIUNGI_CATEGORIA_TITLE = "Aggiungi Categoria";
	public static final String MODIFICA_CATEGORIA_TITLE = "Modifica Categoria";
	public static final String VISUALIZZA_ORDINE_TITLE = "Visualizza Ordine";
	public static final String AGGIUNGI_PRODOTTO_TITLE = "Aggiungi Prodotto";
	public static final String MODIFICA_PRODOTTO_TITLE = "Modifica Prodotto";
	public static final String MODIFICA_PASSWORD_TITLE = "Modifica Password";
	public static final String MODIFICA_CONFIGURAZIONI_NEGOZIO_TITLE = "Modifica Configurazione Negozio";
	public static final String CARRELLO_TITLE = "Il Mio Carrello";
	public static final String PRODOTTO_VIEW_TITLE = "Prodotto";
	public static final String CHECKOUT_TITLE = "Checkout";
	public static final String HELP_TITLE = "Richiesta Assistanza";

	public static final int NESSUNA_CATEGORIA = 1;

	public static final int MIN_HEIGHT_SIZE = 600;
	public static final int MIN_WIDTH_SIZE = 800;

	public static final double LOGIN_HEIGHT = 600;
	public static final double LOGIN_WIDTH = 1000;

	public static final int TOOLTIP_DELAY_DURATION = 1;

	public static final String DEFAULT_ADD_PRODUCT_PHOTO_PATH = "/application/images/add_product.jpg";
	public static final String DEFAULT_DASHBOARD_BG_PHOTO_PATH = "/application/images/dashboard_bg_transparent.png";

	public static final String REGEX_EMAIL_PATTERN = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
	public static final String REGEX_PHONE_PATTERN = "^\\+(?:[0-9] ?){6,14}[0-9]$";
	public static final String REGEX_PORT_NUMBER_PATTERN = "^[0-9]{1,3}$";
	public static final String REGEX_HOST_PATTERN = "(?:[a-zA-Z0-9-]+)\\.+[a-zA-Z]{2,6}$";
	public static final String REGEX_NUMBER_DECIMALS_INCLUDED = "[0-9]+(\\.[0-9][0-9]?)?";
	
	public static final long FREQUENCY = 30; //frequency time di aggiornamento in secondi
	public static final long FREQUENCY_CART_REFRESH = 60; //frequency cart refresh time
	public static final long CONVERT_NANO_TO_SECONDS = 1000000000;
	public static final String ALL_PERMISSIONS = "shop manager"; //il ruolo che ha tutti i permessi
	
	public static final String LIGHT_THEME = "LIGHT";
	public static final String DARK_THEME = "DARK";

	public static final String DEFAULT_PATH_AVATAR_FOLDER = "/src/main/resources/application/images/avatar/";

	public static enum THEME {
		AUTO, LIGHT, DARK
	};
}
