package application.controller;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.transitions.hamburger.HamburgerBasicCloseTransition;

import application.SceneHandler;
import application.Settings;
import application.Settings.THEME;
import application.client.Client;
import application.common.Logger;
import application.model.User;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class MainFrameController extends AnimationTimer {

	@FXML
	private JFXHamburger ham;

	@FXML
	private JFXDrawer drawer;

	@FXML
	private AnchorPane mainPane;

	@FXML
	private Circle avatarContainer;

	private HamburgerBasicCloseTransition transition;

	private User user = Client.getInstance().getUser();

	private boolean allPermissions = user.hasRole(Settings.ALL_PERMISSIONS);

	@FXML
	void initialize() {
		try {
			THEME theme = Client.getInstance().getCurrentTheme();
			SceneHandler.getInstance().changeThemeUI(theme);
			FXMLLoader loader = SceneHandler.getInstance().loadFXML(Settings.SLIDERMENU_FXML);
			ScrollPane sliderMenu = loader.load();
			drawer.setSidePane(sliderMenu);
			transition = new HamburgerBasicCloseTransition(ham);
			transition.setRate(-1);
			setAvatar();
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			Logger.getInstance().captureException(e, errorMessage);
		}
	}

	void setAvatar() {
		User user = Client.getInstance().getUser();
		avatarContainer.setFill(new ImagePattern(user.getAvatar()));
	}

	void transitionHamburger() {
		transition.setRate(transition.getRate() * -1);
		transition.play();
	}

	public boolean getPermissions() {
		return allPermissions;
	}

	@FXML
	void slideMenu(MouseEvent event) {
		transitionHamburger();
		if (!drawer.isOpened())
			drawer.open();
		else
			drawer.close();
	}

	@Override
	public void handle(long now) {
		if (Client.getInstance().userEdited()) {
			setAvatar();
			Client.getInstance().setUserEdited(false);
		} else if (Client.getInstance().themeChanged()) {
			System.out.println(Client.getInstance().getCurrentTheme());
			THEME currentTheme = Client.getInstance().getCurrentTheme();
			SceneHandler.getInstance().changeThemeUI(currentTheme);
			Client.getInstance().setThemeEdited(false);
		}

	}

	public void setMainPane(Node pane) {
		mainPane.getChildren().clear();
		mainPane.getChildren().add(pane);
	}
}
