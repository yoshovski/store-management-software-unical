package application.controller;

import com.jfoenix.controls.JFXButton;

import application.SceneHandler;
import application.client.Client;
import application.common.Protocol;
import application.model.Product;
import application.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ProdottoViewController {

	@FXML
	private Text productName;

	@FXML
	private Label productPrice;

	@FXML
	private ImageView productImage;

	@FXML
	private Label productCategory;

	@FXML
	private Label productDescription;

	@FXML
	private Spinner<Integer> productQuantity;

	@FXML
	private JFXButton close;

	@FXML
	private JFXButton addToCart;

	private Product currentProduct;

	private User user = null;

	@FXML
	void initialize() {
		user = Client.getInstance().getUser();
	}

	@FXML
	void salvaAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		System.out.println("ProdottoViewController.salvaAction()");
		int idProdotto = currentProduct.getIdProdotto();
		System.out.println(idProdotto + "*" + productQuantity.getValue());
		int quantity = productQuantity.getValue().intValue();

		String res = Client.getInstance().reserveProduct(idProdotto, quantity, user.getIdUtente());
		if (!res.equals(Protocol.OK)) {
			SceneHandler.getInstance().showError(res);
			return;
		}
		boolean newItem = Client.getInstance().addToCart(currentProduct, quantity);
		String titoloAlert = "Prodotto Aggiunto al Carrello";
		String messageAlert = "Il prodotto " + productName.getText() + " è stato aggiunto al tuo Carrello!";
		if (!newItem) {
			titoloAlert = "Prodotto Modificato nel Carrello";
			messageAlert = "La quantità del prodotto " + productName.getText() + "è stata modificata nel carrello!";
		}
		Stage stage = (Stage) addToCart.getScene().getWindow();
		stage.close(); // chiude la finestra

		SceneHandler.getInstance().setProdottiScene();
		SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
	}

	@FXML
	void closeAction(ActionEvent event) {
		Stage stage = (Stage) close.getScene().getWindow();
		stage.close();
	}

	public void modificaView(Product prodotto) {
		currentProduct = prodotto;
		productName.setText(prodotto.getNome());
		productCategory.setText(String.valueOf(prodotto.getCategoria().getNome()));
		productDescription.setText(prodotto.getDescrizione());
		int maxQuantity = prodotto.getQuantita();
		productQuantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQuantity, 1));
		productPrice.setText(String.valueOf(prodotto.getPrezzo()));
		productImage.setImage(prodotto.getFoto());
	}

}
