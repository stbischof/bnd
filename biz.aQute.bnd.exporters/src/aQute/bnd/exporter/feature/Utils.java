package aQute.bnd.exporter.feature;

import java.util.Locale;

import org.osgi.util.feature.Feature;
import org.osgi.util.feature.FeatureBundle;
import org.osgi.util.feature.FeatureBundleBuilder;
import org.osgi.util.feature.Features;
import org.osgi.util.feature.ID;

import aQute.bnd.build.Container;
import aQute.bnd.exporter.feature.json.FeatureIDJsonHandler;
import aQute.bnd.exporter.feature.json.FeatureJsonHandler;
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

	public static FeatureBundle toFeatureBundle(Container container) {

		try (Jar jar = new Jar(container.getFile()); Processor p = new Processor()) {

			ID id = idFromMavenCoord(jar, p);

			FeatureBundleBuilder featureBundleBuilder = Features.getBuilderFactory()
				.newBundleBuilder(id);

			addMetadataChecksum(jar, p, featureBundleBuilder);
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
		featureBundleBuilder.addMetadata(FEATURE_BUNDLE_CHECKSUM + "md5", checksumDTO.md5);
		featureBundleBuilder.addMetadata(FEATURE_BUNDLE_CHECKSUM + "sha1", checksumDTO.sha1);
		featureBundleBuilder.addMetadata(FEATURE_BUNDLE_CHECKSUM + "sha256", checksumDTO.sha256);
		featureBundleBuilder.addMetadata(FEATURE_BUNDLE_CHECKSUM + "sha512", checksumDTO.sha512);
	}

	// writeConfigurationsAsComment(writer);
	// writeFeatureExtensionsFromFeatureBundlesAsComment(writer);
	// writeGogoCommandsAuthAsComment(writer);

	public static String toJson(Feature f) throws Exception {

		JSONCodec jsonCodec = new JSONCodec();

		jsonCodec.addHandler(Feature.class, new FeatureJsonHandler());

		jsonCodec.addHandler(ID.class, new FeatureIDJsonHandler());

		String s = jsonCodec.enc()
			.indent("  ")
			.put(f)
			.toString();
		return s;

	}
}
