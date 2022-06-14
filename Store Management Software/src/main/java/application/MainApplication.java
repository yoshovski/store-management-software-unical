package application;

import javafx.application.Application;
import javafx.stage.Stage;
import application.common.Logger;

public class MainApplication extends Application {

	public static void main(String[] args) {
		Logger.getInstance().setLogger("Client");
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		SceneHandler.getInstance().init(primaryStage);
	}

}
