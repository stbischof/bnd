package aQute.bnd.exporter.feature;

import java.io.InputStream;
import java.util.Properties;

import org.osgi.util.feature.FeatureBundle;
import org.osgi.util.feature.Features;
import org.osgi.util.feature.ID;

import aQute.bnd.build.Container;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;

public class Utils {

    private static final String META_INF_MAVEN = "META-INF/maven/";

    private static final String MAVEN_PROPS = "pom.properties";

    private static final String GROUP_ID = "groupId";

    private static final String ARTIFACT_ID = "artifactId";

    private static final String VERSION = "version";

    private static final String CLASSIFIER = "classifier";

	private static final String	TYPE			= "type";

	public static FeatureBundle toArtifact(Container container) {
//        jar.getResources().values().forEach(System.out::println);

//        System.out.println(container);
//        System.out.println(container.getFile());
        try (Jar jar = new Jar(container.getFile())) {
//            jar.getResources().keySet().forEach(System.out::println);
            Resource pomProps = jar
                    .getResources(s -> s.matches(META_INF_MAVEN + ".*" + MAVEN_PROPS))
                    .findFirst()
                    .get();
            InputStream is = pomProps.openInputStream();
            Properties pomProperties = new Properties();
            pomProperties.load(is);
            String groupId = pomProperties.getProperty(GROUP_ID);
            String artifactId = pomProperties.getProperty(ARTIFACT_ID);
            String version = pomProperties.getProperty(VERSION);
            String classifier = pomProperties.getProperty(CLASSIFIER);
			String type = pomProperties.getProperty(TYPE);
			ID id = new ID(groupId, artifactId, version, type, classifier);
			FeatureBundle featureBundle = Features.getBuilderFactory()
				.newBundleBuilder(id)
				.build();
			return featureBundle;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
