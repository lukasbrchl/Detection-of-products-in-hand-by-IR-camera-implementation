package application;

import java.io.IOException;

import image.service.ImageViewService;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import utils.Utils;

public class MainController {
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 512;

	@FXML private Button openStreamButton, closeStreamButton;
	@FXML private ImageView imageView;
	@FXML private Label streamStatus;
	@FXML private Label maxTempLabel, minTempLabel;
	@FXML private Slider maxTempSlider, minTempSlider;
	@FXML public CheckBox scaleTempCheckbox;
	@FXML private AnchorPane minAnchorPane, maxAnchorPane;
	
	private ImageViewService imv;

	public void init() {
		
	}
	
	 @FXML 
	 public void initialize() {
		 minTempLabel.textProperty().bind(Bindings.format("%.2f",minTempSlider.valueProperty()));
		 maxTempLabel.textProperty().bind(Bindings.format("%.2f",maxTempSlider.valueProperty()));
    }
	
	@FXML
	protected void openStream(ActionEvent event) throws IOException {
		if (imv!= null && imv.isRunning()) return;
		imv = new ImageViewService(this, IMAGE_WIDTH, IMAGE_HEIGHT);
		imv.valueProperty().addListener((obs, oldValue, newValue) -> { Utils.updateFXControl(imageView.imageProperty(), newValue);});
		streamStatus.textProperty().bind(imv.messageProperty());
		imv.start();		
	}

	
	@FXML
	protected void closeStream(ActionEvent event) {
		imv.cancel();
	}

	@FXML 
	protected void scaleTempCheckboxClicked() {
		if(scaleTempCheckbox.isSelected()) {
			minAnchorPane.setDisable(false);
			maxAnchorPane.setDisable(false);
		}
		else {
			minAnchorPane.setDisable(true);
			maxAnchorPane.setDisable(true);
		}
		
	}
	protected void setClosed() {
		// close socket
	}

	public final Label getMaxTempLabel() {
		return maxTempLabel;
	}

	public final void setMaxTempLabel(Label maxTempLabel) {
		this.maxTempLabel = maxTempLabel;
	}

	public final Label getMinTempLabel() {
		return minTempLabel;
	}

	public final void setMinTempLabel(Label minTempLabel) {
		this.minTempLabel = minTempLabel;
	}

	public final Slider getMaxTempSlider() {
		return maxTempSlider;
	}

	public final void setMaxTempSlider(Slider maxTempSlider) {
		this.maxTempSlider = maxTempSlider;
	}

	public final Slider getMinTempSlider() {
		return minTempSlider;
	}

	public final void setMinTempSlider(Slider minTempSlider) {
		this.minTempSlider = minTempSlider;
	}

	public final CheckBox getScaleTempCheckbox() {
		return scaleTempCheckbox;
	}

	public final void setScaleTempCheckbox(CheckBox scaleTempCheckbox) {
		this.scaleTempCheckbox = scaleTempCheckbox;
	}

	public final AnchorPane getMinAnchorPane() {
		return minAnchorPane;
	}

	public final void setMinAnchorPane(AnchorPane minAnchorPane) {
		this.minAnchorPane = minAnchorPane;
	}

	public final AnchorPane getMaxAnchorPane() {
		return maxAnchorPane;
	}

	public final void setMaxAnchorPane(AnchorPane maxAnchorPane) {
		this.maxAnchorPane = maxAnchorPane;
	}
	
	
}
