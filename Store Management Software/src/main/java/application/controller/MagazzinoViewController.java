package application.controller;

import java.util.ArrayList;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.client.FilePicker;
import application.client.FilterType;
import application.common.FileBlob;
import application.common.Protocol;
import application.model.Category;
import application.model.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MagazzinoViewController {

	@FXML
	private Text titolo;

	@FXML
	private Rectangle imageContainer;

	@FXML
	private JFXTextField nome;

	@FXML
	private JFXComboBox<Category> categoria;

	@FXML
	private JFXTextArea descrizione;

	@FXML
	private Spinner<Integer> quantity;

	@FXML
	private Spinner<Double> prezzo;

	@FXML
	private JFXButton salva;

	@FXML
	private JFXButton aggiungi;

	@FXML
	private Tooltip toolTipPassword;

	private FileBlob productImage = null;

	private int idProdotto;

	@FXML
	void initialize() {
		titolo.setText("AGGIUNGI NUOVO PRODOTTO");
		salva.setVisible(false);
		aggiungi.setVisible(true);

		loadCategories();

		// configurazione spinner
		quantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 1));
		prezzo.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, Double.MAX_VALUE, 1, 1));

		FilePicker filePicker = new FilePicker();
		imageContainer.setFill(new ImagePattern(filePicker.getImage(Settings.DEFAULT_ADD_PRODUCT_PHOTO_PATH)));
	}

	@FXML
	void salvaAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		if (event.getSource() == salva)
			editProduct();
		else if (event.getSource() == aggiungi)
			addProduct();
	}

	private void editProduct() {
		if (blankFields()) {
			SceneHandler.getInstance().showError("Il campo nome non può essere vuoto!");
			return;
		}
		Product product = new Product(idProdotto, nome.getText(), descrizione.getText(), prezzo.getValue(),
				quantity.getValue(), productImage, categoria.getValue());
		String res = Client.getInstance().editProduct(product);

		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showError(res);
			return;
		}
		Stage stage = (Stage) salva.getScene().getWindow();
		stage.close();

		SceneHandler.getInstance().setMagazzinoScene();

		String titoloAlert = "Prodotto Modificato Con Successo";
		String messageAlert = "Il prodotto " + nome.getText() + " è stato modificato!";
		SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);

	}

	@FXML
	void addProductImage(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		FilePicker filePicker = new FilePicker(FilterType.IMAGE_FILTER);
		productImage = filePicker.getFileBlob();
		imageContainer.setFill(new ImagePattern(filePicker.getImage()));
	}

	public void loadCategories() {
		ArrayList<Category> categoryList = Client.getInstance().getAllCategories();
		categoria.setItems(FXCollections.observableList(categoryList));
		categoria.getSelectionModel().selectFirst(); // seleziona il primo elemento
	}

	public void modificaView(Product prodotto) {
		idProdotto = prodotto.getIdProdotto();
		titolo.setText("MODIFICA PRODOTTO");
		nome.setText(prodotto.getNome());
		categoria.setValue(prodotto.getCategoria());
		descrizione.setText(prodotto.getDescrizione());
		quantity.getValueFactory().setValue(prodotto.getQuantita());
		prezzo.getValueFactory().setValue(prodotto.getPrezzo());
		productImage = prodotto.getFotoFileBlob();
		imageContainer.setFill(new ImagePattern(prodotto.getFoto()));
		salva.setVisible(true);
		aggiungi.setVisible(false);
	}

	private boolean blankFields() {
		return nome.getText().isBlank();
	}

	private void addProduct() {
		if (blankFields()) {
			SceneHandler.getInstance().showError("Il campo nome non può essere vuoto");
			return;
		}
		String res = Client.getInstance().addNewProduct(new Product(nome.getText(), descrizione.getText(),
				prezzo.getValue(), quantity.getValue(), productImage, categoria.getValue()));

		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showError(res);
			return;
		}
		Stage stage = (Stage) aggiungi.getScene().getWindow();
		stage.close(); // chiude la finestra

		SceneHandler.getInstance().setMagazzinoScene();

		String titoloAlert = "Prodotto Aggiunto Con Successo";
		String messageAlert = "Il prodotto " + nome.getText() + " è stato aggiunto!";
		SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);

	}

}
