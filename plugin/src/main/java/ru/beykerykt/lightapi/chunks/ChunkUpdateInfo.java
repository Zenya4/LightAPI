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
package ru.beykerykt.lightapi.chunks;

import org.bukkit.entity.Player;
import ru.beykerykt.lightapi.LightType;

import java.util.Collection;
import java.util.HashSet;

public class ChunkUpdateInfo {

	private int sectionMaskSky = 0;
	private int sectionMaskBlock = 0;
	private Collection<Player> players = new HashSet<Player>();

	public void add(LightType lightType, int sectionMask, Collection<? extends Player> players) {
		if (lightType == LightType.SKY) {
			this.sectionMaskSky |= sectionMask;
		} else {
			this.sectionMaskBlock |= sectionMask;
		}
		add(players);
	}

	private void add(Collection<? extends Player> players) {
		this.players.addAll(players);
	}

	public Collection<Player> getPlayers() {
		return players;
	}

	public int getSectionMaskSky() {
		return sectionMaskSky;
	}

	public int getSectionMaskBlock() {
		return sectionMaskBlock;
	}
}
