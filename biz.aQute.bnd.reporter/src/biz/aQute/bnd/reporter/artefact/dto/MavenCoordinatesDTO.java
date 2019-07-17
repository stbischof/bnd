package biz.aQute.bnd.reporter.artefact.dto;

import org.osgi.dto.DTO;

/**
 * A representation of a maven coordinates.
 */
public class MavenCoordinatesDTO extends DTO {

	/**
	 * The groupId of the maven artefact.
	 * <p>
	 * </p>
	 */
	public String	groupId;
	/**
	 * The artifactId of the maven artefact.
	 * <p>
	 * </p>
	 */
	public String artifactId;

	/**
	 * The version of the maven artefact.
	 * <p>
	 * </p>
	 */
	public String	version;


}
