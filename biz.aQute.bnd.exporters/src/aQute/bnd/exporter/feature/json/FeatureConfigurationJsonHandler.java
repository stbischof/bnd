package aQute.bnd.exporter.feature.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.util.feature.FeatureConfiguration;

import aQute.lib.json.Encoder;
import aQute.lib.json.Handler;
import aQute.lib.json.StringHandler;

public class FeatureConfigurationJsonHandler extends Handler {

	private static final String DELIMITER = ",";

	@Override
	public void encode(Encoder app, Object object, Map<Object, Type> visited) throws IOException, Exception {
		FeatureConfiguration configuration = (FeatureConfiguration) object;

		app.append("{");
		app.indent();

		String del = "";

		if (configuration.getValues() != null && !configuration.getValues()
			.isEmpty()) {

			for (Entry<String, Object> entry : configuration.getValues()
				.entrySet()) {
				app.append(del);
				if (!del.isEmpty()) {
					app.linebreak();
				}
				del = DELIMITER;

				Object value = entry.getValue();
				String key = entry.getKey();
				String type = TypeConverter.convertObjectToTyped(value);
				if (type != null && !type.isEmpty()) {
					key = key + ":" + type;
				}
				if (key.startsWith(".")) {
					// TODO: comment util // , /* with width
					app.append(
						"// Component properties whose names start with full stop are available to the component instance but are not available as service properties of the registered service.");
					app.linebreak();
				}
				StringHandler.string(app, key);
				app.append(":");

				app.encode(value, value == null ? Object.class : value.getClass(), visited);

			}
		}

		app.undent();
		app.append("}");

	}

}
