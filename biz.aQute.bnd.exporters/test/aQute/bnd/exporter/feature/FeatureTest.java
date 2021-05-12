package aQute.bnd.exporter.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.osgi.util.feature.Feature;
import org.osgi.util.feature.FeatureConfiguration;
import org.osgi.util.feature.ID;

import aQute.bnd.unmodifiable.Maps;

public class FeatureTest {
	@Test
	public void test1() throws Exception {

		Feature feature = mock(Feature.class);
		ID id = mock(ID.class);

		when(id.getGroupId()).thenReturn("g");
		when(id.getArtifactId()).thenReturn("a");
		when(id.getVersion()).thenReturn("v");
		when(id.getClassifier()).thenReturn("c");
		when(id.getType()).thenReturn("t");

		when(feature.getID()).thenReturn(id);

		when(feature.getName()).thenReturn("n");
		when(feature.getLicense()).thenReturn("l");
		when(feature.getVendor()).thenReturn("v");
		when(feature.getDescription()).thenReturn("d");
		when(feature.isComplete()).thenReturn(true);

		when(feature.getVariables()).thenReturn(Maps.of("k1", "v1", "k2", "v2"));

		FeatureConfiguration fc1 = mock(FeatureConfiguration.class);
		when(fc1.getValues()).thenReturn(Maps.of("k1", "v1", "k2", null, ".k3", 3, "k4", new int[] {
			1, 2
		}, "k5", Lists.list("a", "b"), "k6", Float.MAX_VALUE));

		FeatureConfiguration fc2 = mock(FeatureConfiguration.class);
		when(fc2.getValues()).thenReturn(Maps.of());

		when(feature.getConfigurations()).thenReturn(Maps.of("pid", fc1, "factoryPid~name", fc2));

		String s = Utils.toJson(feature);
		System.out.println(s);
	}
}
