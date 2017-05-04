package algorithm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import algorithm.domain.DetectionResult;
import algorithm.settings.domain.SettingsWrapper;

public class BackgroundDetect extends AbstractDetect {
	
	private String savePath;

	public BackgroundDetect(SettingsWrapper settings) {
		super(settings);
	}
	
	public BackgroundDetect(SettingsWrapper settings, String savePath) {
		this(settings);
		this.savePath = savePath;
	}
	

	@Override
	public void detect() {
		if (isBackgroundOnly(previewMat) && savePath != null) {			
			Path file = Paths.get(savePath, data.getFilename());
			result = DetectionResult.BACKGROUND;
			try {
				Files.write(file, data.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		else {
			result = DetectionResult.UNDEFINED;
		}
	}


	public String getSavePath() {
		return savePath;
	}


	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}	
}
