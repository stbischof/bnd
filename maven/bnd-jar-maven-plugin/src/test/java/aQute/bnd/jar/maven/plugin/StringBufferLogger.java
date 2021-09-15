package aQute.bnd.jar.maven.plugin;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;

public class StringBufferLogger extends AbstractLogger {
	private final StringBuilder stringBuilder = new StringBuilder();

	public String getLoggedStuff() {
		return stringBuilder.toString();
	}

	public StringBufferLogger(final String name) {
		super(LEVEL_INFO, name);
	}

	@Override
	public void warn(String message, Throwable throwable) {
		System.out.println(message);
		stringBuilder.append(message)
			.append("\n");
	}

	@Override
	public void info(String message, Throwable throwable) {
		System.out.println(message);
		stringBuilder.append(message)
			.append("\n");
	}

	@Override
	public void fatalError(String message, Throwable throwable) {
		System.out.println(message);
		stringBuilder.append(message)
			.append("\n");
	}

	@Override
	public void error(String message, Throwable throwable) {
		System.out.println(message);
		stringBuilder.append(message)
			.append("\n");
	}

	@Override
	public void debug(String message, Throwable throwable) {
		System.out.println(message);
		stringBuilder.append(message)
			.append("\n");
	}

	@Override
	public Logger getChildLogger(String name) {
		return this;
	}
}
