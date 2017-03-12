package image.service;

import java.nio.channels.ClosedByInterruptException;

import application.MainController;
import image.ImageConvertor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import network.DataReciever;
import utils.Utils;

public class ImageViewService extends Service<Image> {
	
	private final MainController mainController;
	private final int width;
	private final int height;

	public ImageViewService(MainController mainController, int width, int height) {
		this.mainController = mainController;
		this.width = width;
		this.height = height;
	}
	
	@Override
	protected Task<Image> createTask() {
		return new Task<Image>() {
			@Override protected Image call() {				
				ImageConvertor imageConvertor = new ImageConvertor(width, height);
				DataReciever dr = new DataReciever("fake", 0, 0);
				Image convertedImage = null;
				byte[] byteArray;
				
				dr.initSocket();
				try {
					byteArray = dr.getImageFromFakeStream();
					while (byteArray != null && byteArray.length != 0) {
						if (mainController.getScaleTempCheckbox().isSelected()) {							
							convertedImage = imageConvertor.convertBinaryToImage(byteArray, (float) mainController.getMinTempSlider().getValue(), (float) mainController.getMaxTempSlider().getValue());	
						} else
							convertedImage = imageConvertor.convertBinaryToImage(byteArray);	
						updateValue(convertedImage);
						updateMessage("Reciving");
						byteArray = dr.getImageFromFakeStream();						
					}	
					
				} catch (InterruptedException | ClosedByInterruptException ex ) { //catch Thread.sleep()
				}
				updateMessage("Done");
				return null;
			}
		};
	}
}
