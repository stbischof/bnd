package aQute.bnd.exporter.feature.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.osgi.util.feature.Feature;
import org.osgi.util.feature.ID;

import aQute.lib.json.Encoder;
import aQute.lib.json.Handler;
import aQute.lib.json.StringHandler;

public class FeatureJsonHandler extends Handler {

	private static final String DELIMITER = ",";

	@Override
	public void encode(Encoder app, Object object, Map<Object, Type> visited) throws IOException, Exception {
		Feature f = (Feature) object;

		app.append("{");
		app.indent();

		StringHandler.string(app, "id");
		app.append(":");
		app.encode(f.getID(), ID.class, visited);

		if (f.getName() != null) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "name");
			app.append(":");
			app.encode(f.getName(), String.class, visited);
		}

		if (f.getDescription() != null) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "description");
			app.append(":");
			app.encode(f.getDescription(), String.class, visited);
		}

		if (f.getVendor() != null) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "vendor");
			app.append(":");
			app.encode(f.getVendor(), String.class, visited);
		}

		if (f.getLicense() != null) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "license");
			app.append(":");
			app.encode(f.getLicense(), String.class, visited);
		}

		if (f.isComplete()) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "complete");
			app.append(":");
			app.encode(f.isComplete(), Boolean.class, visited);
		}

		if (f.getVariables() != null && !f.getVariables()
			.isEmpty()) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "variables");
			app.append(":");
			app.encode(f.getVariables(), Map.class, visited);
		}

		if (f.getBundles() != null && !f.getBundles()
			.isEmpty()) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "bundles");
			app.append(":");
			app.encode(f.getBundles(), List.class, visited);
		}

		if (f.getConfigurations() != null && !f.getConfigurations()
			.isEmpty()) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "configurations");
			app.append(":");
			app.encode(f.getConfigurations(), Map.class, visited);
		}

		if (f.getExtensions() != null && !f.getExtensions()
			.isEmpty()) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "extensions");
			app.append(":");
			app.encode(f.getExtensions(), Map.class, visited);
		}

		app.undent();
		app.append("}");

	}

}
