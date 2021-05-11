package aQute.bnd.exporter.feature;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.osgi.framework.Constants;
import org.osgi.resource.Capability;
import org.osgi.util.feature.Feature;
import org.osgi.util.feature.FeatureBuilder;
import org.osgi.util.feature.FeatureBundle;
import org.osgi.util.feature.FeatureConfiguration;
import org.osgi.util.feature.FeatureExtension;
import org.osgi.util.feature.Features;
import org.osgi.util.feature.ID;

import aQute.bnd.annotation.plugin.BndPlugin;
import aQute.bnd.build.Project;
import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.FileResource;
import aQute.bnd.osgi.Resource;
import aQute.bnd.osgi.resource.CapReqBuilder;
import aQute.bnd.service.export.Exporter;

@BndPlugin(name = "Feature Exporter")
public class FeatureExporter implements Exporter {

	private Project	project;

	private String	type;

	@Override
	public String[] getTypes() {

		return new String[] {
			"feature", "feature.json"
		};
	}

	@Override
	public Map.Entry<String, Resource> export(String type, final Project project, Map<String, String> options)
		throws Exception {

		this.project = project;
		this.type = type;
		doFeatureFile();
		return null;
	}

	private void doFeatureFile() throws Exception {

		ID aId = new ID("groupId", "artefactId", version().orElse("0.0.0-INITIAL"));

		FeatureBuilder featureBuilder = Features.getBuilderFactory()
			.newFeatureBuilder(aId);

		title().ifPresent(featureBuilder::setName);
		decription().ifPresent(featureBuilder::setDescription);
		license().ifPresent(featureBuilder::setLicense);
		vendor().ifPresent(featureBuilder::setVendor);

		featureConfigurations()
			.ifPresent(cs -> featureBuilder.addConfigurations(cs.toArray(FeatureConfiguration[]::new)));
		featureBundles().ifPresent(bs -> featureBuilder.addBundles(bs.toArray(FeatureBundle[]::new)));
		variables().ifPresent(featureBuilder::addVariables);
		extensions().ifPresent(es -> featureBuilder.addExtensions(es.toArray(FeatureExtension[]::new)));

		// capabilities().ifPresent(cap ->
		// featureBuilder.addCapabilities(cap.toArray(FeatureCapability[]::new)));
		// requirement().ifPresent(rs ->
		// featureBuilder.addRequirements(rs.toArray(FeatureRequirement[]::new)));

		// launcher
		// frameworkProperies().ifPresent(featureBuilder.getFrameworkProperties()::putAll);

		// FileWriter
		String fileName = featureName(project);
		File jsonPath = project.getTargetDir()
			.toPath()
			.resolve(fileName)
			.toFile();

		if (jsonPath.exists()) {
			jsonPath.delete();
		}
		jsonPath.createNewFile();

		Feature f = featureBuilder.build();
		String json = Utils.toJson(f);
		Files.writeString(jsonPath.toPath(), json);
		Resource jsonResource = new FileResource(jsonPath);
	}

	private Optional<String> version() {

		return Optional.ofNullable(project.getBundleVersion());
	}

	private Optional<String> decription() {

		return Optional.ofNullable(project.getBundleDescription());
	}

	private Optional<String> license() {

		return Optional.ofNullable(project.get(Constants.BUNDLE_LICENSE));
	}

	private Optional<String> title() {

		return Optional.ofNullable(project.getBundleName());
	}

	private Optional<String> vendor() {

		return Optional.ofNullable(project.getBundleVendor());
	}

	private static String featureName(Project project) {

		String pName = project.getName();
		if ("bnd.bnd".equals(pName)) {
			return "feature.json";
		}
		return "feature-" + pName.replace(".bndrun", ".json");
	}

	Optional<List<FeatureConfiguration>> featureConfigurations() {

		return Optional.empty();

	}

	Optional<List<FeatureBundle>> featureBundles() {

		try {
			return Optional.ofNullable(project.getRunbundles()
				.stream()
				.map(Utils::toFeatureBundle)
				.collect(Collectors.toList()));
		} catch (Exception e) {

			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	Optional<List<Capability>> capabilities() {

		Parameters parameters = project.getProvideCapability();
		List<Capability> list = CapReqBuilder.getCapabilitiesFrom(parameters);
		return Optional.ofNullable(list);

	}

	// Optional<List<MatchingRequirement>> requirement() {
	//
	// Parameters parameters = project.getRequireCapability();
	// List<MatchingRequirement> list =
	// CapReqBuilder.getRequirementsFrom(parameters)
	// .stream()
	// .map(MRI::new)
	// .collect(Collectors.toList());
	//
	// return Optional.ofNullable(list);
	// }

	Optional<Map<String, String>> variables() {

		Map<String, String> vars = project.getProperties()
			.entrySet()
			.stream()
			.filter(isVariable())
			.collect(Collectors.toMap(e -> e.getKey()
				.toString(),
				e -> e == null ? ""
					: e.getValue()
						.toString()));
		return Optional.ofNullable(vars);
	}

	private Predicate<? super Entry<Object, Object>> isVariable() {

		return e -> {
			if (e.getKey() == null) {
				return false;
			}
			if (e.getKey()
				.toString()
				.startsWith("-")) {
				return false;
			}
			if (e.getKey()
				.toString()
				.startsWith("Bundle-")) {
				return false;
			}
			if (e.getKey()
				.toString()
				.startsWith("basedir")) {
				return false;
			}
			if (e.getKey()
				.toString()
				.startsWith("Provide-Capability")) {
				return false;
			}
			if (e.getKey()
				.toString()
				.startsWith("Require-Capability")) {
				return false;
			}

			return true;
		};
	}

	Optional<Map<String, String>> frameworkProperies() {

		return Optional.ofNullable(project.getRunProperties());
	}

	private Optional<List<FeatureExtension>> extensions() {

		List<FeatureExtension> exts = project.getPlugins(ExtensionPlugin.class)
			.stream()
			.peek(System.out::println)
			.map(ExtensionPlugin::toExtension)
			.collect(Collectors.toList());

		return Optional.ofNullable(exts);
	}

	// private static class MRI extends
	// org.apache.felix.utils.resource.RequirementImpl
	// implements MatchingRequirement {
	//
	// public MRI(Requirement r) {
	//
	// super(r.getResource(), r.getNamespace(), r.getDirectives(),
	// r.getAttributes(), null);
	// }
	// }

}
