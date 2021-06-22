/*
 * The MIT License (MIT)
 *
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public abstract class NmsHandlerBase implements INMSHandler {

	@Override
	public int getMinLightHeight(World world) {
		// Always 0 for 1.8 - 1.13.2 (v1_8_R1 - v1_13_R2) (same as world min height)
		// Should be overridden for other versions
		return 0;
	}

	@Override
	public int getMaxLightHeight(World world) {
		// Always 256 for 1.8 - 1.13.2 (v1_8_R1 - v1_13_R2) (same as world max height)
		// Should be overridden for other versions
		return world.getMaxHeight();
	}

	private int getMinLightSection(World world) {
		return getMinLightHeight(world) >> 4;
	}

	private int getMaxLightSection(World world) {
		return (getMaxLightHeight(world) + 15) >> 4;
	}

	@Override
	public final boolean isValidSectionY(World world, int sectionY) {
		return sectionY >= getMinLightSection(world) && sectionY < getMaxLightSection(world);
	}

	@Override
	public int asSectionMask(int sectionY) {
		return 1 << sectionY;
	}

	@Override
	public BitSet asSectionMask(World world, int sectionY) {
		return BitSet.valueOf(new long[]{asSectionMask(sectionY)});
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

	@Deprecated
	@Override
	public void createLight(World world, int x, int y, int z, int light) {
		createLight(world, x, y, z, LightType.BLOCK, light);
	}

	@Deprecated
	@Override
	public void deleteLight(World world, int x, int y, int z) {
		deleteLight(world, x, y, z, LightType.BLOCK);
	}

	@Override
	public List<ChunkInfo> collectChunks(World world, int blockX, int blockY, int blockZ, int lightLevel
	) {
		return collectChunks(world, blockX, blockY, blockZ, LightType.BLOCK, lightLevel);
	}

	@Override
	public List<ChunkInfo> collectChunks(
			World world, int blockX, int blockY, int blockZ, LightType lightType, int lightLevel
	) {
		List<ChunkInfo> list = new ArrayList<ChunkInfo>();
		Collection<Player> players = null;
		if (lightLevel > 0) {
			for (int dx = -1; dx <= 1; dx++) {
				int lightLevelX = lightLevel - getDeltaLight(blockX & 15, dx);
				if (lightLevelX > 0) {
					for (int dz = -1; dz <= 1; dz++) {
						int lightLevelZ = lightLevelX - getDeltaLight(blockZ & 15, dz);
						if (lightLevelZ > 0) {
							for (int dy = -1; dy <= 1; dy++) {
								if (lightLevelZ > getDeltaLight(blockY & 15, dy)) {
									int sectionY = (blockY >> 4) + dy;
									if (isValidSectionY(world, sectionY)) {
										int chunkX = blockX >> 4;
										int chunkZ = blockZ >> 4;
										ChunkInfo cCoord = new ChunkInfo(
												world,
												chunkX + dx,
												sectionY << 4,
												chunkZ + dz,
												players != null ? players : (players = world.getPlayers()));
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

	private int getDeltaLight(int x, int dx) {
		return (((x ^ ((-dx >> 4) & 15)) + 1) & (-(dx & 1)));
	}

	@Deprecated
	@Override
	public void sendChunkSectionsUpdate(
			World world, int chunkX, int chunkZ, int sectionsMask, Collection<? extends Player> players
	) {
		sendChunkSectionsUpdate(world, chunkX, chunkZ, 0, sectionsMask, players);
	}

	@Override
	public void sendChunkSectionsUpdate(
			World world, int chunkX, int chunkZ,
			int sectionsMaskSky, int sectionsMaskBlock, Collection<? extends Player> players
	) {
		for (Player player : players) {
			sendChunkSectionsUpdate(world, chunkX, chunkZ, sectionsMaskSky, sectionsMaskBlock, player);
		}
	}

	@Override
	public void sendChunkSectionsUpdate(
			World world, int chunkX, int chunkZ,
			BitSet sectionsMaskSky, BitSet sectionsMaskBlock, Collection<? extends Player> players
	) {
		for (Player player : players) {
			sendChunkSectionsUpdate(world, chunkX, chunkZ, sectionsMaskSky, sectionsMaskBlock, player);
		}
	}

	private int toInt(BitSet mask) {
		return mask.isEmpty() ? 0 : (int) mask.toLongArray()[0];
	}

	@Override
	public void sendChunkSectionsUpdate(
			World world, int chunkX, int chunkZ,
			BitSet sectionsMaskSky, BitSet sectionsMaskBlock, Player player
	) {
		sendChunkSectionsUpdate(world, chunkX, chunkZ, toInt(sectionsMaskSky), toInt(sectionsMaskBlock), player);
	}

	@Deprecated
	@Override
	public void sendChunkSectionsUpdate(World world, int chunkX, int chunkZ, int sectionsMask, Player player) {
		sendChunkSectionsUpdate(world, chunkX, chunkZ, 0, sectionsMask, player);
	}

	@Deprecated
	@Override
	public void recalculateLight(World world, int x, int y, int z) {
		recalculateLighting(world, x, y, z, LightType.BLOCK);
	}

	protected abstract void recalculateLighting(World world, int x, int y, int z, LightType lightType);

	protected void recalculateNeighbours(World world, int x, int y, int z, LightType lightType) {
		recalculateLighting(world, x - 1, y, z, lightType);
		recalculateLighting(world, x + 1, y, z, lightType);
		recalculateLighting(world, x, y - 1, z, lightType);
		recalculateLighting(world, x, y + 1, z, lightType);
		recalculateLighting(world, x, y, z - 1, lightType);
		recalculateLighting(world, x, y, z + 1, lightType);
	}

	@Deprecated
	@Override
	public List<ChunkInfo> collectChunks(World world, int x, int y, int z) {
		return collectChunks(world, x, y, z, LightType.BLOCK, 15);
	}

	@Deprecated
	@Override
	public void sendChunkUpdate(World world, int chunkX, int chunkZ, Collection<? extends Player> players) {
		sendChunkSectionsUpdate(world, chunkX, chunkZ, 0, isValidSectionY(world, -1) ? 0x3ffff : 0xffff, players);
	}

	@Deprecated
	@Override
	public void sendChunkUpdate(World world, int chunkX, int chunkZ, Player player) {
		sendChunkSectionsUpdate(world, chunkX, chunkZ, 0, isValidSectionY(world, -1) ? 0x3ffff : 0xffff, player);
	}

	@Deprecated
	@Override
	public void sendChunkUpdate(World world, int chunkX, int y, int chunkZ, Collection<? extends Player> players) {
		int mask = getThreeSectionsMask(world, y);
		if (mask != 0) sendChunkSectionsUpdate(world, chunkX, chunkZ, 0, mask, players);
	}

	@Deprecated
	@Override
	public void sendChunkUpdate(World world, int chunkX, int y, int chunkZ, Player player) {
		int mask = getThreeSectionsMask(world, y);
		if (mask != 0) sendChunkSectionsUpdate(world, chunkX, chunkZ, 0, mask, player);
	}

	private int getThreeSectionsMask(World world, int y) {
		return (isValidSectionY(world, y) ? asSectionMask(y) : 0)
				| (isValidSectionY(world, y - 1) ? asSectionMask(y - 1) : 0)
				| (isValidSectionY(world, y + 1) ? asSectionMask(y + 1) : 0);
	}
}
