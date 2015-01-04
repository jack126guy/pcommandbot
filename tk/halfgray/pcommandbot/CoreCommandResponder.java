/* Copyright (c) 2014 Jack126Guy. Refer to /LICENSE.txt for details. */
package tk.halfgray.pcommandbot;

import java.util.Locale;

/**
 * <p>Utility to respond to the core command. The core command provides access
 * to internal bot functions. The first part of the argument (before
 * any whitespace) is the subcommand, which determines the function to execute.
 * Any additional information is the subargument. Some subcommands require
 * authentication.</p>
 * <p>Currently supported subcommands (case-insensitive):
 * <ul>
 * <li><i>auth</i>: Authenticate with the bot. The subargument is the password.</li>
 * <li><i>quit</i>: Quit the application (not advised if multiple bots are running
 * in a single application). Requires authentication.</li>
 * <li><i>join</i>: Join a channel. Requires authentication.</li>
 * <li><i>part</i>: Part a channel. Requires authentication.</li>
 * <li><i>time</i>: Output the time in UTC as reported by the system.</li>
 * <li><i>echo</i>: Reply with the subargument.</li>
 * </ul>
 * </p>
 */
public class CoreCommandResponder implements Responder {
	public static final String ABOUT_STRING = "This bot is running PCommandBot, version 1.0."
		+ " More information is at <http://github.com/jack126guy/pcommandbot>.";

	/**
	 * Password used for authenticating important actions
	 */
	private String password;

	/**
	 * Bot with which this responder is associated
	 */
	private PCommandBot bot;

	/**
	 * Nick of last authenticated user, in lowercase
	 */
	private String authenticateduser;

	/**
	 * Create a new CoreCommandResponder.
	 * @param pass Password for authenticating commands,
	 * which will be supertrimmed with {@link Utilities#supertrim(String)}
	 * @param b Bot with which this responder is associated
	 */
	public CoreCommandResponder(String pass, PCommandBot b) {
		password = Utilities.supertrim(pass);
		bot = b;
		authenticateduser = "";
	}

	@Override
	public String respond(String channel, String user, String[] mentions, String argument) {
		String[] parts = Utilities.WHITESPACE.split(argument, 2);
		String subcommand = parts[0].toLowerCase(java.util.Locale.ENGLISH);
		if(subcommand.isEmpty()) {
			return "Core subcommand not specified";
		}
		String subargument = parts.length > 1 ? parts[1] : "";
		if(subcommand.equals("about")) {
			return ABOUT_STRING;
		} else if(subcommand.equals("auth")) {
			if(channel != null) {
				return "Please do not authenticate publicly.";
			} else {
				if(authenticate(user, subargument)) {
					return "Authenticated. Please keep in mind that authentication"
						+ " applies for only one core command and is void if you change nicks.";
				} else {
					return "Invalid password";
				}
			}
		} else if(subcommand.equals("quit")) {
			if(isAuthenticated(user)) {
				bot.quitServer("Operator terminated the bot");
				System.exit(0);
				return "";
			} else {
				return "Not authenticated";
			}
		} else if(subcommand.equals("join")) {
			if(subargument.isEmpty()) {
				return "Please specify the channel to join";
			}
			if(isAuthenticated(user)) {
				bot.joinChannel(subargument);
				return "Joined "+subargument;
			} else {
				return "Not authenticated";
			}
		} else if(subcommand.equals("part")) {
			if(subargument.isEmpty()) {
				return "Please specify the channel to part";
			}
			if(isAuthenticated(user)) {
				bot.partChannel(subargument, "Operator commanded");
				if((channel != null)
					&& channel.toLowerCase(Locale.ENGLISH)
						.equals(subargument.toLowerCase(Locale.ENGLISH))) {
					//Parting the same channel from which the message was sent
					return "";
				} else {
					return "Parted "+subargument;
				}
			} else {
				return "Not authenticated";
			}
		} else if(subcommand.equals("time")) {
			java.text.DateFormat iso8601ish = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss ('UTC')");
			iso8601ish.setTimeZone(java.util.TimeZone.getTimeZone("Etc/UTC"));
			return iso8601ish.format(new java.util.Date());
		} else if(subcommand.equals("echo")) {
			return subargument;
		} else {
			return "Core subcommand not recognized";
		}
	}

	/**
	 * Attempt to authenticate the user. If authentication fails,
	 * the user who was previously authenticated retains that
	 * authentication.
	 * @param user Username to authenticate
	 * @param pass Password to check
	 * @return {@code true} if authentication succeeded, {@code false}
	 * otherwise
	 */
	protected boolean authenticate(String user, String pass) {
		if(password.equals(pass)) {
			authenticateduser = user.toLowerCase(Locale.ENGLISH);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if the user is authenticated to perform sensitive functions.
	 * If true, the authentication is voided, so that only one sensitive
	 * action may be performed with each authentication. (The user may
	 * authenticate again.)
	 * @param user Name of user to check
	 * @return {@code true} if the user is authenticated, {@code false}
	 * otherwise
	 */
	protected boolean isAuthenticated(String user) {
		if(authenticateduser.equals(user.toLowerCase(Locale.ENGLISH))) {
			authenticateduser = "";
			return true;
		} else {
			return false;
		}
	}
}
