package biz.aQute.bnd.reporter.plugins.entries.bundle;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Processor;
import aQute.bnd.service.reporter.ReportEntryPlugin;
import aQute.lib.io.IO;
import biz.aQute.bnd.reporter.plugins.serializer.JsonReportSerializerPlugin;
import biz.aQute.bnd.reporter.plugins.transformer.JtwigTransformerPlugin;
import junit.framework.TestCase;

public class GogoPluginTest extends TestCase {

	public void testComponents() throws Exception {

		try (final Jar jar = new Jar("jar", "testresources/gogoEntry/source.jar");
			final Processor p = new Processor()) {

			final GogoPlugin e = new GogoPlugin();
			final Map<String, Object> result = new HashMap<>();
			e.setReporter(p);

			result.put(e.getProperties()
				.get(ReportEntryPlugin.ENTRY_NAME_PROPERTY), e.extract(jar, Locale.forLanguageTag("und")));

			assertTrue(p.isOk());

			final ByteArrayOutputStream s = new ByteArrayOutputStream();
			new JsonReportSerializerPlugin().serialize(result, s);

			final StringBuffer ee = new StringBuffer();

			for (final String l : Files.readAllLines(Paths.get("testresources/gogoEntry/result.json"),
				StandardCharsets.UTF_8)) {

				ee.append(l + "\n");
			}
			ee.deleteCharAt(ee.length() - 1);

			assertEquals(ee.toString(), new String(s.toByteArray()));
		}
	}

	public void testfelixgogo() throws Exception {

		try (final ByteArrayOutputStream model = new ByteArrayOutputStream();
			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			final Jar jar = new Jar("jar", "testresources/gogoEntry/gogo.jar");
			final Processor p = new Processor()) {

			final GogoPlugin e = new GogoPlugin();
			final Map<String, Object> result = new HashMap<>();
			e.setReporter(p);

			result.put(e.getProperties()
				.get(ReportEntryPlugin.ENTRY_NAME_PROPERTY), e.extract(jar, Locale.forLanguageTag("und")));

			assertTrue(p.isOk());


			new JsonReportSerializerPlugin().serialize(result, model);

			// System.out.println(new String(model.toByteArray()));

			final JtwigTransformerPlugin t = new JtwigTransformerPlugin();

			t.transform(IO.stream(model.toByteArray()),
				JtwigTransformerPlugin.class.getResourceAsStream("templates/readme.twig"), output,
				new HashMap<String, String>());

			System.out.println(new String(output.toByteArray()));

		}
	}
}
