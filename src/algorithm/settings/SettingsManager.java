package algorithm.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import algorithm.settings.domain.PreprocessingSettings;
import algorithm.settings.domain.PreviewSettings;
import algorithm.settings.domain.Settings;
import algorithm.settings.domain.SettingsWrapper;

/**
* Static class for storing and retrieving UI settings.
* 
* @author Lukas Brchl
*/
public final class SettingsManager {
	
	public static final String settingsFilename = "_settings.xml";
	
	private SettingsManager() {}

	public static SettingsWrapper loadSettings(String path) {
		try {
			File file = new File(createSettingsPath(path));
			JAXBContext jaxbContext = JAXBContext.newInstance(SettingsWrapper.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			return (SettingsWrapper) jaxbUnmarshaller.unmarshal(file);
		  } catch (JAXBException e) {
			e.printStackTrace();
		  }	
		return null;
	}
	
	public static void storeSettings(SettingsWrapper settings, String path) throws IOException {
		try {
			File file = new File(createSettingsPath(path));		
			JAXBContext jaxbContext = JAXBContext.newInstance(SettingsWrapper.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();	
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);	// output pretty printed		
			jaxbMarshaller.marshal(settings, file);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public static String createSettingsPath (String path) {
		return path + "/" + settingsFilename;
	}
		
	public static boolean fileExists(String path) {
		return new File(createSettingsPath(path)).exists();
	}

}
