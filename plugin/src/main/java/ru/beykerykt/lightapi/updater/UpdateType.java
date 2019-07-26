/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vladimir Mikhailov <beykerykt@gmail.com>
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
package ru.beykerykt.lightapi.updater;

import org.bukkit.ChatColor;
import ru.beykerykt.lightapi.LightAPI;

import java.util.regex.Matcher;

public enum UpdateType {
	OUTDATE(ChatColor.GRAY + "Outdate"),
	MAJOR(ChatColor.RED + "Major"),
	MINOR(ChatColor.YELLOW + "Minor"),
	PATCH(ChatColor.GREEN + "Patch");

	private String name;

	UpdateType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static UpdateType compareVersion(String newVersion) {
		Integer[] pluginMatcher = getMatchers(LightAPI.getInstance().getDescription().getVersion());
		Integer[] updateMatcher = getMatchers(newVersion);

		if (pluginMatcher[0] < updateMatcher[0]) {
			return UpdateType.MAJOR;
		}

		if (pluginMatcher[1] < updateMatcher[1]) {
			return UpdateType.MINOR;
		}

		if (pluginMatcher[2] < updateMatcher[2]) {
			return UpdateType.PATCH;
		}

		return UpdateType.OUTDATE;
	}

	public static Integer[] getMatchers(String version) {
		Matcher matcher = Updater.regex.matcher(version);
		Integer[] list = new Integer[3];
		if (matcher.matches()) {
			list[0] = Integer.parseInt(matcher.group(1));
			list[1] = Integer.parseInt(matcher.group(2));
			list[2] = Integer.parseInt(matcher.group(3));
		}
		return list;
	}
}
