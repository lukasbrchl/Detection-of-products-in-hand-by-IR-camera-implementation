package application;

public class HandEnum {

	public enum Parts {
		ARM("Arm detected"),
		HAND("Hand detected"),
		NONE("Nothing interesting");
		
		private String label;
		Parts(String label) {
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
	}
	
}
