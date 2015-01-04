/* Copyright (c) 2014 Jack126Guy. Refer to /LICENSE.txt for details. */
package tk.halfgray.pcommandbot;

import java.util.regex.Pattern;

/**
 * General utilities
 */
public class Utilities {
	/**
	 * Pattern that matches one or more whitespace characters.
	 * Whitespace characters are those that satisfy either
	 * {@link Character#isSpaceChar(int)} or {@link Character#isISOControl(int)}.
	 */
	public static final Pattern WHITESPACE = Pattern.compile("[\\p{javaSpaceChar}\\p{javaISOControl}]+");

	/**
	 * "Supertrim" a string. That is, remove all characters at the beginning
	 * and end that satisfy either {@link Character#isSpaceChar(int)} or
	 * {@link Character#isISOControl(int)}. This may remove more characters
	 * than {@link String#trim()}.
	 * @param str String to supertrim
	 * @return Supertrimmed string
	 */
	public static String supertrim(String str) {
		java.util.regex.Matcher m = Pattern.compile("^[\\p{javaSpaceChar}\\p{javaISOControl}]*(.*)[\\p{javaSpaceChar}\\p{javaISOControl}]*$", Pattern.DOTALL).matcher(str);
		m.matches();
		return m.group(1);
	}

	/**
	 * Convert a list of mentions into a prefix of the form
	 * {@code "a, b, c, ...: "}.
	 * @param mentions Mentioned nicks
	 * @return A prefix that includes the nicks, or {@code ""}
	 * if no nicks were given
	 */
	public static String toMentionPrefix(String[] mentions) {
		if(mentions.length == 0) {
			return "";
		} else {
			String tmp = mentions[0];
			for(int i = 1; i < mentions.length; i++) {
				tmp += (", " + mentions[i]);
			}
			tmp += ": ";
			return tmp;
		}
	}
}
