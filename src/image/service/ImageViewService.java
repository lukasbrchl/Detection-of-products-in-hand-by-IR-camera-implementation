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
import utils.Utils;

public class ImageViewService extends Service<List<Mat>> {
	
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
	protected Task<List<Mat>> createTask() {
		
		return new Task<List<Mat>>() {
			@Override protected List<Mat> call() {				
				List<Mat> resultMats;; //FIXME: Make object wrapper instead. First in list is maybe normalized, second is original.
				Mat original; 
				byte[] byteArray;
				
				try {
					byteArray = dataReciever.getImageFromStream();
					while (byteArray != null && byteArray.length != 0 && !isCancelled()) {
						resultMats = new LinkedList<>();
						original = imageConvertor.convertBinaryToMat(byteArray);
						if (mainController.getScaleTempCheckbox().isSelected())						
							resultMats.add(imageConvertor.convertBinaryToMat(byteArray, (float) mainController.getMinTempSlider().getValue(), (float) mainController.getMaxTempSlider().getValue()));	
						else
							resultMats.add(original);	
						resultMats.add(original); 
						updateValue(resultMats);
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
