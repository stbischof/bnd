package aQute.bnd.exporter.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.osgi.util.feature.Feature;
import org.osgi.util.feature.ID;

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
		String s = Utils.toJson(feature);
		System.out.println(s);
	}
}
