/* Copyright (c) 2014 Jack126Guy. Refer to /LICENSE.txt for details. */
package tk.halfgray.pcommandbot;

import org.jibble.pircbot.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Bot that responds to commands sent through IRC.
 * A bot should be instantiated and then configured using
 * {@link #loadConfiguration(java.io.Reader)}.
 * The configuration may already load certain commands, but
 * additional commands may be registered with {@link #getResponders()}
 * and {@link #getSynonymousCommands()}.
 */
public class PCommandBot extends PircBot {
	/**
	 * Default username (login name) to use
	 */
	public static final String DEFAULT_USERNAME = "PCB";

	/**
	 * Default command prefix to use in case of missing configuration
	 */
	public static final String DEFAULT_COMMAND_PREFIX = "!";

	/**
	 * Default core command to use in case of missing configuration
	 */
	public static final String DEFAULT_CORE_COMMAND = "p";


	/**
	 * Version string
	 */
	public static final String PCB_VERSION_STRING = "PCommandBot v1.0.2 http://github.com/jack126guy/pcommandbot";

	/**
	 * Pattern that matches one or more characters not accepted in nicks
	 * according to RFC 2182
	 */
	public static final java.util.regex.Pattern NON_NICK_CHARACTERS = java.util.regex.Pattern.compile("[^a-zA-Z0-9\\[\\]\\\\\\`\\_\\^\\{\\|\\}]+");

	/**
	 * Configuration as a JSON object
	 */
	private JSONObject config;

	/**
	 * Whether this bot is ready to run (configuration loaded)
	 */
	private boolean isready;

	/**
	 * Server to connect to
	 */
	private String server;

	/**
	 * Port to use when connecting
	 */
	private int port;

	/**
	 * Nicks to use when connecting
	 */
	private String[] nicks;

	/**
	 * NickServ password
	 */
	private String nickservpassword;

	/**
	 * Channels to join
	 */
	private String[] channels;

	/**
	 * Command prefix
	 */
	private String commandprefix;

	/**
	 * Argument terminator
	 */
	private String argumentterminator;

	/**
	 * Default message when messaged privately
	 */
	private String defaultprivate;

	/**
	 * Response when mentioned
	 */
	private String mentionedresponse;

	/**
	 * Responders for commands
	 */
	private Map<String, Responder> responders;

	/**
	 * Synonymous commands
	 */
	private Map<String, String[]> syncommands;

	/**
	 * Lists of users. Keys are the channel names,
	 * and values are the lists.
	 */
	private Map<String, java.util.Set<String>> userlists;

	/**
	 * Create a new instance of this bot.
	 * Configuration must be loaded using {@link #loadConfiguration(java.io.Reader)}
	 */
	public PCommandBot() {
		super();
		responders = new HashMap<String, Responder>();
		syncommands = new HashMap<String, String[]>();
		userlists = new HashMap<String, java.util.Set<String>>();
		setVersion(PCB_VERSION_STRING);
		config = null;
		server = "";
		port = 0;
		this.setLogin(DEFAULT_USERNAME);
		nicks = new String[0];
		channels = new String[0];
		commandprefix = DEFAULT_COMMAND_PREFIX;
		argumentterminator = "";
		defaultprivate = "";
		mentionedresponse = "";
		isready = false;
	}

	/**
	 * Load the configuration for this bot from the given reader.
	 * The reader should point to a JSON-encoded object.
	 * If there is any error, the configuration is reset.
	 * @param creader Reader pointing to the configuration
	 * @throws java.io.IOException If there is a problem reading the configuration
	 * @throws IllegalArgumentException If there is a problem parsing
	 * the configuration from JSON, or if fields are missing
	 * @see #resetConfiguration()
	 */
	public void loadConfiguration(java.io.Reader creader)
		throws java.io.IOException {
		resetConfiguration();
		try {
			parseConfiguration(creader);
		} catch(Exception e) {
			resetConfiguration();
			if(e instanceof java.io.IOException) {
				throw (java.io.IOException) e;
			} else if(e instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e;
			}
		}

		//Load configuration fields
		if(config.get("server") instanceof String) {
			server = (String) config.get("server");
		} else {
			server = "";
			throw new IllegalArgumentException("Could not find server name");
		}

		if(config.get("port") instanceof Number) {
			port = ((Number) config.get("port")).intValue();
		} else {
			port = 0;
		}

		String username;
		if(config.get("username") instanceof String) {
			username = (String) config.get("username");
		} else {
			username = DEFAULT_USERNAME;
		}
		setLogin(username);

		//Convert nick list to array
		Object nickconfig = config.get("nicks");
		if((nickconfig instanceof List) && !((List) nickconfig).isEmpty()) {
			try {
				nicks = (String[]) ((List) nickconfig).toArray(new String[0]);
			} catch(ArrayStoreException e) {
				nicks = new String[0];
				throw new IllegalArgumentException("Invalid nick item encountered", e);
			}
			for(int i = 0; i < nicks.length; i++) {
				nicks[i] = Utilities.supertrim(nicks[i]);
			}
		} else {
			nicks = new String[0];
			throw new IllegalArgumentException("Could not find nicks to use");
		}

		if(config.get("nickserv_password") instanceof String) {
			nickservpassword = Utilities.supertrim((String) config.get("nickserv_password"));
		} else {
			nickservpassword = "";
		}

		//Convert channel list to array
		Object chanconfig = config.get("channels");
		if((chanconfig instanceof List) && !((List) chanconfig).isEmpty()) {
			try {
				channels = (String[]) ((List) chanconfig).toArray(new String[0]);
			} catch(ArrayStoreException e) {
				channels = new String[0];
				throw new IllegalArgumentException("Invalid channel item encountered", e);
			}
			for(int i = 0; i < channels.length; i++) {
				channels[i] = Utilities.supertrim(channels[i]);
			}
		} else {
			channels = new String[0];
		}

		String cmdpfx;
		if(config.get("command_prefix") instanceof String) {
			cmdpfx = Utilities.supertrim((String) config.get("command_prefix"));
		} else {
			cmdpfx = DEFAULT_COMMAND_PREFIX;
		}
		if(cmdpfx.isEmpty()) {
			cmdpfx = DEFAULT_COMMAND_PREFIX;
		}
		commandprefix = cmdpfx;

		if(config.get("argument_terminator") instanceof String) {
			argumentterminator = Utilities.supertrim((String) config.get("argument_terminator"));
		} else {
			argumentterminator = "";
		}

		if(config.get("default_private") instanceof String) {
			defaultprivate = Utilities.supertrim((String) config.get("default_private"));
		} else {
			defaultprivate = "";
		}

		if(config.get("mentioned_response") instanceof String) {
			mentionedresponse = Utilities.supertrim((String) config.get("mentioned_response"));
		} else {
			mentionedresponse = "";
		}

		//Load commands from configuration
		loadFixedCommands();
		loadSynonymousCommands();

		//Core command should replace any other command
		String corecommand;
		if(config.get("core_command") instanceof String) {
			corecommand = Utilities.supertrim((String) config.get("core_command"));
		} else {
			corecommand = DEFAULT_CORE_COMMAND;
		}
		if(corecommand.isEmpty()) {
			corecommand = DEFAULT_CORE_COMMAND;
		}
		getSynonymousCommands().remove(corecommand);
		Object adminpass = config.get("admin_password");
		if((adminpass instanceof String) && !((String) adminpass).isEmpty()) {
			getResponders().put(corecommand.toLowerCase(Locale.ENGLISH),
			new CoreCommandResponder((String) adminpass, this));
		} else {
			throw new IllegalArgumentException("Could not find admin password");
		}

		//Ready
		isready = true;
	}

	/**
	 * Parse the configuration.
	 * @param creader Reader pointing to a JSON-encoded object
	 * @throws java.io.IOException If there is a problem reading from {@code creader}
	 * @throws IllegalArgumentException If the configuration could not be parsed,
	 * or if the root value is not a JSON object
	 * @see #loadConfiguration(java.io.Reader)
	 */
	protected void parseConfiguration(java.io.Reader creader)
		throws java.io.IOException {
		Object temp;
		try {
			temp = new JSONParser().parse(creader);
		} catch(ParseException e) {
			throw new IllegalArgumentException("Could not parse configuration: "+e.getLocalizedMessage(), e);
		}
		if(temp instanceof JSONObject) {
			config = (JSONObject) temp;
		} else {
			throw new IllegalArgumentException("Root value of the configuration is not an object");
		}
	}

	/**
	 * Load fixed commands from the configuration,
	 * if the configuration has been parsed.
	 * @throws IllegalStateException If the configuration is not yet parsed
	 * @see #loadConfiguration(java.io.Reader)
	 */
	protected void loadFixedCommands() {
		if(config == null) {
			throw new IllegalStateException("Configuration has not yet been parsed");
		}
		Object fixedcommands = config.get("commands");
		//Ensure that it is a JSON object; do nothing if it is not
		if(!(fixedcommands instanceof java.util.Map)) {
			return;
		}
		Map fcmap = (Map) fixedcommands;
		Iterator fciter = fcmap.keySet().iterator();
		Object fckey;
		while(fciter.hasNext()) {
			fckey = fciter.next();
			//Ensure that the key and value are strings
			if(!((fckey instanceof String) && (fcmap.get(fckey) instanceof String))) {
				continue;
			}
			this.getResponders().put(((String) fckey).toLowerCase(Locale.ENGLISH),
				new FixedResponder((String) fcmap.get(fckey)));
		}
	}

	/**
	 * Load synonymous commands from the configuration,
	 * if the configuration has been parsed.
	 * @throws IllegalStateException If the configuration is not yet parsed
	 * @see #loadConfiguration(java.io.Reader)
	 */
	protected void loadSynonymousCommands() {
		if(config == null) {
			throw new IllegalStateException("Configuration has not yet been parsed");
		}
		Object fixedcommands = config.get("commands");
		//Ensure that it is a JSON object
		if(!(fixedcommands instanceof java.util.Map)) {
			return;
		}
		Map fcmap = (Map) fixedcommands;
		Iterator fciter = fcmap.keySet().iterator();
		Object fckey;
		Iterator syniter;
		Object synonym;
		List<String> synonyms = new java.util.ArrayList<String>();
		while(fciter.hasNext()) {
			fckey = fciter.next();
			synonyms.clear();
			//Ensure that the key is a string and the value a JSON array
			if(!((fckey instanceof String) && (fcmap.get(fckey) instanceof List))) {
				continue;
			}
			syniter = ((List) fcmap.get(fckey)).iterator();
			while(syniter.hasNext()) {
				synonym = syniter.next();
				//Ignore non-string synonyms
				if(synonym instanceof String) {
					synonyms.add((String) synonym);
				}
			}
			getSynonymousCommands().put(((String) fckey).toLowerCase(Locale.ENGLISH),
				synonyms.toArray(new String[0]));
		}
	}

	/**
	 * Reset configuration to its initial state.
	 * This does not disconnect the bot from the server.
	 * The bot can be reconfigured with {@link #loadConfiguration(java.io.Reader)}
	 */
	public void resetConfiguration() {
		config = null;
		server = "";
		port = 0;
		this.setLogin(DEFAULT_USERNAME);
		nicks = new String[0];
		channels = new String[0];
		commandprefix = DEFAULT_COMMAND_PREFIX;
		argumentterminator = "";
		defaultprivate = "";
		mentionedresponse = "";
		getResponders().clear();
		getSynonymousCommands().clear();
		isready = false;
	}

	/**
	 * Check if the bot is ready to run. Generally, this means that the
	 * configuration has been loaded with {@link #loadConfiguration(java.io.Reader)}
	 * @return {@code true} if the bot is ready, {@code false} otherwise
	 */
	public boolean isReady() {
		return isready;
	}

	/**
	 * Get the server that this bot should connect to
	 * @return Server name, or {@code ""} if the configuration
	 * is not loaded
	 * @see #loadConfiguration(java.io.Reader)
	 */
	protected String getConfiguredServer() {
		return server;
	}

	/**
	 * Get the port on which this bot should connect.
	 * An valid port number is between 1 and 65535 (inclusively);
	 * an invalid port number indicates that the default port
	 * should be used.
	 * @return Port number
	 * @see #loadConfiguration(java.io.Reader)
	 */
	protected int getConfiguredPort() {
		return port;
	}

	/**
	 * Get the nicks that this bot should use, in order of
	 * preference.
	 * @return Nicks
	 * @see #loadConfiguration(java.io.Reader)
	 */
	protected String[] getConfiguredNicks() {
		return nicks;
	}

	/**
	 * Get the password this bot should use to identify to NickServ
	 * @return Password for NickServ, or {@code ""} if no password was set
	 * @see #loadConfiguration(java.io.Reader)
	 */
	protected String getNickservPassword() {
		return nickservpassword;
	}

	/**
	 * Get the channels that this bot should join.
	 * @return Channels
	 * @see #loadConfiguration(java.io.Reader)
	 */
	protected String[] getConfiguredChannels() {
		return channels;
	}

	/**
	 * Get the command prefix.
	 * @return Command prefix
	 */
	protected String getCommandPrefix() {
		return commandprefix;
	}

	/**
	 * Get the argument terminator
	 * @return Argument terminator, or {@code ""}
	 * if none is configured
	 */
	protected String getArgumentTerminator() {
		return argumentterminator;
	}

	/**
	 * Get the default message sent as a response to a message
	 * that mentions this bot.
	 * @return Default response to a message that mentions the bot,
	 * or {@code ""} if none should be sent
	 */
	protected String getMentionedResponse() {
		return mentionedresponse;
	}

	/**
	 * Get the default message sent as a reply to a private message
	 * that does not include a command.
	 * @return Default response to a private message, or {@code ""}
	 * if none should be sent
	 */
	protected String getDefaultPrivate() {
		return defaultprivate;
	}

	/**
	 * Get lists of users, as a map of channel names to lists of nicks.
	 * These lists should be used to track nicks for processing mentions.
	 * All channel names and nicks are lowercase, such that
	 * {@code str.toLowerCase(java.util.Locale.ENGLISH).equals(str)}
	 * returns {@code true}.
	 * @return Lists of users, as a map
	 * @see String#toLowerCase()
	 */
	protected Map<String, java.util.Set<String>> getUserLists() {
		return userlists;
	}

	/**
	 * Get the the commands recognized by this bot as a map of
	 * command strings to {@link Responder} objects. This map
	 * can be used to add, remove, and change commands.
	 * Command strings are lowercase such that
	 * {@code str.toLowerCase(java.util.Locale.ENGLISH).equals(str)}
	 * returns {@code true}.
	 * @return Map representing the bot's commands
	 * @see String#toLowerCase(java.util.Locale)
	 */
	public Map<String, Responder> getResponders() {
		return responders;
	}

	/**
	 * Get the synonymous commands recognized by this bot as
	 * a map of command strings to synonyms (arrays of strings).
	 * An invocation of a synonymous command causes each command
	 * registered as a synonym to be invoked in turn; nonexistent synonyms
	 * are ignored. These synonyms cannot themselves be synonymous commands.
	 * This map can be used to add, remove, and change commands.
	 * All command strings (in both keys and values)are lowercase such that
	 * {@code str.toLowerCase(java.util.Locale.ENGLISH).equals(str)}
	 * returns {@code true}.
	 * @return Map representing the bot's synonymous commands
	 * @see String#toLowerCase(java.util.Locale)
	 */
	public Map<String, String[]> getSynonymousCommands() {
		return syncommands;
	}

	/**
	 * Start the bot. This will connect the bot to the server
	 * and listen for commands.
	 * @throws IllegalStateException If the bot is not ready
	 * @throws NickAlreadyInUseException If none of the configured nicks can be used
	 * @throws java.io.IOException If the {@code connect()} function cannot connect
	 * @throws IrcException If the {@code connect()} function cannot connect
	 * @see #isReady()
	 * @see PircBot#connect(String, int)
	 * @see PircBot#connect(String)
	 */
	public void start() throws NickAlreadyInUseException,
		java.io.IOException, IrcException {
		if(!isReady()) {
			throw new IllegalStateException("Bot is not ready");
		}
		for(String nick : getConfiguredNicks()) {
			setName((String) nick);
			try {
				if((getConfiguredPort() >= 1) && (getConfiguredPort() <= 65535)) {
					connect(getConfiguredServer(), getConfiguredPort());
				} else {
					connect(getConfiguredServer());
				}
			} catch(NickAlreadyInUseException e) {
				continue;
			}
			//No NickAlreadyInUseException: Success (or other problem)
			break;
		}
		if(!isConnected()) {
			throw new NickAlreadyInUseException("Could not connect with any nick");
		}
	}

	/**
	 * Perform actions upon connecting. This method is overridden
	 * to identify to NickServ and join channels.
	 * @see PircBot#onConnect()
	 */
	@Override
	protected void onConnect() {
		//Identify with NickServ
		if(getNickservPassword().length() > 0) {
			this.sendMessage("NickServ", "IDENTIFY "+getNickservPassword());
		}
		//Join channels
		for(String channel : getConfiguredChannels()) {
			joinChannel(channel);
		}
	}

	/**
	 * Process the user list. This method is overridden to place the nicks
	 * into a list of users for tracking.
	 * @param channel Channel for which the list applies
	 * @param users Users in the channel
	 * @see PircBot#onUserList(String, User[])
	 */
	@Override
	protected void onUserList(String channel, User[] users) {
		channel = channel.toLowerCase(Locale.ENGLISH);
		if(!getUserLists().containsKey(channel)) {
			getUserLists().put(channel, new java.util.HashSet<String>());
		}
		java.util.Set<String> userset = getUserLists().get(channel);
		userset.clear();
		for(User user : users) {
			//Remove any prefix
			userset.add(user.getNick().toLowerCase(Locale.ENGLISH).split("[^a-zA-Z0-9]*", 2)[1]);
		}
	}

	@Override
	protected void onJoin(String channel, String sender, String login, String hostname) {
		channel = channel.toLowerCase(Locale.ENGLISH);
		sender = sender.toLowerCase(Locale.ENGLISH);
		//It is redundant to add the bot when it joins
		if(!sender.equals(getNick().toLowerCase(Locale.ENGLISH))) {
			getUserLists().get(channel).add(sender);
		}
	}

	@Override
	protected void onPart(String channel, String sender, String login, String hostname) {
		channel = channel.toLowerCase(Locale.ENGLISH);
		sender = sender.toLowerCase(Locale.ENGLISH);
		if(sender.equals(getNick().toLowerCase(Locale.ENGLISH))) {
			//Bot parted: Remove user list to save memory
			getUserLists().remove(channel);
		} else {
			//Someone else parted
			getUserLists().get(channel).remove(sender);
		}
	}

	@Override
	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		channel = channel.toLowerCase(Locale.ENGLISH);
		recipientNick = recipientNick.toLowerCase(Locale.ENGLISH);
		if(recipientNick.equals(getNick().toLowerCase(Locale.ENGLISH))) {
			//Bot was kicked: Remove user list to save memory
			getUserLists().remove(channel);
		} else {
			//Someone else was kicked
			getUserLists().get(channel).remove(recipientNick);
		}
	}

	/**
	 * Get the nicks mentioned in a message, if any.
	 * @param channel Channel to which the message was sent
	 * @param message Message text
	 * @return Array of mentioned nicks, possibly empty
	 */
	protected String[] getMentions(String channel, String message) {
		String[] nicktokens = NON_NICK_CHARACTERS.split(message);
		int filtercount = 0;
		for(int i = 0; i < nicktokens.length; i++) {
			if(!getUserLists().get(channel.toLowerCase(Locale.ENGLISH)).contains(nicktokens[i].toLowerCase(Locale.ENGLISH))) {
				nicktokens[i] = "";
				filtercount++;
			}
		}
		String[] filterednts = new String[nicktokens.length-filtercount];
		int filteredi = 0;
		for (String nicktoken : nicktokens) {
			if (!nicktoken.isEmpty()) {
				filterednts[filteredi] = nicktoken;
				filteredi++;
			}
		}
		return filterednts;
	}

	/**
	 * Process any commands in a message into an array of responses.
	 * @param channel Channel from which the message originated,
	 * or {@code null} if it was a private message
	 * @param sender Sender of the message
	 * @param message Message text
	 * @return Array of responses (with mentions added), possibly empty
	 * @throws IllegalStateException If the bot is not ready
	 * @see #isReady()
	 */
	protected String[] processCommands(String channel, String sender, String message) {
		if(!isReady()) {
			throw new IllegalStateException("Bot is not ready");
		}
		String termpattern;
		if(getArgumentTerminator().isEmpty()) {
			termpattern = "$";
		} else {
			termpattern = "(?:"+Pattern.quote(getArgumentTerminator())+"|$)";
		}
		Matcher matcher = Pattern.compile(
				Pattern.quote(getCommandPrefix())
				+"([^\\p{javaSpaceChar}\\p{javaISOControl}]+?)"
				+"("
				+Utilities.WHITESPACE.pattern()
				+"(.*?)"
				+")?"
				+termpattern
				, Pattern.DOTALL
			)
			.matcher(message);
		String[] mentions;
		if(channel != null) {
			mentions = getMentions(channel, message);
		} else {
			mentions = new String[0];
		}
		List<String> responses = new java.util.ArrayList<String>();
		String lccmdstr;
		String argument;
		String[] synonyms;
		String response;
		while(matcher.find()) {
			lccmdstr = matcher.group(1).toLowerCase(Locale.ENGLISH);
			argument = matcher.group(3);
			//If there is nothing after the command string,
			//groups 2 and 3 will be null
			if(argument == null) {
				argument = "";
			} else {
				argument = Utilities.supertrim(argument);
			}
			//Check synonymous command
			if(getSynonymousCommands().containsKey(lccmdstr)) {
				synonyms = getSynonymousCommands().get(lccmdstr);
				for(String synonym : synonyms) {
					if(getResponders().containsKey(synonym)) {
						response = getResponders().get(synonym).respond(channel, sender, mentions, argument);
						if(!response.isEmpty()) {
							responses.add(Utilities.toMentionPrefix(mentions)+response);
						}
					}
				}
				//Do not check regular responder
				continue;
			}
			//Check regular responder
			if(getResponders().containsKey(lccmdstr)) {
				response = getResponders().get(lccmdstr).respond(channel, sender, mentions, argument);
				if(!response.isEmpty()) {
					responses.add(Utilities.toMentionPrefix(mentions)+response);
				}
			}
		}
		return responses.toArray(new String[0]);
	}

	/**
	 * Process potential commands from public messages. Responses to commands
	 * that mention other users in the channel will mention the same users.
	 * @param channel Channel from which the message was sent
	 * @param sender Sender of the message
	 * @param login Login name of the user
	 * @param hostname Hostname of the user
	 * @param message Message text
	 * @see PircBot#onMessage(String, String, String, String, String)
	 */
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		String[] responses = processCommands(channel, sender, message);
		if(responses.length > 0) {
			for(String response : responses) {
				sendMessage(channel, response);
			}
		} else {
			if(java.util.Arrays.asList(getMentions(channel, message.toLowerCase(Locale.ENGLISH)))
				.contains(getNick().toLowerCase(Locale.ENGLISH))
				&& !getMentionedResponse().isEmpty()) {
				sendMessage(channel, getMentionedResponse());
			}
		}
	}

	/**
	 * Process potential commands from private messages.
	 * @param sender Sender of the message
	 * @param login Login name of the user
	 * @param hostname Hostname of the user
	 * @param message Message test
	 * @see PircBot#onPrivateMessage(String, String, String, String)
	 */
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		String[] responses = processCommands(null, sender, message);
		if(responses.length > 0) {
			for(String response : responses) {
				sendMessage(sender, response);
			}
		} else {
			if(!getDefaultPrivate().isEmpty()) {
				sendMessage(sender, getDefaultPrivate());
			}
		}
	}
}
