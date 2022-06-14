package application.controller;

import java.util.ArrayList;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import application.SceneHandler;
import application.client.Client;
import application.common.Protocol;
import application.model.Category;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CategoriaViewController {

	@FXML
	private JFXTextField descrizione;

	@FXML
	private Text titolo;

	@FXML
	private JFXButton aggiungi;

	@FXML
	private JFXComboBox<Category> categoriaParent;

	@FXML
	private JFXTextField nome;

	@FXML
	private JFXButton salva;

	private ArrayList<Category> categoryList;

	private Category currentCategory;

	@FXML
	void initialize() {
		aggiungi.setVisible(true);
		salva.setVisible(false);
	}

	@FXML
	void salvaAction(ActionEvent event) {
		if (event.getSource().equals(aggiungi))
			addCategory();
		else if (event.getSource().equals(salva))
			editCategory();
	}

	public void loadCategories(ArrayList<Category> categories) {
		categoryList = categories;
		categoriaParent.setItems(FXCollections.observableList(categoryList));
		categoriaParent.getSelectionModel().selectFirst(); // seleziona il primo elemento
	}

	private void addCategory() {
		int idCategoriaParent = categoriaParent.getValue().getIdCategoria();

		String res = Client.getInstance()
				.addNewCategory(new Category(nome.getText(), descrizione.getText(), idCategoriaParent));

		if (res.equals(Protocol.OK)) {
			// chiude la finestra
			Stage stage = (Stage) salva.getScene().getWindow();
			stage.close();

			SceneHandler.getInstance().setCategorieScene();

			String titoloAlert = "Categoria Aggiunta Con Successo";
			String messageAlert = "La categoria " + nome.getText() + " è stata aggiunta!";
			SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
		} else
			SceneHandler.getInstance().showError(res);

	}

	public void modificaView(Category selectedItem) {
		currentCategory = selectedItem;
		salva.setVisible(true);
		aggiungi.setVisible(false);

		nome.setText(currentCategory.getNome());
		descrizione.setText(currentCategory.getDescrizione());

		for (int i = 0; i < categoryList.size(); i++)
			if (categoryList.get(i).getIdCategoria() == currentCategory.getIdParentCategoria())
				categoriaParent.getSelectionModel().select(categoryList.get(i));

	}

	private void editCategory() {
		int newIdParent = categoriaParent.getSelectionModel().getSelectedItem().getIdCategoria();

		Category editedCategory = new Category(currentCategory.getIdCategoria(), nome.getText(), descrizione.getText(),
				newIdParent);
		String res = Client.getInstance().editCategory(editedCategory);

		if (res.equals(Protocol.OK)) {
			// chiude la finestra
			Stage stage = (Stage) aggiungi.getScene().getWindow();
			stage.close();

			SceneHandler.getInstance().setCategorieScene();

			String titoloAlert = "Categoria Modificata Con Successo";
			String messageAlert = "La categoria " + nome.getText() + " è stata modificata!";
			SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
		} else
			SceneHandler.getInstance().showError(res);

	}
}
