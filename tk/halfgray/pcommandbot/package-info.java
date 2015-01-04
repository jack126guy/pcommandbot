/**
 * <p>PCommandBot is a bot that responds to commands sent through IRC.</p>
 * <p>A command consists of a command prefix, a command string, whitespace,
 * and an argument. Command prefixes are case-sensitive, but
 * command strings are not. The argument is read until the end of the message,
 * or until the (case-sensitive) argument terminator is reached
 * (if the bot is configured with one). The argument is always "supertrimmed"
 * with {@link tk.halfgray.pcommandbot.Utilities#supertrim(String)}
 * before processing. Any text before the command prefix is ignored
 * for processing (except for mentions).</p>
 * <p>Commands may be sent publicly or privately. For public commands,
 * any users whose names are included anywhere in the message are considered
 * "mentioned," and any response to the command mentions them as well.</p>
 * <p>An application should create a new instance of
 * {@link tk.halfgray.pcommandbot.PCommandBot} and configure it.</p>
 */
package tk.halfgray.pcommandbot;
