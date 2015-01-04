/* Copyright (c) 2014 Jack126Guy. Refer to /LICENSE.txt for details. */
package tk.halfgray.pcommandbot;

/**
 * Simple main class for text commands. One's own main class should be used
 * for more complex applications.
 */
public class Main {
	/**
	 * @param args Command-line arguments
	 */
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Usage: java -jar PCommandBot.jar [configfile]");
			System.exit(1);
		}
		java.io.File configfile = new java.io.File(args[0]);
		java.io.FileReader configreader = null;
		try {
			configreader = new java.io.FileReader(configfile);
		} catch(Exception e) {
			System.out.println("Could not read configuration file: "+e.getLocalizedMessage());
			System.exit(1);
		}
		PCommandBot bot = new PCommandBot();
		try {
			bot.loadConfiguration(configreader);
		} catch(java.io.IOException e) {
			System.out.println("Could not load configuration file: "+e.getLocalizedMessage());
			System.exit(1);
		} catch(IllegalArgumentException e) {
			System.out.println("Could not parse the configuration: "+e.getLocalizedMessage());
			System.exit(1);
		}
		try {
			bot.start();
		} catch(Exception e) {
			System.out.println("Could not start the bot: "+e.getMessage());
		}
	}
}
