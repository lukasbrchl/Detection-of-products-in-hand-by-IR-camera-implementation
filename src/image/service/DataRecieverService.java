package image.service;

import java.nio.channels.ClosedByInterruptException;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;

import application.MainController;
import image.ImageConvertor;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import network.DataReciever;
import network.domain.ImageData;
import utils.Utils;

public class DataRecieverService extends Service<ImageData> {
	
	private final DataReciever dataReciever;

	public DataRecieverService(DataReciever dataReciever) {
		this.dataReciever = dataReciever;
	}
	
	@Override
	protected Task<ImageData> createTask() {
		
		return new Task<ImageData>() {
			@Override protected ImageData call() {				
				byte[] byteArray;				
				try {
					byteArray = dataReciever.getImageFromStream();
					while (byteArray != null && byteArray.length != 0 && !isCancelled()) {
						ImageData data = new ImageData(byteArray);
						updateValue(data);
						updateMessage(dataReciever.getStatus().getStrStatus());
						byteArray = dataReciever.getImageFromStream();						
					}	
					
				} catch (InterruptedException | ClosedByInterruptException ex ) { //catch Thread.sleep()
				}
				updateMessage(dataReciever.getStatus().getStrStatus());
				return null;
			}
		};
	}
}
