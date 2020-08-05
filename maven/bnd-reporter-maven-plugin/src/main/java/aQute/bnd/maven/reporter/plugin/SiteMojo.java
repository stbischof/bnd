package aQute.bnd.maven.reporter.plugin;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.osgi.Processor;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.reporter.ReportExporterService;
import aQute.bnd.service.reporter.ReportGeneratorService;
import biz.aQute.bnd.reporter.exporter.ReportExporterBuilder;

/**
 * Exports a set of readme files (template can be parametrized with system
 * properties starting with 'bnd.reporter.*').
 */
@Mojo(name = "site", threadSafe = true)
public class SiteMojo extends AbstractMojo {

	@Component
	private MavenProjectHelper	projectHelper;
	private static final Logger	logger		= LoggerFactory.getLogger(SiteMojo.class);

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject		project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession		session;

	@Parameter(property = "bnd.reporter.skip", defaultValue = "false")
	private boolean				skip;

	@Parameter
	private Map<String, String>	parameters	= new HashMap<>();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			logger.debug("skip project as configured");
			return;
		}

		try (Processor processor = new Processor()) {
			processor.setTrace(logger.isDebugEnabled());
			processor.setBase(project.getBasedir());

			// Set the readme configuration to the processor
			processor.setProperty("-exportreport.__autoreadme",
				"jeckyll.md.aggregat;template=default:readme.twig" + getParameters());

			List<Dependency> deps = project.getDependencies();

			List<MavenProject> projects = session
				.getProjects()
				.stream()
				.filter(mp -> contains(mp, deps))
				.collect(Collectors.toList());


			MavenProjectWrapper toAnalyze = new MavenProjectWrapper(session.getProjects(), project);

			// Create the generator service.
			ReportGeneratorService generator = null;
			String scope = null;
			if (toAnalyze.isAggregator()) {
				generator = ReportGeneratorFactory.create()
					.setProcessor(processor)
					.useCustomConfig()
					.withAggregatorProjectDefaultPlugins()
					.build();
			} else {
				generator = ReportGeneratorFactory.create()
					.setProcessor(processor)
					.useCustomConfig()
					.withProjectDefaultPlugins()
					.build();
			}

			// Create the exporter service.
			ReportExporterService reporter = ReportExporterBuilder.create()
				.setProcessor(processor)
				.setGenerator(generator)
				.build();

			logger.info("Generating reports...");

			Map<String, Resource> reportResults = reporter.exportReportsOf(toAnalyze);

			report(processor);

			if (!processor.isOk()) {
				throw new MojoExecutionException("Errors in bnd processing, see log for details.");
			}

			if (reportResults.isEmpty()) {
				logger.info("No report matching the '{}' scope has been found.", scope);
			}

			for (Entry<String, Resource> result : reportResults.entrySet()) {
				try {
					result.getValue()
						.write(new FileOutputStream(result.getKey()));
					logger.info("The report at {} has been successfully created.", result.getKey());
				} catch (Exception exception) {
					throw new MojoExecutionException("Failed to write the report at " + result.getKey(), exception);
				}
			}
		} catch (MojoExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new MojoExecutionException("bnd error: " + e.getMessage(), e);
		}
	}

	private static boolean contains(MavenProject mp, List<Dependency> deps) {

		return deps.stream()
			.anyMatch(d -> {
				return d.getGroupId()
					.equals(mp.getGroupId())
					&& d.getArtifactId()
						.equals(mp.getArtifactId());
			});
	}

	private void report(Processor processor) {
		for (String warning : processor.getWarnings()) {
			logger.warn("Warning : {}", warning);
		}
		for (String error : processor.getErrors()) {
			logger.error("Error   : {}", error);
		}
	}

	private String getParameters() {
		Map<String, String> params = new HashMap<>(parameters);

		System.getProperties()
			.stringPropertyNames()
			.stream()
			.filter(k -> k.startsWith("bnd.reporter."))
			.forEach(k -> {
				if (System.getProperty(k) != null) {
					params.put(k, System.getProperty(k));
				}
			});

		StringBuilder param = new StringBuilder();

		if (!params.isEmpty()) {
			param.append(";parameters='");
		}

		params.entrySet()
			.forEach(e -> {
				param.append(e.getKey() + "=" + e.getValue() + ",");
			});

		if (!params.isEmpty()) {
			param.deleteCharAt(param.length() - 1);
			param.append("'");
		}
		return param.toString();
	}
}
