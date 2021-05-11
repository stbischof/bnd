package aQute.bnd.exporter.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.osgi.util.feature.Feature;
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

		String s = Utils.toJson(feature);
		System.out.println(s);
	}
}
