package application.controller;

import java.util.ArrayList;
import com.jfoenix.controls.JFXButton;

import application.SceneHandler;
import application.client.Client;
import application.common.Protocol;
import application.model.Product;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class MagazzinoController {

	@FXML
	private TableColumn<Product, String> descrizioneColumn;

	@FXML
	private TableColumn<Product, String> categoriaColumn;

	@FXML
	private TableColumn<Product, String> quantityColumn;

	@FXML
	private JFXButton aggiungi;

	@FXML
	private JFXButton modifica;

	@FXML
	private JFXButton rimuovi;

	@FXML
	private JFXButton refresh;

	@FXML
	private TableColumn<Product, String> nomeColumn;

	@FXML
	private TableView<Product> prodottiView;

	@FXML
	private TableColumn<Product, ImageView> fotoColumn;

	@FXML
	private TableColumn<Product, Double> prezzoColumn;

	@FXML
	private TableColumn<Product, Integer> idColumn;

	@FXML
	void initialize() {
		updateTable();
		prodottiView.getSelectionModel().selectFirst();
	}

	private void updateTable() {
		// specifica le regole di popolamento delle colonne della tabella prodottiView
		idColumn.setCellValueFactory(new PropertyValueFactory<>("idProdotto"));
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

		quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantita"));
		prezzoColumn.setCellValueFactory(new PropertyValueFactory<>("prezzo"));

		ArrayList<Product> productList = Client.getInstance().getAllMagazzinoProducts();
		ObservableList<Product> prodotti = FXCollections.observableArrayList(productList);
		prodottiView.setItems(prodotti);

	}

	@FXML
	void aggiungiProdottoWindow(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setAggiungiProdottoScene();
	}

	@FXML
	void modificaProdotto(MouseEvent event) {
		if (prodottiView.getItems().isEmpty())
			return;

		if ((event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)
				|| event.getSource().equals(modifica)) {
			MagazzinoViewController controller = SceneHandler.getInstance().setModificaProdottoScene();
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

	@FXML
	void refreshAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		prodottiView.getItems().clear();
		updateTable();
	}

	@FXML
	void rimuoviProdotto(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		Product product = prodottiView.getSelectionModel().getSelectedItem();
		if (product == null) {
			SceneHandler.getInstance().showInfo("Prodotto non selezionato", "Devi prima selezionare un prodotto.");
			return;
		}
		boolean confirm = SceneHandler.getInstance().showConfirm(
				"Rimuovi: " + product.getIdProdotto() + " - " + product.getNome(),
				"Sei sicuro di voler eliminare il prodotto " + product.getIdProdotto() + " - " + product.getNome()
						+ " ?");

		if (confirm) {
			String res = Client.getInstance().removeProduct(product.getIdProdotto());
			if (res.equals(Protocol.OK)) {
				String titoloAlert = "Prodotto Rimosso Con Successo";
				String messageAlert = "Il prodotto (" + product.getIdProdotto() + " - " + product.getNome()
						+ ") è stato rimosso!";
				updateTable();
				SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
			} else
				SceneHandler.getInstance().showError(res);
		}
	}

}