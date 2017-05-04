package algorithm.settings.domain;

import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.IntegerProperty;

@XmlRootElement()
public class MogSettings extends Settings {
	IntegerProperty mogThreshold;
	IntegerProperty handThreshold;
	IntegerProperty handMaskDilate;
}
