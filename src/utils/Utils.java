package utils;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;

/**
* General helper class
* @author Lukas Brchl
*/
public class Utils {
	
	 /**
	   * Updates GUI component from a non-GUI thread.
	   * Adding an update request into a queue and waiting for GUI thread to handle it.
	   * 
	   * @param property property to update
	   * @param value value to set
	   */
	public static <T> void updateFXControl(final ObjectProperty<T> property, final T value)	{
		Platform.runLater(() -> {
			property.set(value);
		});
	}
	
}
