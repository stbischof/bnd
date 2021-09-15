package aQute.bnd.jar.maven.plugin;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "jar", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class JarMojo extends AbstractJarMojo {
	/**
	 * Directory containing the classes and resource files that should be
	 * packaged into the JAR.
	 */
	@Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
	private File	classesDirectory;

	/**
	 * Classifier to add to the artifact generated. If given, the artifact will
	 * be attached as a supplemental artifact. If not given this will create the
	 * main artifact which is the default behavior. If you try to do that a
	 * second time without using a classifier the build will fail.
	 */
	@Parameter
	private String	classifier;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getClassifier() {
		return classifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getType() {
		return "jar";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected File getClassesDirectory() {
		return classesDirectory;
	}
}
