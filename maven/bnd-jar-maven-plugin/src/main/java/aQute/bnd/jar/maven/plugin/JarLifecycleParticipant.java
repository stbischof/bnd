package aQute.bnd.jar.maven.plugin;

import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.google.common.base.Strings;

/**
 * Lifecycle participant that is meant to kick in Maven3 to lessen the needed
 * POM changes. It will silently "scan" the projects, disable all executions of
 * maven-jar-plugin, and "install" itself instead.
 *
 * @see: sonatype/nexus-maven-plugins
 *       org.sonatype.nexus.maven.staging.deploy.DeployLifecycleParticipant
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "Qute.bnd.jar.maven.plugin.JarLifecycleParticipant")
public class JarLifecycleParticipant extends AbstractMavenLifecycleParticipant implements LogEnabled {
	/**
	 * We have to have some access to GAV of the plugin, but lifecycle
	 * participant is loaded up very early, so we cannot use MojoExecutions
	 * (actually, we do not even have one in scope) and other Maven Core stuff
	 * to get them. Any way to discover these?
	 */
	public static String	_THIS_GROUP_ID					= "biz.aQute.bnd";

	public static String	_THIS_ARTIFACT_ID				= "bnd-jar-maven-plugin";

	public static String	MAVEN_JAR_PLUGIN_GROUP_ID		= "org.apache.maven.plugins";

	public static String	MAVEN_JAR_PLUGIN_ARTIFACT_ID	= "maven-jar-plugin";

	private Logger			logger;

	protected String getPluginGroupId() {
		return _THIS_GROUP_ID;
	}

	protected String getPluginArtifactId() {
		return _THIS_ARTIFACT_ID;
	}

	@Override
	public void enableLogging(final Logger logger) {
		this.logger = logger;
	}

	@Override
	public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
		try {
			final int totalModules = session.getProjects()
				.size();
			logger.info("Inspecting build with total of " + totalModules + " modules...");

			int stagingGoalsFoundInModules = 0;
			// check do we need to do anything at all?
			// should not find any nexus-staging-maven-plugin deploy goal
			// executions in any project
			// otherwise, assume it's "manually done"
			for (MavenProject project : session.getProjects()) {
				final Plugin nexusMavenPlugin = getBuildPluginsBndMavenPlugin(project.getModel());
				if (nexusMavenPlugin != null) {
					if (!nexusMavenPlugin.getExecutions()
						.isEmpty()) {
						for (PluginExecution pluginExecution : nexusMavenPlugin.getExecutions()) {
							final List<String> goals = pluginExecution.getGoals();
							if (goals.contains("jar") || goals.contains("jar-tests")) {
								stagingGoalsFoundInModules++;
								break;
							}
						}
					}
				}
			}

			if (stagingGoalsFoundInModules > 0) {
				logger.info("Not installing bnd jar features:");
				if (stagingGoalsFoundInModules > 0) {
					logger.info(" * Preexisting staging related goal bindings found in " + stagingGoalsFoundInModules
						+ " modules.");
				}
				return;
			}

			logger.info("Installing bnd jar features:");

			// make maven-jar-plugin to be skipped and install us instead
			int skipped = 0;
			for (MavenProject project : session.getProjects()) {
				final Plugin bndMavenPlugin = getBuildPluginsBndMavenPlugin(project.getModel());
				if (bndMavenPlugin != null) {
					// skip the maven-jar-plugin
					final Plugin mavenDeployPlugin = getBuildPluginsMavenJarPlugin(project.getModel());
					if (mavenDeployPlugin != null) {

						mavenDeployPlugin.getExecutions()
							.clear();

						// add executions to bnd-jar-maven-plugin
						final PluginExecution execution = new PluginExecution();
						execution.setId("injected-bnd-jar");
						execution.getGoals()
							.add("jar");
						execution.setPhase("jar");
						execution.setConfiguration(bndMavenPlugin.getConfiguration());
						bndMavenPlugin.getExecutions()
							.add(execution);

						// count this in
						skipped++;
					}
				}
			}
			if (skipped > 0) {
				logger.info("  ... total of " + skipped + " executions of maven-jar-plugin replaced with "
					+ getPluginArtifactId());
			}
		} catch (IllegalStateException e) {
			// thrown by getPluginByGAFromContainer
			throw new MavenExecutionException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the bnd-jar-maven-plugin from build/plugins section of model or
	 * {@code null} if not present.
	 */
	protected Plugin getBuildPluginsBndMavenPlugin(final Model model) {
		if (model.getBuild() != null) {
			return getNexusMavenPluginFromContainer(model.getBuild());
		}
		return null;
	}

	/**
	 * Returns the maven-jar-plugin from build/plugins section of model or
	 * {@code null} if not present.
	 */
	protected Plugin getBuildPluginsMavenJarPlugin(final Model model) {
		if (model.getBuild() != null) {
			return getMavenDeployPluginFromContainer(model.getBuild());
		}
		return null;
	}

	/**
	 * Returns the bnd-jar-maven-plugin from pluginContainer or {@code null} if
	 * not present.
	 */
	protected Plugin getNexusMavenPluginFromContainer(final PluginContainer pluginContainer) {
		return getPluginByGAFromContainer(getPluginGroupId(), getPluginArtifactId(), pluginContainer);
	}

	/**
	 * Returns the maven-jar-plugin from pluginContainer or {@code null} if not
	 * present.
	 */
	protected Plugin getMavenDeployPluginFromContainer(final PluginContainer pluginContainer) {
		return getPluginByGAFromContainer(MAVEN_JAR_PLUGIN_GROUP_ID, MAVEN_JAR_PLUGIN_ARTIFACT_ID, pluginContainer);
	}

	// ==

	protected void setPluginScalarConfigurationValueEverywhere(final String key, final String value,
		final boolean override, final Plugin plugin) {
		setPluginScalarConfigurationValueInAllExecutions(key, value, override, plugin);
		Xpp3Dom pluginConfiguration = setPluginScalarConfigurationValue(key, value, override,
			(Xpp3Dom) plugin.getConfiguration());
		plugin.setConfiguration(pluginConfiguration);
	}

	protected void setPluginScalarConfigurationValueInAllExecutions(final String key, final String value,
		final boolean override, final Plugin plugin) {
		for (PluginExecution execution : plugin.getExecutions()) {
			Xpp3Dom executionConfiguration = setPluginScalarConfigurationValue(key, value, override,
				(Xpp3Dom) execution.getConfiguration());
			execution.setConfiguration(executionConfiguration);
		}
	}

	protected Xpp3Dom setPluginScalarConfigurationValue(final String key, final String value, final boolean override,
		final Xpp3Dom originalConfiguration) {
		Xpp3Dom configuration = originalConfiguration;
		if (configuration == null) {
			configuration = new Xpp3Dom("configuration");
		}
		Xpp3Dom changed = configuration.getChild(key);
		if (changed != null && !override) {
			return originalConfiguration; // is present, we do not override it
		}
		if (changed == null) {
			changed = new Xpp3Dom(key);
			configuration.addChild(changed);
		}
		changed.setValue(value);
		return configuration;
	}

	protected Plugin getPluginByGAFromContainer(final String groupId, final String artifactId,
		final PluginContainer pluginContainer) {
		Plugin result = null;
		for (Plugin plugin : pluginContainer.getPlugins()) {
			if (Strings.nullToEmpty(groupId)
				.equals(Strings.nullToEmpty(plugin.getGroupId()))
				&& Strings.nullToEmpty(artifactId)
					.equals(Strings.nullToEmpty(plugin.getArtifactId()))) {
				if (result != null) {
					throw new IllegalStateException(
						"The build contains multiple versions of plugin " + groupId + ":" + artifactId);
				}
				result = plugin;
			}

		}
		return result;
	}
}
