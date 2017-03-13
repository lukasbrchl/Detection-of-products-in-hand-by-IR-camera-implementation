package image.service;

import java.nio.channels.ClosedByInterruptException;

import org.opencv.core.Mat;

import application.MainController;
import image.ImageConvertor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import network.DataReciever;
import utils.Utils;

public class ImageViewService extends Service<Mat> {
	
	private final MainController mainController;
	private final int width;
	private final int height;
	private final ImageConvertor imageConvertor;
	private final DataReciever dataReciever;

	public ImageViewService(MainController mainController, ImageConvertor imageConvertor, DataReciever dataReciever, int width, int height) {
		this.mainController = mainController;
		this.imageConvertor = imageConvertor;
		this.dataReciever = dataReciever;
		this.width = width;
		this.height = height;
	}
	
	
	
	@Override
	protected Task<Mat> createTask() {
		return new Task<Mat>() {
			@Override protected Mat call() {				
				Mat convertedMat = null;
				byte[] byteArray;
				
				dataReciever.initSocket();
				try {
					byteArray = dataReciever.getImageFromStream();
					while (byteArray != null && byteArray.length != 0) {
						if (mainController.getScaleTempCheckbox().isSelected()) {							
							convertedMat = imageConvertor.convertBinaryToMat(byteArray, (float) mainController.getMinTempSlider().getValue(), (float) mainController.getMaxTempSlider().getValue());	
						} else
							convertedMat = imageConvertor.convertBinaryToMat(byteArray);	
						updateValue(convertedMat);
						if (dataReciever.isPaused()) 
							updateMessage("Paused");
						else
							updateMessage("Streaming");
						byteArray = dataReciever.getImageFromStream();						
					}	
					
				} catch (InterruptedException | ClosedByInterruptException ex ) { //catch Thread.sleep()
				}
				updateMessage("Stream closed");
				return null;
			}
		};
	}
}
