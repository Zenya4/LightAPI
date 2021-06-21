/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Vladimir Mikhailov <beykerykt@gmail.com>
 * Copyright (c) 2021 Qveshn
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
package ru.beykerykt.lightapi.server;

import org.bukkit.Bukkit;
import ru.beykerykt.lightapi.server.nms.INMSHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerModManager {

	private static Map<String, ServerModInfo> supportImpl = new ConcurrentHashMap<String, ServerModInfo>();
	private static INMSHandler handler;

	public static Class<? extends INMSHandler> findImplementaion(String modName) {
		ServerModInfo impl = supportImpl.get(modName);
		return impl != null ? impl.getVersions().get(getServerVersion()) : null;
	}

	public static void initImplementaion(Class<? extends INMSHandler> clazz) throws Exception {
		ServerModManager.handler = clazz.getConstructor().newInstance();
	}

	public static void shutdown() {
		if (isInitialized()) {
			handler = null;
		}
	}

	public static boolean isInitialized() {
		return handler != null;
	}

	public static boolean registerServerMod(ServerModInfo info) {
		if (supportImpl.containsKey(info.getModName())) {
			return false;
		}
		supportImpl.put(info.getModName(), info);
		return true;
	}

	public static boolean unregisterServerMod(String modName) {
		if (supportImpl.containsKey(modName)) {
			return false;
		}
		supportImpl.remove(modName);
		return true;
	}

	public static INMSHandler getNMSHandler() {
		return handler;
	}

	public static String getServerVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}

	public static String getServerName() {
		return Bukkit.getVersion().split("-")[1];
	}

	public static String getBukkitName() {
		return Bukkit.getName();
	}
}
