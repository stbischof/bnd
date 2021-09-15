package aQute.bnd.jar.maven.plugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "test-jar", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public class TestJarMojo extends AbstractJarMojo {

	/**
	 * Set this to <code>true</code> to bypass test-jar generation. Its use is
	 * <b>NOT RECOMMENDED</b>, but quite convenient on occasion.
	 */
	@Parameter(property = "maven.test.skip")
	private boolean	skip;

	/**
	 * Directory containing the test classes and resource files that should be
	 * packaged into the JAR.
	 */
	@Parameter(defaultValue = "${project.build.testOutputDirectory}", required = true)
	private File	testClassesDirectory;

	/**
	 * Classifier to use for {@code test-jar}.
	 */
	@Parameter(defaultValue = "tests")
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
		return "test-jar";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected File getClassesDirectory() {
		return testClassesDirectory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("Skipping packaging of the test-jar");
		} else {
			super.execute();
		}
	}
}
