package data.reciever.domain;

/**
* Enum for holding correct client stream state.
* 
* @author Lukas Brchl
*/
public enum Status {
	CONNECTED("Connected"),
	STREAMING("Streaming"),
	PAUSED("Paused"),
	CLOSED("Closed");
	
	private final String strStatus;
	private Status (String strStatus) {
		this.strStatus = strStatus;
	}
	public String getStrStatus() {
		return strStatus;
	}
}