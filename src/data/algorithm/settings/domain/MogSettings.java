package data.algorithm.settings.domain;

import javax.xml.bind.annotation.XmlRootElement;

import javafx.beans.property.IntegerProperty;

@XmlRootElement(name="mogSettings")
public class MogSettings {
	IntegerProperty mogThreshold;
	IntegerProperty handThreshold;
	IntegerProperty handMaskDilate;
}
