package algorithm.settings.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
* Wrapper class for all available UI settings. Also helps for more comfortable storing to XML file with jaxb.
* @author Lukas Brchl
*/
@XmlRootElement()
@XmlSeeAlso({Settings.class})
public class SettingsWrapper {
	
	private Collection <Settings> settings;

	public SettingsWrapper() {
		settings = new ArrayList<Settings>(Arrays.asList(new PreviewSettings(), new PreprocessingSettings(), new MogSettings(), new EdgeDetectSettings()));
	}
	
	public SettingsWrapper(Collection<Settings> settings) {
		this.settings = settings;
	}
	
    @XmlElementRef()
	public Collection<Settings> getSettings() {
		return settings;
	}

	public void setSettings(Collection<Settings> settings) {
		this.settings = settings;
	}
	
	/**
	 * Gets corresponding setting class depending on input parameter.
	 * 
	 * @param class to look for
	 * @return corresponding setting object
	 */
	public Settings getByCls(Class<?> cls) {
		return settings.stream().filter(o -> o.getClass().equals(cls)).findAny().get();
	}

	@Override
	public String toString() {
		return "SettingsWrapper [settings=" + settings + "]";
	}
	
}
