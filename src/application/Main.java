package application;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application{
	public static final float MIN_TEMP = 26.0f;
	public static final float MAX_TEMP = 36.0f;
	
//	public static void main(String[] args) throws IOException, InterruptedException {
//        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
//        ImageConvertor imageConvertor = new ImageConvertor(640, 512);
//        ImageViewer imViewer1 = new ImageViewer(); ImageViewer imViewer2 = new ImageViewer(); ImageViewer imViewer3 = new ImageViewer(); ImageViewer imViewer4 = new ImageViewer(); ImageViewer imViewer5 = new ImageViewer();
//        ImageViewer imViewer6 = new ImageViewer(); ImageViewer imViewer7 = new ImageViewer(); ImageViewer imViewer8 = new ImageViewer(); ImageViewer imViewer9 = new ImageViewer(); ImageViewer imViewer10 = new ImageViewer();
//
//		Path backgroundPath = Paths.get("img/binary/background.bin");
//		Path keysPath = Paths.get("img/binary/keys.bin");
//		byte[] backgroundData = Files.readAllBytes(backgroundPath);
//		byte[] keysData = Files.readAllBytes(keysPath);
//		}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MainLayout.fxml"));
			Parent root = (Parent) loader.load();
//			BorderPane rootElement = (BorderPane) loader.load();
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setTitle("ThesisProject");
			primaryStage.setScene(scene);
			primaryStage.show();			
			MainController controller = loader.getController();
			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we)
				{
					controller.setClosed();
				}
			}));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME); //load OpenCV		
		launch(args);
	}
	
	
}
