# Configuring PCommandBot

PCommandBot takes its configuration from a [JSON](http://json.org/)-encoded configuration file. A template is provided in `pcb-config.json`.

The root should be an object with the following keys:

* *server*: (String) The server to connect to
* *port*: (Number) The port to connect to (omit to use the default set by PircBot)
* *nicks*: (Array of strings) Nicks to try, in order
* *username*: (String) Username that appears when users run a WHOIS
* *nickserv_password*: (String) Password to use to identify to NickServ (omit if not needed)
* *channels*: (String) Channels to join upon connecting (more can be joined later)
* *command_prefix*: (String) Text that signals a command (omit to use the default ["!"])
* *argument_terminator*: (String) Text that signals the end of an argument/command (omit or leave empty to let arguments run to the end of the message)
* *core_command*: (String) Command that provides access to core functionality (omit to use the default ["p"])
* *admin_password*: (String) Password to access certain core functionality (required)
* *commands*: (Object) Basic commands. String values indicate that the bot should reply with a fixed message; array values indicate that the bot should execute one or more other commands. (These may be externally configured, but they cannot lead to still other commands.)
* *default_private*: (String) Default response to a private message that does not contain any commands (omit to send no message)
* *mentioned_response*: (String) Response when the bot is mentioned in a channel without any command (omit to send no message)