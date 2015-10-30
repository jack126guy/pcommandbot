/* Copyright (c) 2015 Jack126Guy. Refer to /LICENSE.txt for details. */
package tk.halfgray.pcommandbot;

/**
 * Responder that responds with a fixed message.
 * Circumstances of the original message are ignored.
 */
public class FixedResponder implements Responder {
	/**
	 * String for the response
	 */
	protected String fixedResponse;

	/**
	 * Create a new FixedResponder that responds with the given message.
	 * @param message Fixed message that this responder uses
	 */
	public FixedResponder(String message) {
		fixedResponse = message;
	}

	@Override
	public String respond(String channel, String user, String[] mentions, String argument) {
		return fixedResponse;
	}
}
