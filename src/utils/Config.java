package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
* Singleton object class for working with stored config file.
* @author Lukas Brchl
*/
public class Config {
	
   private static Config instance = null;

   public static final String SOCKET_HOSTNAME = "socket.hostname";
   public static final String SOCKET_PORT = "socket.port";
   public static final String FLIR_DUMMY_PATH = "image.dummy.path.flir";
   public static final String WEBCAM_DUMMY_PATH= "image.dummy.path.webcam";
   public static final String FLIR_IMAGE_SAVE = "image.save.path.flir";
   public static final String WEBCAM_IMAGE_SAVE = "image.save.path.webcam";
   
   private static final String CONFIG_PATH = "conf/config.properties";
   private Properties properties;
   
   public static Config getInstance() {
      if(instance == null) {
         instance = new Config();
      }
      return instance;
   }
   
   protected Config() {
	try {
		properties = new Properties();
		InputStream input = new FileInputStream(CONFIG_PATH);
		properties.load(input);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
   }
   
	public String getValue(String key) {
		return properties.get(key).toString();
	}
	
}
