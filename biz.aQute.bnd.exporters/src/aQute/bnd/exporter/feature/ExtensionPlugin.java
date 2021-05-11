package aQute.bnd.exporter.feature;

import java.util.Arrays;
import java.util.Map;

import org.osgi.util.feature.FeatureExtension;
import org.osgi.util.feature.FeatureExtensionBuilder;
import org.osgi.util.feature.Features;

import aQute.bnd.annotation.plugin.BndPlugin;
import aQute.bnd.service.Plugin;
import aQute.service.reporter.Reporter;

@BndPlugin(name = "FeatureExtensionPlugin")
public class ExtensionPlugin implements Plugin {

    private Map<String, String> map;

    @Override
    public void setProperties(Map<String, String> map) throws Exception {

        this.map = map;

    }

    @Override
    public void setReporter(Reporter processor) {

    }

	public FeatureExtension toExtension() {

		String sType = map.getOrDefault("type", FeatureExtension.Type.ARTIFACTS.toString());
		String sState = map.getOrDefault("state", FeatureExtension.Kind.OPTIONAL.toString());
		String sName = map.getOrDefault("name", "unknmown");
		String sText = map.getOrDefault("text", "");

		FeatureExtension.Type type = FeatureExtension.Type.valueOf(sType);
		FeatureExtension.Kind state = FeatureExtension.Kind.valueOf(sState);
		FeatureExtensionBuilder featureExtensionBuilder = Features.getBuilderFactory()
			.newExtensionBuilder(sName, type, state);

		if (FeatureExtension.Type.JSON.equals(type)) {
			featureExtensionBuilder.setJSON(sText);
		} else if (FeatureExtension.Type.TEXT.equals(type)) {
			Arrays.asList(map.getOrDefault("text", "")
				.split(System.lineSeparator()))
				.forEach(featureExtensionBuilder::addText);
		}

		return featureExtensionBuilder.build();
	}

}
