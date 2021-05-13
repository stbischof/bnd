package aQute.bnd.exporter.feature;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.osgi.util.feature.Feature;
import org.osgi.util.feature.FeatureBundle;
import org.osgi.util.feature.FeatureBundleBuilder;
import org.osgi.util.feature.Features;
import org.osgi.util.feature.ID;

import aQute.bnd.build.Container;
import aQute.bnd.exporter.feature.json.FeatureExporterConfig;
import aQute.bnd.exporter.feature.json.JsonUtil;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Processor;
import aQute.lib.json.JSONCodec;
import biz.aQute.bnd.reporter.maven.dto.ChecksumDTO;
import biz.aQute.bnd.reporter.maven.dto.MavenCoordinatesDTO;
import biz.aQute.bnd.reporter.plugins.entries.bundle.ChecksumPlugin;
import biz.aQute.bnd.reporter.plugins.entries.bundle.MavenCoordinatePlugin;

public class Utils {

	public static String	FEATURE					= "bnd.feature.";
	public static String	FEATURE_BUNDLE			= FEATURE + "bundle";
	public static String	FEATURE_BUNDLE_CHECKSUM	= FEATURE_BUNDLE + "checksum.";

	public static FeatureBundle toFeatureBundle(Container container, FeatureExporterConfig exporterConfig) {

		try (Jar jar = new Jar(container.getFile()); Processor p = new Processor()) {

			ID id = idFromMavenCoord(jar, p);

			FeatureBundleBuilder featureBundleBuilder = Features.getBuilderFactory()
				.newBundleBuilder(id);

			if (exporterConfig.bundleHashes) {
				addMetadataChecksum(jar, p, featureBundleBuilder);
			}
			// maybe license

			return featureBundleBuilder.build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static ID idFromMavenCoord(Jar jar, Processor p) {
		MavenCoordinatePlugin mavenCoordPlugin = new MavenCoordinatePlugin();
		mavenCoordPlugin.setReporter(p);

		final MavenCoordinatesDTO mvnCoordDTO = mavenCoordPlugin.extract(jar, Locale.forLanguageTag("und"));

		ID id = new ID(mvnCoordDTO.groupId, mvnCoordDTO.artifactId, mvnCoordDTO.version, mvnCoordDTO.type,
			mvnCoordDTO.classifier);
		return id;
	}

	private static void addMetadataChecksum(Jar jar, Processor p, FeatureBundleBuilder featureBundleBuilder) {
		ChecksumPlugin plugin = new ChecksumPlugin();
		plugin.setReporter(p);

		final ChecksumDTO checksumDTO = plugin.extract(jar, Locale.forLanguageTag("und"));
		featureBundleBuilder.addMetadata(FEATURE_BUNDLE_CHECKSUM, checksumDTO);

	}

	// writeConfigurationsAsComment(writer);
	// writeFeatureExtensionsFromFeatureBundlesAsComment(writer);
	// writeGogoCommandsAuthAsComment(writer);

	public static String toJson(Feature feature, FeatureExporterConfig c) throws Exception {

		JSONCodec jsonCodec = new JSONCodec();

		Map<String, Object> featureMap = JsonUtil.createJsonFreatureMap(feature);

		String s = jsonCodec.enc()
			.indent("  ")
			.put(featureMap)
			.toString();

		s = s.lines()
			.map(line -> line.replaceAll("(.*)\\\"____COMMENT____.*\\\":\\\"(.*)\\\",", "$1$2"))
			.collect(Collectors.joining(System.lineSeparator()));

		return s;

	}
}
