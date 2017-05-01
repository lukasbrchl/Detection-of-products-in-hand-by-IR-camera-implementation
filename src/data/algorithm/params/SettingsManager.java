package data.algorithm.params;

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

public class SettingsManager {
	
	public static final String preprocFilename = "_preprocessingSettings.xml";

	public static PreprocessingSettings loadPreproc(String path) {
		try {
			File file = new File(path + "/" + preprocFilename);
			JAXBContext jaxbContext = JAXBContext.newInstance(PreprocessingSettings.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			PreprocessingSettings ps = (PreprocessingSettings) jaxbUnmarshaller.unmarshal(file);
			return ps;
		  } catch (JAXBException e) {
			e.printStackTrace();
		  }	
		return null;
	}
	
	public static void storePreproc(PreprocessingSettings ps, String path) throws IOException {
		try {
			File file = new File(path + "/" + preprocFilename);		
			JAXBContext jaxbContext = JAXBContext.newInstance(PreprocessingSettings.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();	
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);	// output pretty printed		
			jaxbMarshaller.marshal(ps, file);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

}
