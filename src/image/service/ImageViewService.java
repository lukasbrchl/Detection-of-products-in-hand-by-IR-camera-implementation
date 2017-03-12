package image.service;

import image.ImageConvertor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import network.DataReciever;
import utils.Utils;

public class ImageViewService extends Service<Image> {
	
	private final int width;
	private final int height;

	public ImageViewService(int width, int height) {
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
					while (byteArray != null && byteArray.length != 0) { // && !isCancelled()
						convertedImage = imageConvertor.convertBinaryToImage(byteArray);	
						updateValue(convertedImage);
						byteArray = dr.getImageFromFakeStream();						
					}	
				} catch (InterruptedException ex) { //catch Thread.sleep()
				}
				return null;
			}
		};
	}
}
