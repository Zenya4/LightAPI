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
package ru.beykerykt.lightapi.server.nms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class NmsHandlerBase implements INMSHandler {

	@Override
	public boolean isValidSectionY(int sectionY) {
		return sectionY >= 0 && sectionY < 16;
	}

	@Override
	public int asSectionMask(int sectionY) {
		return 1 << sectionY;
	}

	protected int getViewDistance(Player player) {
		return Bukkit.getViewDistance();
	}

	private boolean isVisibleToPlayer(World world, int chunkX, int chunkZ, Player player) {
		Location location = player.getLocation();
		if (!world.equals(location.getWorld())) return false;
		double dx = chunkX - (location.getBlockX() >> 4);
		double dz = chunkZ - (location.getBlockZ() >> 4);
		return (int) Math.sqrt(dx * dx + dz * dz) < getViewDistance(player);
	}

	@Override
	public Collection<? extends Player> filterVisiblePlayers(
			World world, int chunkX, int chunkZ, Collection<? extends Player> players
	) {
		List<Player> result = new ArrayList<Player>();
		for (Player player : players) {
			if (isVisibleToPlayer(world, chunkX, chunkZ, player)) {
				result.add(player);
			}
		}
		return result;
	}

	private int getDeltaLight(int x, int dx) {
		return (((x ^ ((-dx >> 4) & 15)) + 1) & (-(dx & 1)));
	}

	@Override
	public List<ChunkInfo> collectChunks(World world, int blockX, int blockY, int blockZ, int lightLevel) {
		List<ChunkInfo> list = new ArrayList<ChunkInfo>();
		if (lightLevel > 0) {
			for (int dx = -1; dx <= 1; dx++) {
				int lightLevelX = lightLevel - getDeltaLight(blockX & 15, dx);
				if (lightLevelX > 0) {
					for (int dz = -1; dz <= 1; dz++) {
						int lightLevelZ = lightLevelX - getDeltaLight(blockZ & 15, dz);
						if (lightLevelZ > 0) {
							for (int dy = -1; dy <= 1; dy++) {
								int lightLevelY = lightLevelZ - getDeltaLight(blockY & 15, dy);
								if (lightLevelY > 0) {
									int sectionY = blockY >> 4;
									if (isValidSectionY(sectionY)) {
										int chunkX = blockX >> 4;
										int chunkZ = blockZ >> 4;
										ChunkInfo cCoord = new ChunkInfo(
												world,
												chunkX + dx,
												((sectionY + dy) << 4) + 15,
												chunkZ + dz,
												world.getPlayers());
										list.add(cCoord);
									}
								}
							}
						}
					}
				}
			}
		}
		return list;
	}

	@Override
	public void sendChunkSectionsUpdate(
			World world, int chunkX, int chunkZ, int sectionsMask, Collection<? extends Player> players
	) {
		for (Player player : players) {
			sendChunkSectionsUpdate(world, chunkX, chunkZ, sectionsMask, player);
		}
	}

	protected void recalculateLighting(World world, int x, int y, int z) {
		throw new UnsupportedOperationException("recalculateLighting not implemented");
	}

	protected void recalculateNeighbour(World world, int x, int y, int z) {
		recalculateLighting(world, x - 1, y, z);
		recalculateLighting(world, x + 1, y, z);
		recalculateLighting(world, x, y - 1, z);
		recalculateLighting(world, x, y + 1, z);
		recalculateLighting(world, x, y, z - 1);
		recalculateLighting(world, x, y, z + 1);
	}

	@Deprecated
	@Override
	public List<ChunkInfo> collectChunks(World world, int x, int y, int z) {
		return collectChunks(world, x, y, z, 15);
	}

	@Deprecated
	@Override
	public void sendChunkUpdate(World world, int chunkX, int chunkZ, Collection<? extends Player> players) {
		sendChunkUpdate(world, chunkX, chunkZ, isValidSectionY(-1) ? 0x3ffff : 0xffff, players);
	}

	@Deprecated
	@Override
	public void sendChunkUpdate(World world, int chunkX, int chunkZ, Player player) {
		sendChunkSectionsUpdate(world, chunkX, chunkZ, isValidSectionY(-1) ? 0x3ffff : 0xffff, player);
	}

	@Deprecated
	@Override
	public void sendChunkUpdate(World world, int chunkX, int y, int chunkZ, Collection<? extends Player> players) {
		int mask = getThreeSectionsMask(y);
		if (mask != 0) sendChunkUpdate(world, chunkX, chunkZ, mask, players);
	}

	@Deprecated
	@Override
	public void sendChunkUpdate(World world, int chunkX, int y, int chunkZ, Player player) {
		int mask = getThreeSectionsMask(y);
		if (mask != 0) sendChunkSectionsUpdate(world, chunkX, chunkZ, mask, player);
	}

	private int getThreeSectionsMask(int y) {
		return (isValidSectionY(y) ? asSectionMask(y) : 0)
				| (isValidSectionY(y - 1) ? asSectionMask(y - 1) : 0)
				| (isValidSectionY(y + 1) ? asSectionMask(y + 1) : 0);
	}
}
