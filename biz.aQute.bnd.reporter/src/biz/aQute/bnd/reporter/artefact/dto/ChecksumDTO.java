package biz.aQute.bnd.reporter.artefact.dto;

import org.osgi.dto.DTO;

/**
 * A representation of Checksums of an artefact.
 */
public class ChecksumDTO extends DTO {

	/**
	 * The md5 checksum of the artefact.
	 * <p>
	 * </p>
	 */
	public String	md5;

	/**
	 * The sha1 checksum of the artefact.
	 * <p>
	 * </p>
	 */
	public String	sha1;

	/**
	 * The sha256 checksum of the artefact.
	 * <p>
	 * </p>
	 */
	public String	sha256;


}
