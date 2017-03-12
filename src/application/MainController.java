package application;

import java.io.IOException;

import image.service.ImageViewService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import utils.Utils;

public class MainController {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 512;

	@FXML
	private Button openStreamButton, closeStreamButton;
	@FXML
	private ImageView imageView;
	
	private ImageViewService imv;

	@FXML
	protected void openStream(ActionEvent event) throws IOException {
		if (imv!= null && imv.isRunning()) return;
		
		imv = new ImageViewService(IMAGE_WIDTH, IMAGE_HEIGHT);;
		imv.valueProperty().addListener((obs, oldValue, newValue) -> { Utils.updateFXControl(imageView.imageProperty(), newValue);});
		imv.start();		
	}

	@FXML
	protected void closeStream(ActionEvent event) {
		imv.cancel();
	}

	protected void setClosed() {
		// close socket
	}
}
