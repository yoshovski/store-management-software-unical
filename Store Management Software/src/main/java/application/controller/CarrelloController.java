package application.controller;

import java.util.HashMap;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.jfoenix.controls.JFXButton;

import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.common.Protocol;
import application.model.Product;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class CarrelloController extends AnimationTimer {

	@FXML
	private Label totaleOrdine;

	@FXML
	private JFXButton procedi;

	@FXML
	private TableView<Product> prodottiView;

	@FXML
	private TableColumn<Product, ImageView> fotoColumn;

	@FXML
	private TableColumn<Product, String> nomeColumn;

	@FXML
	private TableColumn<Product, String> descrizioneColumn;

	@FXML
	private TableColumn<Product, String> quantityColumn;

	@FXML
	private TableColumn<Product, String> prezzoColumn;

	@FXML
	private TableColumn<Product, JFXButton> rimuoviColumn;

	private Double totalOrder;

	private HashMap<Product, Integer> prodottiCarrello;

	private long previousTime = 0;

	private static boolean checkoutScene = false;

	@FXML
	void initialize() {
		updateTable();
	}

	private void calculateSum() {
		for (Product p : prodottiCarrello.keySet()) {
			double tempTotalOrder = p.getPrezzo() * prodottiCarrello.get(p);
			totalOrder += Math.round(tempTotalOrder * 100.0) / 100.0;
		}
		totaleOrdine.setText(Double.toString(totalOrder));
	}

	private void updateTable() {
		prodottiView.getItems().clear();
		prodottiCarrello = Client.cartItems;
		totalOrder = 0.00;
		if (prodottiCarrello.isEmpty())
			return;

		totaleOrdine.setText(Double.toString(totalOrder));
		calculateSum();
		prodottiView.getItems().addAll(prodottiCarrello.keySet());

		fotoColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Product, ImageView>, ObservableValue<ImageView>>() {

					@Override
					public ObservableValue<ImageView> call(CellDataFeatures<Product, ImageView> param) {
						Image image = param.getValue().getFoto();
						ImageView img = new ImageView(image);
						img.setPreserveRatio(true);
						img.setFitHeight(80);
						img.setFitWidth(80);
						return new ReadOnlyObjectWrapper<ImageView>(img);
					}
				});

		prezzoColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Product, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Product, String> param) {
						Product prod = param.getValue();
						int idProd = prod.getIdProdotto();
						double prezzo = 0;

						for (Product p : prodottiCarrello.keySet())
							if (p.getIdProdotto() == idProd)
								prezzo = p.getPrezzo() * prodottiCarrello.get(p);
						return new ReadOnlyStringWrapper(Double.toString(prezzo));
					}

				});
		quantityColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Product, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Product, String> param) {
						int quantity = 0;
						for (Product p : prodottiCarrello.keySet())
							if (param.getValue().getIdProdotto() == p.getIdProdotto())
								quantity = prodottiCarrello.get(p);
						return new ReadOnlyStringWrapper(Integer.toString(quantity));
					}
				});
		nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
		descrizioneColumn.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
		rimuoviColumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Product, JFXButton>, ObservableValue<JFXButton>>() {

					@Override
					public ObservableValue<JFXButton> call(CellDataFeatures<Product, JFXButton> param) {
						JFXButton button = new JFXButton();

						button.setPrefWidth(rimuoviColumn.getWidth() / 0.5);

						button.setGraphic(new Glyph("FontAwesome", FontAwesome.Glyph.TRASH));
						button.setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent event) {
								Product product = param.getValue();
								boolean confirm = SceneHandler.getInstance().showConfirm("Sei sicuro?",
										"Sei sicuro di voler rimuovere questo prodotto dal carrello?");
								if (!confirm)
									return;
								removeProduct(product);
							}
						});
						return new ReadOnlyObjectWrapper<JFXButton>(button);
					}
				});
		prodottiView.getSelectionModel().selectFirst();
	}

	private void removeProduct(Product p) {
		int idUtente = Client.getInstance().getUser().getIdUtente();
		int quantity = prodottiCarrello.get(p);
		String res = Client.getInstance().removeProductCart(idUtente, p.getIdProdotto(), quantity);
		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showError(res);
			return;
		}
		Client.getInstance().removeProductCart(p);
		SceneHandler.getInstance().setCarrelloScene();
		boolean confirm = SceneHandler.getInstance().showInfo("Prodotto rimosso con successo",
				"Il prodotto selezionato stato rimosso con successo dal carrello!");
		if (!confirm)
			return;
	}

	@FXML
	void continuaAcquistiAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		SceneHandler.getInstance().setProdottiScene();
		checkoutScene = true;
	}

	@FXML
	void procediOrdineAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		if (prodottiView.getItems().size() < 1) {
			SceneHandler.getInstance().showError("Il tuo carrello è vuoto...");
			return;
		}

		checkoutScene = true;
		TableView<Product> tempTable = cloneTable(prodottiView);
		CheckoutController controller = SceneHandler.getInstance().setCheckoutScene();
		tempTable.getSelectionModel().selectAll();
		controller.setTable(tempTable);
		controller.setCarrello(prodottiCarrello);
	}

	private TableView<Product> cloneTable(TableView<Product> table) {
		TableView<Product> tempTable = new TableView<Product>();
		tempTable.getColumns().addAll(table.getColumns());
		tempTable.getItems().addAll(table.getItems());
		return tempTable;
	}

	public static void setCheckoutScene(boolean confirm) {
		checkoutScene = confirm;
	}

	@Override
	public void handle(long now) {
		if ((now - previousTime) / Settings.CONVERT_NANO_TO_SECONDS >= Settings.FREQUENCY_CART_REFRESH) {
			if (!checkoutScene) {
				boolean confirm = Client.getInstance().refreshCurrentCartItems();
				if (confirm == true)
					updateTable();
			}
			previousTime = now;
		}
	}
}
