package algorithm.settings.domain;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
* Superclass for all available UI settings.
* 
* @author Lukas Brchl
*/
@XmlSeeAlso({PreprocessingSettings.class,PreviewSettings.class, MogSettings.class, EdgeDetectSettings.class})
public abstract class Settings {

}
