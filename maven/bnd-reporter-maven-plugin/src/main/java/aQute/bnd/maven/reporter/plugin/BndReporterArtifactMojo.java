package aQute.bnd.maven.reporter.plugin;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.execution.MavenSession;
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

import aQute.bnd.maven.reporter.plugin.entries.mavenproject.CodeSnippetPlugin;
import aQute.bnd.maven.reporter.plugin.entries.mavenproject.CommonInfoPlugin;
import aQute.bnd.maven.reporter.plugin.entries.mavenproject.FileNamePlugin;
import aQute.bnd.osgi.Processor;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.reporter.ReportExporterService;
import aQute.bnd.service.reporter.ReportGeneratorService;
import biz.aQute.bnd.reporter.exporter.ReportExporterBuilder;

/**
 * Exports a set of user defined reports.
 */
@Mojo(name = "attach-artifact-bnd-src-report", threadSafe = true)
public class BndReporterArtifactMojo extends AbstractMojo {

	@Component
	private MavenProjectHelper			projectHelper;
	private static final String			AGGREGATOR_SCOPE	= "aggregator";
	private static final String			PROJECT_SCOPE		= "project";

	private static final Logger			logger				= LoggerFactory.getLogger(BndReporterArtifactMojo.class);

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject				project;

	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession				session;

	@Parameter(property = "bnd.reporter.skip", defaultValue = "false")
	private boolean						skip;

	@Parameter
	private ReportConfig		reportConfig		= null;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			logger.debug("skip project as configured");
			return;
		}

		try (Processor processor = new Processor()) {
			processor.setTrace(logger.isDebugEnabled());
			processor.setBase(project.getBasedir());

			MavenProjectWrapper toAnalyze = new MavenProjectWrapper(session.getProjects(), project);

			// Add the report configurations to the processor
			if (reportConfig != null) {
			String key = "-reportconfig." + "default";
			String instruction = reportConfig
					.toInstruction();

				processor.setProperty(key, instruction);

				// Add the report configuration to the analyzed object in case
				// of aggregator.
				toAnalyze.getReportConfig()
					.setProperty(key, instruction);
			}
			// Create the generator service.
			ReportGeneratorService generator = null;

					generator = ReportGeneratorFactory.create()
						.setProcessor(processor)
						.useCustomConfig()
						.addPlugin(CodeSnippetPlugin.class
							.getName())
						.addPlugin(FileNamePlugin.class.getName())
						.addPlugin(CommonInfoPlugin.class
							.getName())
						.build();


			Report report = new Report();
			// TODO: OR JSON?
			report.setOutputFile("target/bnd-src-report.xml");
			processor.setProperty("-exportreport." + "bnd-repo", report.toInstruction());

			// Create the exporter service.
			ReportExporterService reporter = ReportExporterBuilder.create()
				.setProcessor(processor)
				.setGenerator(generator)
				.setScope(
					AGGREGATOR_SCOPE)// TODO: notwendig?
				.build();

			logger.info("Generating reports...");

			Map<String, Resource> reportResults = reporter.exportReportsOf(toAnalyze);

			report(processor);

			if (!processor.isOk()) {
				throw new MojoExecutionException("Errors in bnd processing, see log for details.");
			}

			if (reportResults.isEmpty()) {
				logger.info("No report matching the '{}' scope has been found.");
			}

			for (Entry<String, Resource> result : reportResults.entrySet()) {
				try {
					File f = new File(result.getKey());
					Files.createDirectories(f.toPath()
						.getParent());

					Files.write(f.toPath(), result.getValue()
						.buffer()
						.array());

					projectHelper.attachArtifact(project, "bnd-report", null, f);

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

	private void report(Processor processor) {
		for (String warning : processor.getWarnings()) {
			logger.warn("Warning : {}", warning);
		}
		for (String error : processor.getErrors()) {
			logger.error("Error   : {}", error);
		}
	}
}
