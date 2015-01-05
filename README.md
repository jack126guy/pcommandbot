# PCommandBot

PCommandBot is a highly configurable and extensible bot for responding to commands sent through IRC. These commands can be used to provide information, play games, or do random stuff.

## Usage

PCommandBot requires Java 6 or later to run.

In the simplest case, you can set up PCommandBot to reply to commands with a fixed message:

	java -jar PCommandBot.jar [configfile]

For more information on the configuration file, please read `CONFIGURING.txt`.

For other tasks, you will need to use the `PCommandBot` class and load the configuration in your own application. More information is available in the [Javadoc](http://jack126guy.github.io/pcommandbot/javadoc/tk/halfgray/pcommandbot/PCommandBot.html).

## Interaction

A command consists of a command prefix, a command string, whitespace, and a possible argument. By default, the argument is read until the end of the message, but PCommandBot may be configured with an argument terminator that signals the end. (This allows for multiple commands in a single message.) All fields except the command string are case-sensitive.

By default, the command prefix is `!`, so `!help` invokes a command with the string `help`.

### Administration

A special command called the core command is used to access core functionality of the bot. By default the core command is `p`.

The first part of the argument (before whitespace) is the subcommand, which determines the action to be taken. The remainder of the argument is the subargument, which is passed to the subcommand.

Currently, the recognized subcommands are:

* *auth*: Authenticate with the bot to perform sensitive actions. The subargument is the configured password.
* *quit*: Quit the application. (This calls `System.exit()`, so this is not recommended for multi-bot applications.) Requires authentication.
* *join*: Join a channel. The subargument is the channel to join. Requires authentication.
* *part*: Part a channel. The subargument is the channel to part. Requires authentication.
* *time*: Get the time in UTC.
* *echo*: Reply with the subargument.

## Building From Source

Source code is available at <http://github.com/jack126guy/pcommandbot>.

The `sources.txt` and `manifest.txt` files are provided in the source distribution to enable building with just the JDK:

	cd [source dir]
	javac -d [outside dir] @sources.txt
	cd [outside directory]
	jar cmf [source dir]/manifest.txt PCommandBot.jar *

## Other Information

The latest version is 1.0.3.

PCommandBot is available under the GNU General Public License, version 3 or later. Different parts of the application are available under different terms; refer to `LICENSE.txt` for details.

PCommandBot is based on the [PircBot](http://www.jibble.org/pircbot.php) framework and uses [JSON-Simple](https://code.google.com/p/json-simple/) for configuration processing.