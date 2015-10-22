package es.upm.oeg.cidercl.util;

/**
 * Some utilities for strings
 *
 *
 */
public class StringTools {

	/**
	 * Splits compound words that uses camelCase (e.g., "myTest -> "my Test")
	 * Borrowed from
	 * http://stackoverflow.com/questions/2559759/how-do-i-convert-
	 * camelcase-into-human-readable-names-in-java
	 *
	 * @param s
	 *            compound word
	 * @return split string
	 */
	public static String splitCamelCase(String s) {

		return s.replaceAll(String.format("%s|%s|%s",
				"(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
	}



}
