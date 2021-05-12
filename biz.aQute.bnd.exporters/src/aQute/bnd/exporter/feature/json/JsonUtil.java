package aQute.bnd.exporter.feature.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.osgi.util.feature.Feature;
import org.osgi.util.feature.FeatureConfiguration;
import org.osgi.util.feature.ID;

import aQute.bnd.unmodifiable.Lists;

public class JsonUtil {

	public static Map<String, String> fromID(ID id) {
		if (Objects.isNull(id)) {
			return null;
		}
		Map<String, String> jsonMapFeatureID = new LinkedHashMap<>();
		jsonMapFeatureID.put("groupID", id.getGroupId());
		jsonMapFeatureID.put("artifactId", id.getArtifactId());
		jsonMapFeatureID.put("version", id.getVersion());
		jsonMapFeatureID.put("classifier", id.getClassifier());
		jsonMapFeatureID.put("type", id.getType());

		return jsonMapFeatureID;

	}

	public static Map<String, Object> createJsonFreatureMap(Feature feature) {

		Map<String, Object> jsonMapFeature = new LinkedHashMap<>();
		// ID
		Map<String, String> id = fromID(feature.getID());
		jsonMapFeature.put("id", id);
		// Meta
		jsonMapFeature.put("name", feature.getName());
		jsonMapFeature.put("description", feature.getDescription());
		jsonMapFeature.put("vendor", feature.getVendor());
		jsonMapFeature.put("license", feature.getLicense());

		comment(jsonMapFeature, "A complete feature has no external dependencies");
		jsonMapFeature.put("complete", feature.isComplete());

		comment(jsonMapFeature,
			"variables used in configuration and framework properties are substituted at launch time.");
		jsonMapFeature.put("variables", feature.getVariables());

		Map<String, Map<String, Object>> jsonConfigurationsMap = createJsonConfigurationMap(
			feature.getConfigurations());
		comment(jsonMapFeature,
			Lists.of("The configurations are specified following the format defined by the OSGi Configurator",
				"specification: https://osgi.org/specification/osgi.cmpn/7.0.0/service.configurator.html",
				"Variables declared in the variables section can be used for late binding of variables",
				"they can be specified with the Launcher, or the default from the variables section is used.",
				"Factory configurations can be specified using the named factory syntax, which separates",
				"The factory PID and the name with a tilde '~'"));
		jsonMapFeature.put("configuration", jsonConfigurationsMap);

		comment(jsonMapFeature,
			Lists.of("The bundles that are part of the feature. Bundles are referenced using Maven",
				"coordinates and can have additional metadata associated with them. Bundles can",
				"specified as either a simple string (the Maven coordinates of the bundle) or",
				"as an object with 'id' and additional metadata."));
		jsonMapFeature.put("bundles", null);
		return jsonMapFeature;
	}

	private static Map<String, Map<String, Object>> createJsonConfigurationMap(
		Map<String, FeatureConfiguration> configurationsMap) {

		if (Objects.isNull(configurationsMap)) {
			return null;
		}
		Map<String, Map<String, Object>> jsonConfigurationMap = new LinkedHashMap<>();
		for (Entry<String, FeatureConfiguration> configurationsEntry : configurationsMap.entrySet()) {
			Entry<String, Map<String, Object>> jsonConfigurationEntry = createJsonConfigurationEntry(
				configurationsEntry);
			jsonConfigurationMap.put(jsonConfigurationEntry.getKey(), jsonConfigurationEntry.getValue());
		}
		return jsonConfigurationMap;
	}

	public static Entry<String, Map<String, Object>> createJsonConfigurationEntry(
		Entry<String, FeatureConfiguration> entry) {
		FeatureConfiguration featureConfiguration = entry.getValue();
		if (Objects.isNull(featureConfiguration)) {
			return null;
		}
		Map<String, Object> jsonConfigurationEntryValueMap = new LinkedHashMap<>();
		Map<String, Object> configurationValuesMap = featureConfiguration.getValues();
		for (Entry<String, Object> configurationValuesEntry : configurationValuesMap.entrySet()) {

			String jsonKey = configurationValuesEntry.getKey();
			Object jsonValue = configurationValuesEntry.getValue();
			// TODO: Ask P.K how Converter could help to get the Name of The
			// type as String
			String type = TypeConverter.convertObjectToTyped(jsonValue);
			if (type != null && !type.isEmpty()) {
				jsonKey = jsonKey + ":" + type;
			}
			jsonConfigurationEntryValueMap.put(jsonKey, jsonValue);
		}
		Entry<String, Map<String, Object>> jsonConfigurationEntry = Map.entry(entry.getKey(),
			jsonConfigurationEntryValueMap);
		return jsonConfigurationEntry;
	}

	static void comment(Map<String, Object> map, String line) {
		map.put(newCommentKey(), "");
		map.put(newCommentKey(), "// " + line);
	}

	static void comment(Map<String, Object> map, List<String> lines) {
		map.put(newCommentKey(), "");
		map.put(newCommentKey(), "/*");
		for (String line : lines) {
			map.put(newCommentKey(), " * " + line);
		}
		map.put(newCommentKey(), " */");
	}

	private static String newCommentKey() {
		return "____COMMENT____" + UUID.randomUUID();
	}
}
