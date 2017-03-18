package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	
   private static Config instance = null;

   public static final String SOCKET_HOSTNAME = "socket.hostname";
   public static final String SOCKET_PORT = "socket.port";
   public static final String DUMMY_PATH = "image.dummy.path";
   public static final String IMAGE_SAVE = "image.save.path";
   public static final String DUMMY_SLEEP = "image.dummy.playback.sleep";
   
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
