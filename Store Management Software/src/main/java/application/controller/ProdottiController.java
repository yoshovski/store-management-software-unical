package application.controller;

import java.util.ArrayList;

import com.jfoenix.controls.JFXButton;

import application.SceneHandler;
import application.client.Client;
import application.common.Protocol;
import application.model.Product;
import application.model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class ProdottiController {

	@FXML
	private JFXButton refresh;

	@FXML
	private JFXButton visualizza;

	@FXML
	private JFXButton aggiungiAlCarrello;

	@FXML
	private TableView<Product> prodottiView;

	@FXML
	private TableColumn<Product, ImageView> fotoColumn;

	@FXML
	private TableColumn<Product, String> nomeColumn;

	@FXML
	private TableColumn<Product, String> descrizioneColumn;

	@FXML
	private TableColumn<Product, String> categoriaColumn;

	@FXML
	private TableColumn<Product, Double> prezzoColumn;

	private Product currentProduct;
	private User user = Client.getInstance().getUser();

	@FXML
	void initialize() {
		updateTable();
		prodottiView.getSelectionModel().selectFirst();
	}

	private void updateTable() {
		// specifica le regole di popolamento delle colonne della tabella prodottiView
		nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
		descrizioneColumn.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
		categoriaColumn.setCellValueFactory(new Callback<CellDataFeatures<Product, String>, ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(CellDataFeatures<Product, String> productObject) {
				return new ReadOnlyStringWrapper(productObject.getValue().getCategoria().getNome());
			}
		});
		fotoColumn
				.setCellValueFactory(new Callback<CellDataFeatures<Product, ImageView>, ObservableValue<ImageView>>() {
					@Override
					public ObservableValue<ImageView> call(CellDataFeatures<Product, ImageView> userObject) {
						Image img = userObject.getValue().getFoto();
						ImageView foto = new ImageView(img);
						foto.setPreserveRatio(true);
						foto.setFitHeight(80);
						foto.setFitWidth(80);
						return new ReadOnlyObjectWrapper<ImageView>(foto);
					}
				});
		prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));

		ArrayList<Product> productList = Client.getInstance().getAllProducts();
		ObservableList<Product> prodotti = FXCollections.observableArrayList(productList);
		prodottiView.setItems(prodotti);

	}

	@FXML
	void aggiungiAlCarrelloAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		currentProduct = prodottiView.getSelectionModel().getSelectedItem();
		if (currentProduct == null) {
			SceneHandler.getInstance().showError("Nessun prodotto selezionato da aggiungere al carrello");
			return;
		}
		int productQuantity = 1;
		String res = Client.getInstance().reserveProduct(currentProduct.getIdProdotto(), productQuantity,
				user.getIdUtente());

		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showError(res);
			updateTable();
			return;
		}
		boolean newItem = Client.getInstance().addToCart(currentProduct, productQuantity);
		String titoloAlert = "Prodotto Aggiunto al Carrello";
		String messageAlert = "Il prodotto " + currentProduct.getNome() + " è stato aggiunto al tuo Carrello!";
		if (!newItem) {
			titoloAlert = "Prodotto Modificato nel Carrello";
			messageAlert = "La quantità del prodotto " + currentProduct.getNome() + " è stata modificata nel carrello!";
		}
		SceneHandler.getInstance().setProdottiScene();
		SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
	}

	@FXML
	void refreshAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		prodottiView.getItems().clear();
		updateTable();
	}

	@FXML
	void visualizzaProdottoAction(MouseEvent event) {
		if (prodottiView.getItems().isEmpty())
			return;

		if ((event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
				|| event.getSource().equals(visualizza)) {
			ProdottoViewController controller = SceneHandler.getInstance().setVisualizzaProdottoScene();
			Product prodotto = prodottiView.getSelectionModel().getSelectedItem();
			if (prodotto == null) {
				boolean clicked = SceneHandler.getInstance().showInfo("Prodotto non selezionato",
						"Devi prima selezionare un prodotto.");
				if (clicked)
					return;
			} else
				controller.modificaView(prodotto);
		}

	}

}
