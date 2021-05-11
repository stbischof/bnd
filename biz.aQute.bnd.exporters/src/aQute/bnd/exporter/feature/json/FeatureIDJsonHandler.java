package aQute.bnd.exporter.feature.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import org.osgi.util.feature.ID;

import aQute.lib.json.Encoder;
import aQute.lib.json.Handler;
import aQute.lib.json.StringHandler;

public class FeatureIDJsonHandler extends Handler {

	private static final String DELIMITER = ",";

	@Override
	public void encode(Encoder app, Object object, Map<Object, Type> visited) throws IOException, Exception {
		ID id = (ID) object;

		app.append("{");
		app.indent();

		StringHandler.string(app, "groupID");
		app.append(":");
		app.encode(id.getGroupId(), String.class, visited);

		app.append(DELIMITER);
		app.linebreak();
		StringHandler.string(app, "artifactId");
		app.append(":");
		app.encode(id.getArtifactId(), String.class, visited);

		app.append(DELIMITER);
		app.linebreak();
		StringHandler.string(app, "version");
		app.append(":");
		app.encode(id.getVersion(), String.class, visited);

		if (id.getClassifier() != null) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "classifier");
			app.append(":");
			app.encode(id.getClassifier(), String.class, visited);
		}

		if (id.getType() != null) {
			app.append(DELIMITER);
			app.linebreak();
			StringHandler.string(app, "type");
			app.append(":");
			app.encode(id.getType(), String.class, visited);

		}

		app.undent();
		app.append("}");

	}

}
