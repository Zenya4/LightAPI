/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Qveshn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.beykerykt.lightapi.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Light static debug mechanics to print messages
 * Using:
 * <p>
 * 1. Init prefix and its foreground and background colors.
 * Example: Debug.setPrefix(plugin.getName(), ChatColor.WHITE, ChatColor.DARK_BLUE);
 * <p>
 * 2. Enable output
 * Example: Debug.setEnable(true);
 * <p>
 * 3. Print a message anywhere in the code
 * Example: Debug.print("Hello %s!", "World");
 */
public class Debug {

	private static boolean isEnabled = false;
	private static String coloredPrefix = "";

	private static String getColorPrefix(String prefix, ChatColor fg, ChatColor bg) {
		if (prefix == null || prefix.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		ChatColorToAnsi c;
		if (fg != null && (c = ChatColorToAnsi.valueOf(fg)) != null) {
			sb.append(';').append(c.fg());
		}
		if (bg != null && (c = ChatColorToAnsi.valueOf(bg)) != null) {
			sb.append(';').append(c.bg());
		}
		return sb.length() > 0
				? String.format("\u001b[%sm[%s]\u001b[m ", sb.substring(1), prefix)
				: String.format("[!!!%s] ", prefix);
	}

	/**
	 * Set prefix which will be printed in squared brackets before message
	 *
	 * @param prefix The prefix (usually the plugin name)
	 * @param fg     The foreground color of the prefix.
	 * @param bg     The background color of the prefix.
	 */
	public static void setPrefix(String prefix, ChatColor fg, ChatColor bg) {
		coloredPrefix = getColorPrefix(prefix, fg, bg);
	}

	/**
	 * Enable or disable print command
	 *
	 * @param enabled Enables print command if true. Otherwise, disables print command
	 */
	public static void setEnable(boolean enabled) {
		Debug.isEnabled = enabled;
	}

	/**
	 * Checks if debug print command is enabled.
	 *
	 * @return The true value if debug print command is enabled. Otherwise, returns false.
	 */
	public static boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Prints a formatted string using the specified format string and arguments like
	 * {@link String#format(String, Object...) String.format} does.
	 *
	 * @param format A format string if arguments exist or a simple text if no arguments exist.
	 * @param args   Arguments referenced by the format string.
	 */
	public static void print(String format, Object... args) {
		if (isEnabled) {
			Bukkit.getLogger().log(
					Level.INFO,
					String.format("%s%s",
							coloredPrefix,
							args == null || args.length == 0
									? format
									: String.format(format, args)
					));
		}
	}

	private enum AnsiColor {
		BLACK(0),
		RED(1),
		GREEN(2),
		YELLOW(3),
		BLUE(4),
		MAGENTA(5),
		CYAN(6),
		WHITE(7);

		private final int value;

		AnsiColor(int index) {
			this.value = index;
		}

		public int fg() {
			return value + 30;
		}

		public int bg() {
			return value + 40;
		}
	}

	@SuppressWarnings("unused")
	private enum ChatColorToAnsi {
		BLACK(ChatColor.BLACK, AnsiColor.BLACK, false),
		DARK_BLUE(ChatColor.DARK_BLUE, AnsiColor.BLUE, false),
		DARK_GREEN(ChatColor.DARK_GREEN, AnsiColor.GREEN, false),
		DARK_AQUA(ChatColor.DARK_AQUA, AnsiColor.CYAN, false),
		DARK_RED(ChatColor.DARK_RED, AnsiColor.RED, false),
		DARK_PURPLE(ChatColor.DARK_PURPLE, AnsiColor.MAGENTA, false),
		GOLD(ChatColor.GOLD, AnsiColor.YELLOW, false),
		GRAY(ChatColor.GRAY, AnsiColor.WHITE, false),
		DARK_GRAY(ChatColor.DARK_GRAY, AnsiColor.BLACK, true),
		BLUE(ChatColor.BLUE, AnsiColor.BLUE, true),
		GREEN(ChatColor.GREEN, AnsiColor.GREEN, true),
		AQUA(ChatColor.AQUA, AnsiColor.CYAN, true),
		RED(ChatColor.RED, AnsiColor.RED, true),
		LIGHT_PURPLE(ChatColor.LIGHT_PURPLE, AnsiColor.MAGENTA, true),
		YELLOW(ChatColor.YELLOW, AnsiColor.YELLOW, true),
		WHITE(ChatColor.WHITE, AnsiColor.WHITE, true);

		private final ChatColor chatColor;
		private final AnsiColor ansiColor;
		private final boolean bright;

		ChatColorToAnsi(ChatColor chatColor, AnsiColor ansiColor, boolean bright) {
			this.chatColor = chatColor;
			this.ansiColor = ansiColor;
			this.bright = bright;
		}

		public String fg() {
			return String.format(bright ? "%d;1" : "%d", ansiColor.fg());
		}

		public String bg() {
			return String.format(bright ? "%d;1" : "%d", ansiColor.bg());
		}

		private static Map<ChatColor, ChatColorToAnsi> mapFromChatColor = new HashMap<ChatColor, ChatColorToAnsi>();

		static {
			for (ChatColorToAnsi value : ChatColorToAnsi.values()) {
				mapFromChatColor.put(value.chatColor, value);
			}
		}

		public static ChatColorToAnsi valueOf(ChatColor chatColor) {
			return mapFromChatColor.get(chatColor);
		}
	}
}
