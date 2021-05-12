package aQute.bnd.exporter.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.TreeMap;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.osgi.util.feature.Feature;
import org.osgi.util.feature.FeatureConfiguration;
import org.osgi.util.feature.ID;

import aQute.bnd.exporter.feature.json.FeatureExporterConfig;
import aQute.bnd.unmodifiable.Maps;

public class FeatureTest {
	@Test
	public void test1() throws Exception {

		Feature feature = mock(Feature.class);
		ID id = mock(ID.class);

		when(id.getGroupId()).thenReturn("g");
		when(id.getArtifactId()).thenReturn("a");
		when(id.getVersion()).thenReturn("v");
		when(id.getClassifier()).thenReturn(null);
		when(id.getType()).thenReturn(null);
		// when(id.getClassifier()).thenReturn("c");
		// when(id.getType()).thenReturn("t");

		when(feature.getID()).thenReturn(id);

		when(feature.getName()).thenReturn("n");
		when(feature.getLicense()).thenReturn("l");
		when(feature.getVendor()).thenReturn("v");
		when(feature.getDescription()).thenReturn("d");
		when(feature.isComplete()).thenReturn(true);

		when(feature.getVariables()).thenReturn(Maps.of("k1", "v1", "k2", "v2"));

		FeatureConfiguration fc1 = mock(FeatureConfiguration.class);
		Map<String, Object> cfgMap = new TreeMap<String, Object>();
		cfgMap.put("k1", "v1");
		cfgMap.put("k2", null);
		cfgMap.put("k3", 3);
		cfgMap.put("k4", new int[] {
			1, 2
		});
		cfgMap.put("k5", Lists.list("a", "b"));
		cfgMap.put("k6", Float.MAX_VALUE);

		when(fc1.getValues()).thenReturn(cfgMap);

		FeatureConfiguration fc2 = mock(FeatureConfiguration.class);
		when(fc2.getValues()).thenReturn(Maps.of());

		when(feature.getConfigurations()).thenReturn(Maps.of("pid", fc1, "factoryPid~name", fc2));

		FeatureExporterConfig c = new FeatureExporterConfig();
		String s = Utils.toJson(feature, c);
		System.out.println(s);
	}
}
