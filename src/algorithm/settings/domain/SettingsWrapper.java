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
	
	public Settings getByCls(Class<?> cls) {
		return settings.stream().filter(o -> o.getClass().equals(cls)).findAny().get();
	}

	@Override
	public String toString() {
		return "SettingsWrapper [settings=" + settings + "]";
	}
	
}
