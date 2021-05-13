package aQute.bnd.exporter.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.osgi.util.feature.Feature;
import org.osgi.util.feature.FeatureBundle;
import org.osgi.util.feature.FeatureConfiguration;
import org.osgi.util.feature.FeatureExtension;
import org.osgi.util.feature.FeatureExtension.Kind;
import org.osgi.util.feature.FeatureExtension.Type;
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
		when(id.getClassifier()).thenReturn("c");
		when(id.getType()).thenReturn("t");

		when(feature.getID()).thenReturn(id);

		when(feature.getName()).thenReturn("name");
		when(feature.getLicense()).thenReturn("license");
		when(feature.getVendor()).thenReturn("vendor");
		when(feature.getDescription()).thenReturn("description");
		when(feature.getCategories()).thenReturn(List.of("tooling", "docker", "bundle"));
		when(feature.getCopyright()).thenReturn("copyright");
		when(feature.getDocURL()).thenReturn("http://www.osgi.org");
		when(feature.getSCM()).thenReturn("scm");

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

		FeatureBundle featureBundle1 = mock(FeatureBundle.class);
		ID featureBundle1Id = mock(ID.class);

		when(featureBundle1Id.getGroupId()).thenReturn("bundle1g");
		when(featureBundle1Id.getArtifactId()).thenReturn("bundle1a");
		when(featureBundle1Id.getVersion()).thenReturn("bundle1v");
		when(featureBundle1Id.getClassifier()).thenReturn("bundle1c");
		when(featureBundle1Id.getType()).thenReturn("bundle1t");

		when(featureBundle1.getID()).thenReturn(featureBundle1Id);
		FeatureBundle featureBundle2 = mock(FeatureBundle.class);
		ID featureBundle2Id = mock(ID.class);

		when(featureBundle2Id.getGroupId()).thenReturn("bundle2g");
		when(featureBundle2Id.getArtifactId()).thenReturn("bundle2a");
		when(featureBundle2Id.getVersion()).thenReturn("bundle2v");
		when(featureBundle2Id.getClassifier()).thenReturn("bundle2c");
		when(featureBundle2Id.getType()).thenReturn("bundle2t");

		when(featureBundle2.getID()).thenReturn(featureBundle2Id);
		when(featureBundle2.getMetadata()).thenReturn(Map.of("k1", "v1", "hashes",
			Map.of("md5", "###", "sha1", "###", "sha256", "###"), "osgi.content", "###sha256###"));

		List<FeatureBundle> featureBundles = List.of(featureBundle1, featureBundle2);
		when(feature.getBundles()).thenReturn(featureBundles);

		FeatureExtension extension1 = mock(FeatureExtension.class);
		when(extension1.getKind()).thenReturn(Kind.MANDATORY);
		when(extension1.getType()).thenReturn(Type.ARTIFACTS);
		when(extension1.getArtifacts())
			.thenReturn(List.of(ID.fromMavenID("g:a:v1"), ID.fromMavenID("g:a:v2"), ID.fromMavenID("g:a:v3")));

		FeatureExtension extension2 = mock(FeatureExtension.class);
		when(extension2.getKind()).thenReturn(Kind.OPTIONAL);
		when(extension2.getType()).thenReturn(Type.TEXT);
		when(extension2.getText()).thenReturn(List.of("line1", "line2", "line3"));

		FeatureExtension extension3 = mock(FeatureExtension.class);
		when(extension3.getKind()).thenReturn(Kind.TRANSIENT);
		when(extension3.getType()).thenReturn(Type.JSON);
		when(extension3.getJSON()).thenReturn("{\"foo\":\"bar\"}");

		Map<String, FeatureExtension> extensionMap = new LinkedHashMap<>();
		extensionMap.put("ext1", extension1);
		extensionMap.put("ext2", extension2);
		extensionMap.put("ext3", extension3);
		when(feature.getExtensions()).thenReturn(extensionMap);

		FeatureExporterConfig c = new FeatureExporterConfig();
		String s = Utils.toJson(feature, c);
		System.out.println(s);

		// // TODO: this must throw an exception
		// Features.getBuilderFactory()
		// .newBundleBuilder(ID.fromMavenID("g:a:v"))
		// .addMetadata("id", "does override the id")
		// .build();

		// TODO: read component xml and metadata and create configuration.
	}

}
