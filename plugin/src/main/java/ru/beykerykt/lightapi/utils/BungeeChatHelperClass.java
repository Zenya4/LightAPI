/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vladimir Mikhailov <beykerykt@gmail.com>
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

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import ru.beykerykt.lightapi.LightAPI;

public class BungeeChatHelperClass {

	public static boolean hasBungeeChatAPI() {
		try {
			Class<?> clazz = Class.forName("net.md_5.bungee.api.chat.TextComponent");
			if (clazz != null) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void sendMessageTitle(Player player, LightAPI plugin) {
		TextComponent title = new TextComponent("§b-------< §eLightAPI-fork ");
		TextComponent version = new TextComponent("§f" + plugin.getDescription().getVersion());
		version.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lightapi update"));
		version.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder("Click here for check update\n§7/lightapi update").create()));
		title.addExtra(version);
		title.addExtra(new TextComponent(" §b>-------"));
		sendMessage(player, title, plugin);
	}

	public static void sendMessageAboutPlugin(Player player, LightAPI plugin) {
		sendMessageTitle(player, plugin);

		plugin.sendMessage(player, "§bDevelopers: §f%s", LightAPI.join("§7, §f", plugin.getDescription().getAuthors()));
		plugin.sendMessage(player, "§bSource code: §f%s", LightAPI.sourceCodeUrl);
		TextComponent licensed = new TextComponent("§bLicensed under ");
		TextComponent MIT = new TextComponent("§fMIT License");
		MIT.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://opensource.org/licenses/MIT/"));
		MIT.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder("Goto for information about license!").create()));
		licensed.addExtra(MIT);
		sendMessage(player, licensed, plugin);

		plugin.printServerInfo(player);

		TextComponent text = new TextComponent("§e§l<");
		TextComponent sourcecode = new TextComponent("§6Source code");
		sourcecode.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, LightAPI.sourceCodeUrl));
		sourcecode.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder("Goto the GitHub!").create()));
		text.addExtra(sourcecode);
		text.addExtra(new TextComponent("§e§l> <"));

		TextComponent developer = new TextComponent("§6Developer");
		developer.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, LightAPI.authorUrl));
		developer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder("§f" + LightAPI.author).create()));
		text.addExtra(developer);
		text.addExtra(new TextComponent("§e§l> <"));

		TextComponent contributors = new TextComponent("§6Contributors");
		contributors.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, LightAPI.contributorsUrl));
		contributors.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder("ALIENS!!").create()));
		text.addExtra(contributors);
		text.addExtra(new TextComponent("§e§l> <"));

		TextComponent checkUpdate = new TextComponent("§6Check update");
		checkUpdate.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lightapi update"));
		checkUpdate.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder("Click here for check update\n§7/lightapi update").create()));
		text.addExtra(checkUpdate);
		text.addExtra(new TextComponent("§e§l>"));
		sendMessage(player, text, plugin);


	}

	private static void sendMessage(Player player, BaseComponent component, LightAPI plugin) {
		BaseComponent prefix = new TextComponent(plugin.messagePrefix());
		prefix.addExtra(component);
		player.spigot().sendMessage(prefix);
	}
}
