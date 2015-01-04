/* Copyright (c) 2014 Jack126Guy. Refer to /LICENSE.txt for details. */
package tk.halfgray.pcommandbot;

/**
 * Utility for responding to a command, depending on the nature of
 * the message that invoked it. A Responder is generally bound to a command
 * string and invoked when the command is received. The Responder can act
 * on the additional information received, or ignore it.
 * A class shall implement the
 * {@link #respond(String, String, String[], String)} method to provide
 * an appropriate response.
 */
public interface Responder {
	/**
	 * Respond to a command. For public messages, the users mentioned
	 * in the original message will be mentioned in the response automatically.
	 * Note that "trimmed" as described in the argument is actually a supertrim,
	 * as described in {@link Utilities#supertrim(String)}.
	 * @param channel Channel from which this message originated
	 * ({@code null} for private messages)
	 * @param user User from which the command was sent
	 * @param mentions Users mentioned in the original message
	 * @param argument Argument to the command, trimmed
	 * @return A response string, or {@code ""}
	 * to indicate no response (not recommended in most cases)
	 */
	String respond(String channel, String user, String[] mentions, String argument);
}
