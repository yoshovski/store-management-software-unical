package application.controller;

import java.util.ArrayList;
import java.util.HashMap;

import com.jfoenix.controls.JFXButton;

import application.SceneHandler;
import application.Settings;
import application.client.Client;
import application.common.Protocol;
import application.model.Category;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class CategorieController {

	@FXML
	private TableColumn<Category, String> descrizioneColumn;

	@FXML
	private JFXButton aggiungi;

	@FXML
	private JFXButton modifica;

	@FXML
	private JFXButton rimuovi;

	@FXML
	private JFXButton refresh;

	@FXML
	private TableColumn<Category, String> nomeColumn;

	@FXML
	private TableView<Category> categorieView;

	@FXML
	private TableColumn<Category, String> categoriaParentColumn;

	private ArrayList<Category> categorie = new ArrayList<Category>();
	private HashMap<Integer, String> categorieHash = new HashMap<Integer, String>();

	@FXML
	void initialize() {
		updateTable();
	}

	@FXML
	void aggiungiCategoriaWindow(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		CategoriaViewController controller = SceneHandler.getInstance().setAggiungiNuovaCategoriaScene();
		controller.loadCategories(categorie);
	}

	void updateTable() {
		categorie = Client.getInstance().getAllCategories();

		for (Category c : this.categorie)
			categorieHash.put(c.getIdCategoria(), c.getNome());

		nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
		descrizioneColumn.setCellValueFactory(new PropertyValueFactory<>("descrizione"));
		categoriaParentColumn
				.setCellValueFactory(new Callback<CellDataFeatures<Category, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<Category, String> param) {
						Integer idParentCategory = param.getValue().getIdParentCategoria();
						String nomeParentCategory = "";
						if (idParentCategory != Settings.NESSUNA_CATEGORIA)
							nomeParentCategory = categorieHash.get(idParentCategory);
						return new ReadOnlyStringWrapper(nomeParentCategory);
					}
				});
		ObservableList<Category> cat = FXCollections.observableArrayList(categorie);

		categorieView.setItems(cat);
		categorieView.getSelectionModel().selectFirst();
	}

	@FXML
	void modificaCategoria(MouseEvent event) {
		if (event.getButton().equals(MouseButton.PRIMARY) && (event.getClickCount() == 2)
				|| event.getSource().equals(modifica)) {
			
			if(categorieView.getSelectionModel().getSelectedItem().getIdCategoria()==Settings.NESSUNA_CATEGORIA){
				SceneHandler.getInstance().showError("Non puoi moificare questa categoria");
				return;
			}
			
			CategoriaViewController controller = SceneHandler.getInstance().setModificaCategoriaScene();
			controller.loadCategories(categorie);
			controller.modificaView(categorieView.getSelectionModel().getSelectedItem());
		}
	}

	@FXML
	void refreshAction(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;
		updateTable();
	}

	@FXML
	void rimuoviCategoria(MouseEvent event) {
		if (!event.getButton().equals(MouseButton.PRIMARY))
			return;

		Category category = categorieView.getSelectionModel().getSelectedItem();
		if (category == null) {
			SceneHandler.getInstance().showInfo("Categoria non selezionata", "Devi prima selezionare una categoria.");
			return;
		}

		boolean result = SceneHandler.getInstance().showConfirm("Sei sicuro?",
				"Stai per rimuovere la categoria '" + category.getNome() + "'. Vuoi proseguire?");
		if (!result)
			return;

		boolean isParentCategory = false;

		for (Category c : categorie)
			if (c.getIdParentCategoria() == category.getIdCategoria()) {
				isParentCategory = true;
				break;
			}

		boolean confirm = true;

		if (category.getIdCategoria() == Settings.NESSUNA_CATEGORIA) {
			SceneHandler.getInstance().showError("Non puoi eliminare questa categoria");
			return;
		} else if (isParentCategory)
			confirm = SceneHandler.getInstance().showConfirm("Sei sicuro?", "La categoria che vuoi eliminare contiene "
					+ "sottocategorie. Le sottocategorie di primo livello erediteranno la Categorie Genitore di \""
					+ category.getNome() + "\".");

		if (!confirm)
			return;

		String res = Client.getInstance().removeCategory(category);

		if (res.equals(Protocol.OK)) {
			String titoloAlert = "Categoria Rimossa Con Successo";
			String messageAlert = "La categoria " + category.getNome() + " è stata rimossa!";
			updateTable();
			SceneHandler.getInstance().showInfo(titoloAlert, messageAlert);
		} else
			SceneHandler.getInstance().showError(res);

	}

}
