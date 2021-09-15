package aQute.bnd.jar.maven.plugin;

import java.util.ArrayList;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class JarLifecycleParticipantTest
{
  private StringBufferLogger fakeLogger;

	private JarLifecycleParticipant	defaultLifecycleParticipant;

  @Before
  public void before() {
    fakeLogger = new StringBufferLogger("fake");
	this.defaultLifecycleParticipant = new JarLifecycleParticipant();
    this.defaultLifecycleParticipant.enableLogging(fakeLogger);
  }

  // ===

  protected Plugin createPlugin(String groupId, String artifactId) {
    final Plugin plugin = new Plugin();
    plugin.setGroupId(groupId);
    plugin.setArtifactId(artifactId);
    Xpp3Dom configuration = new Xpp3Dom("configuration");
    plugin.setConfiguration(configuration);
    return plugin;
  }

  protected Plugin createPlugin(String groupId, String artifactId, String goal) {
    final Plugin plugin = createPlugin(groupId, artifactId);
    if (goal != null && goal.trim().length() > 0) {
      final PluginExecution execution = new PluginExecution();
      execution.setId("default-" + goal);
		execution.setPhase("jar");
      execution.getGoals().add(goal);
      plugin.getExecutions().add(execution);
    }
    return plugin;
  }

  protected Plugin createPluginAndSetConfig(String groupId, String artifactId, String goal, String configValue) {
    final Plugin plugin = createPlugin(groupId, artifactId, goal);
    plugin.setGroupId(groupId);
    plugin.setArtifactId(artifactId);
    Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
    Xpp3Dom foo = new Xpp3Dom("aSwitch");
    foo.setValue(configValue);
    configuration.addChild(foo);
    return plugin;
  }

  protected MavenSession createSessionWithProjectsForModels(Model... models) {
    final MavenSession mockSession = Mockito.mock(MavenSession.class);
    final ArrayList<MavenProject> projects = new ArrayList<MavenProject>();
    for (Model model : models) {
      final MavenProject mockProject = Mockito.mock(MavenProject.class);
      Mockito.when(mockProject.getModel()).thenReturn(model);
      Mockito.when(mockProject.getGroupId()).thenReturn(model.getGroupId());
      Mockito.when(mockProject.getArtifactId()).thenReturn(model.getArtifactId());
      Mockito.when(mockProject.getVersion()).thenReturn(model.getVersion());
      projects.add(mockProject);
    }
    Mockito.when(mockSession.getProjects()).thenReturn(projects);
    return mockSession;
  }

  protected Model createModel(String groupId, String artifactId) {
    final Model model = new Model();
    model.setGroupId(groupId);
    model.setArtifactId(artifactId);
    model.setVersion("1.0");
    model.setBuild(new Build());
    return model;
  }

  // ==

  @Test
  public void testSimpleOneModule()
      throws MavenExecutionException
  {
    final Model model = createModel("org.foo", "simple");
	// jar defined as "usual"
    model.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant.MAVEN_JAR_PLUGIN_GROUP_ID,
				JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID, "jar"));
    // out plugin just declared as extension, nothing more. no goals
    model.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant._THIS_GROUP_ID, JarLifecycleParticipant._THIS_ARTIFACT_ID));

    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

	// aftermath: jar plugin should executions removed, bnd-jar-maven-plugin
	// should have executions added
    for (Plugin plugin : mockSession.getProjects().get(0).getModel().getBuild().getPlugins()) {
		if (JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
        Assert.assertTrue("No executions for plugin "
				+ JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID,
				plugin.getExecutions()
					.isEmpty());
      }
		else if (JarLifecycleParticipant._THIS_ARTIFACT_ID.equals(plugin.getArtifactId())) {
			Assert.assertEquals("One execution for plugin " + JarLifecycleParticipant._THIS_ARTIFACT_ID, 1,
            plugin.getExecutions().size());
      }
      else {
        // wtf? we did not add any other plugin
        Assert.fail("Unknown plugin (): " + plugin.getGroupId() + ":" + plugin.getArtifactId());
      }
    }
    // logging should happen
	Assert.assertTrue("Missing log?", fakeLogger.getLoggedStuff()
		.contains("Installing bnd jar"));
    Assert.assertTrue("Wrong count reported?", fakeLogger.getLoggedStuff().contains("... total of 1"));
  }

  @Test
  public void testSimpleOneModuleWithManuallySetPlugin()
      throws MavenExecutionException
  {
    final Model model = createModel("org.foo", "simple-but-manually-set");
	// jar defined as "usual"
    model.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant.MAVEN_JAR_PLUGIN_GROUP_ID,
				JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID, "jar"));
    // out plugin declared as extension with manually set execution
    model.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant._THIS_GROUP_ID, JarLifecycleParticipant._THIS_ARTIFACT_ID,
            "jar"));

    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

    // aftermath: lifecycle participant should stay put, do not intervene at all!
    for (Plugin plugin : mockSession.getProjects().get(0).getModel().getBuild().getPlugins()) {
		if (JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
        Assert.assertEquals("One execution for plugin "
				+ JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID, 1,
				plugin.getExecutions()
					.size());
      }
		else if (JarLifecycleParticipant._THIS_ARTIFACT_ID.equals(plugin.getArtifactId())) {
			Assert.assertEquals("One execution for plugin " + JarLifecycleParticipant._THIS_ARTIFACT_ID, 1,
            plugin.getExecutions().size());
      }
      else {
        // wtf? we did not add any other plugin
        Assert.fail("Unknown plugin (): " + plugin.getGroupId() + ":" + plugin.getArtifactId());
      }
    }
    // logging should happen
	Assert.assertTrue("Missing log?", fakeLogger.getLoggedStuff()
		.contains("Not installing bnd jar"));
  }

  @Test
  public void testComplexReactorWithAggregators()
      throws MavenExecutionException
  {
    // layout
    // aggregator-pom:
	// parent-A : has one config for bnd-jar-maven-plugin (declared as extension
	// + config)
    // module-A1
	// parent-B : had other config for bnd-jar-maven-plugin (declared as
	// extension + config)
    // module-B1

    // remember: we "simulate" running in maven, where models are already interpolated and executions added!
    // hence, we do not fiddle with "modules" but just creating a list, as "maven would" load and sort em

    // aggregator-pom, essentially empty
    final Model aggregatorPom = createModel("org.foo", "aggregator");

    // parent-A:
    final Model parentA = createModel("org.foo", "parent-a");
	// jar defined as "usual"
    parentA.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant.MAVEN_JAR_PLUGIN_GROUP_ID,
				JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID, "jar"));
    // our plugin declared as extension, no execution and A specific param
    parentA.getBuild().getPlugins().add(
			createPluginAndSetConfig(JarLifecycleParticipant._THIS_GROUP_ID, JarLifecycleParticipant._THIS_ARTIFACT_ID,
				null, "profile-a"));

    // module-A:
    final Model moduleA = createModel("org.foo", "module-a");
	// jar defined as "usual"
    moduleA.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant.MAVEN_JAR_PLUGIN_GROUP_ID,
				JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID, "jar"));
    // our plugin declared as extension, no execution and A specific param
    moduleA.getBuild().getPlugins().add(
			createPluginAndSetConfig(JarLifecycleParticipant._THIS_GROUP_ID, JarLifecycleParticipant._THIS_ARTIFACT_ID,
				null, "profile-a"));

    // parent-B:
    final Model parentB = createModel("org.foo", "parent-b");
	// jar defined as "usual"
    parentB.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant.MAVEN_JAR_PLUGIN_GROUP_ID,
				JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID, "jar"));
    // our plugin declared as extension, no execution and A specific param
    parentB.getBuild().getPlugins().add(
			createPluginAndSetConfig(JarLifecycleParticipant._THIS_GROUP_ID, JarLifecycleParticipant._THIS_ARTIFACT_ID,
				null, "profile-b"));

    // module-B:
    final Model moduleB = createModel("org.foo", "module-b");
	// jar defined as "usual"
    moduleB.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant.MAVEN_JAR_PLUGIN_GROUP_ID,
				JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID, "jar"));
    // our plugin declared as extension, no execution and A specific param
    moduleB.getBuild().getPlugins().add(
			createPluginAndSetConfig(JarLifecycleParticipant._THIS_GROUP_ID, JarLifecycleParticipant._THIS_ARTIFACT_ID,
				null, "profile-b"));

    final MavenSession mockSession =
        createSessionWithProjectsForModels(aggregatorPom, parentA, moduleA, parentB, moduleB);

    defaultLifecycleParticipant.afterProjectsRead(mockSession);

    // aftermath: everyting should happen as "usual", but check the proper configurations!
    for (Plugin plugin : mockSession.getProjects().get(0).getModel().getBuild().getPlugins()) {
		if (JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId())) {
        Assert.assertTrue("No executions for plugin "
				+ JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID,
				plugin.getExecutions()
					.isEmpty());
      }
		else if (JarLifecycleParticipant._THIS_ARTIFACT_ID.equals(plugin.getArtifactId())) {
			Assert.assertEquals("One execution for plugin " + JarLifecycleParticipant._THIS_ARTIFACT_ID, 1,
            plugin.getExecutions().size());
        if (plugin.getArtifactId().endsWith("-a")) {
          Assert.assertTrue("Config mismatch!",
              ((Xpp3Dom) plugin.getConfiguration()).getChild("aSwitch").getValue().endsWith("-a"));
        }
        else if (plugin.getArtifactId().endsWith("-b")) {
          Assert.assertTrue("Config mismatch!",
              ((Xpp3Dom) plugin.getConfiguration()).getChild("aSwitch").getValue().endsWith("-b"));
        }
        else {
          // wtf? we did not add any other plugin
          Assert.fail("Unknown plugin config: " + plugin.getGroupId() + ":" + plugin.getArtifactId());
        }
      }
      else {
        // wtf? we did not add any other plugin
        Assert.fail("Unknown plugin: " + plugin.getGroupId() + ":" + plugin.getArtifactId());
      }
    }
    // logging should happen
	Assert.assertTrue("Missing log?", fakeLogger.getLoggedStuff()
		.contains("Installing bnd jar"));
    Assert.assertTrue("Wrong count reported?", fakeLogger.getLoggedStuff().contains("... total of 4"));
  }

  @Test
  public void testABadProject()
      throws MavenExecutionException
  {
    final Model model = createModel("org.foo", "bad");
    model.getBuild().getPlugins().add(
			createPlugin(JarLifecycleParticipant.MAVEN_JAR_PLUGIN_GROUP_ID,
				JarLifecycleParticipant.MAVEN_JAR_PLUGIN_ARTIFACT_ID, "jar"));
    // our plugin declared MULTIPLE TIMES
    model.getBuild().getPlugins().add(
			createPluginAndSetConfig(JarLifecycleParticipant._THIS_GROUP_ID, JarLifecycleParticipant._THIS_ARTIFACT_ID,
				null, "profile-a"));
    model.getBuild().getPlugins().add(
			createPluginAndSetConfig(JarLifecycleParticipant._THIS_GROUP_ID, JarLifecycleParticipant._THIS_ARTIFACT_ID,
				null, "profile-a"));

    final MavenSession mockSession = createSessionWithProjectsForModels(model);

    try {
      defaultLifecycleParticipant.afterProjectsRead(mockSession);
      Assert.fail("Should choke on this!");
    }
    catch (MavenExecutionException e) {
      // good
      Assert.assertEquals("The build contains multiple versions of plugin "
			+ JarLifecycleParticipant._THIS_GROUP_ID + ":" + JarLifecycleParticipant._THIS_ARTIFACT_ID,
          e.getMessage());
    }
  }
}