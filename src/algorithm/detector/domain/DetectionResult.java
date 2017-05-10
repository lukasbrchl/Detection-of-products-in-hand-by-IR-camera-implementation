package algorithm.detector.domain;

/**
* Enum classificating detection result.
*  
* @author Lukas Brchl
*/
public enum DetectionResult {
	EMPTY_HAND("Empty hand", "empty_hand"),
	HAND_WITH_GOODS("Empty hand", "hand_with_goods"),
	BACKGROUND("Background", "background"),
	UNDEFINED("Undefined", "undefined");
	
	private final String result, path;	
	private DetectionResult (String result, String path) {
		this.result = result;
		this.path = path;
	}
	
	public String getResult() {
		return result;
	}
	
	public String getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		return result;
	}
	
	
}
