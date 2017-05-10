package algorithm.detector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import algorithm.detector.domain.DetectionResult;
import algorithm.settings.domain.SettingsWrapper;

/**
* This class is mainly for detecting images with background and storing them to the separate folder.
* These backgrounds are then used for Mog detection alghoritm.
* 
* @author Lukas Brchl
*/
public class BackgroundDetector extends AbstractDetector {
	
	private String savePath;

	public BackgroundDetector(SettingsWrapper settings) {
		super(settings);
	}
	
	public BackgroundDetector(SettingsWrapper settings, String savePath) {
		this(settings);
		this.savePath = savePath;
	}

	@Override
	public DetectionResult detect() {
		if (isBackgroundOnly(previewMat, 220, 30, 50) && savePath != null) {			
			Path file = Paths.get(savePath, data.getFilename());			
			try {
				Files.write(file, data.getData());
				return DetectionResult.BACKGROUND;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		return DetectionResult.UNDEFINED;
	}

	public String getSavePath() {
		return savePath;
	}


	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}	
}
