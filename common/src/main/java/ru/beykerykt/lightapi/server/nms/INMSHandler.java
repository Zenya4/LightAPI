/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vladimir Mikhailov <beykerykt@gmail.com>
 * Copyright (c) 2021 LOOHP <jamesloohp@gmail.com>
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
package ru.beykerykt.lightapi.server.nms;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;

import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public interface INMSHandler {

	// Lights...
	@Deprecated
	void createLight(World world, int x, int y, int z, int light);

	void createLight(World world, int x, int y, int z, LightType lightType, int light);

	@Deprecated
	void deleteLight(World world, int x, int y, int z);

	void deleteLight(World world, int x, int y, int z, LightType lightType);

	@Deprecated
	void recalculateLight(World world, int x, int y, int z);

	// Chunks...
	@Deprecated
	List<ChunkInfo> collectChunks(World world, int x, int y, int z);

	@Deprecated
	List<ChunkInfo> collectChunks(World world, int blockX, int blockY, int blockZ, int lightLevel);

	List<ChunkInfo> collectChunks(World world, int blockX, int blockY, int blockZ, LightType lightType, int lightLevel);

	@Deprecated
	void sendChunkSectionsUpdate(
			World world, int chunkX, int chunkZ, int sectionsMask, Collection<? extends Player> players
	);

	@Deprecated
	void sendChunkSectionsUpdate(World world, int chunkX, int chunkZ, int sectionsMask, Player player);

	@Deprecated
	void sendChunkUpdate(World world, int chunkX, int chunkZ, Collection<? extends Player> players);

	@Deprecated
	void sendChunkUpdate(World world, int chunkX, int chunkZ, Player player);

	@Deprecated
	void sendChunkUpdate(World world, int chunkX, int y, int chunkZ, Collection<? extends Player> players);

	@Deprecated
	void sendChunkUpdate(World world, int chunkX, int y, int chunkZ, Player player);

	@Deprecated
	void sendChunkSectionsUpdate(
			World world,
			int chunkX,
			int chunkZ,
			int sectionsMaskSky,
			int sectionsMaskBlock,
			Collection<? extends Player> players
	);

	@Deprecated
	void sendChunkSectionsUpdate(
			World world,
			int chunkX,
			int chunkZ,
			int sectionsMaskSky,
			int sectionsMaskBlock,
			Player player
	);

	void sendChunkSectionsUpdate(
			World world,
			int chunkX,
			int chunkZ,
			BitSet sectionsMaskSky,
			BitSet sectionsMaskBlock,
			Collection<? extends Player> players
	);

	void sendChunkSectionsUpdate(
			World world,
			int chunkX,
			int chunkZ,
			BitSet sectionsMaskSky,
			BitSet sectionsMaskBlock,
			Player player
	);

	// Utils...
	boolean isValidSectionY(World world, int sectionY);

	@Deprecated
	int asSectionMask(int sectionY);

	BitSet asSectionMask(World world, int sectionY);

	Collection<? extends Player> filterVisiblePlayers(
			World world, int chunkX, int chunkZ, Collection<? extends Player> players
	);

	boolean isSupported(World world, LightType lightType);

	int getMinLightHeight(World world);

	int getMaxLightHeight(World world);
}
