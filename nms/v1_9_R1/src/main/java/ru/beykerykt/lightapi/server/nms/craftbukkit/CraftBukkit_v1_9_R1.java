/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Vladimir Mikhailov <beykerykt@gmail.com>
 * Copyright (c) 2020 Qveshn
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
package ru.beykerykt.lightapi.server.nms.craftbukkit;

import net.minecraft.server.v1_9_R1.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.server.nms.NmsHandlerBase;

public class CraftBukkit_v1_9_R1 extends NmsHandlerBase {

	@Override
	public void createLight(World world, int x, int y, int z, LightType lightType, int light) {
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		worldServer.a(lightType == LightType.SKY ? EnumSkyBlock.SKY : EnumSkyBlock.BLOCK,
				new BlockPosition(x, y, z), light);
		recalculateNeighbours(world, x, y, z, lightType);
	}

	@Override
	public void deleteLight(World world, int x, int y, int z, LightType lightType) {
		recalculateLighting(world, x, y, z, lightType);
	}

	@Override
	protected void recalculateLighting(World world, int x, int y, int z, LightType lightType) {
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		BlockPosition position = new BlockPosition(x, y, z);
		worldServer.c(lightType == LightType.SKY ? EnumSkyBlock.SKY : EnumSkyBlock.BLOCK, position);
	}

	@Override
	public void sendChunkSectionsUpdate(
			World world, int chunkX, int chunkZ, int sectionsMaskSky, int sectionsMaskBlock, Player player
	) {
		int sectionsMask = sectionsMaskSky | sectionsMaskBlock;
		Chunk chunk = ((CraftWorld) world).getHandle().getChunkAt(chunkX, chunkZ);
		// The last argument is bit-mask what chunk sections to update. Mask containing
		// 16 bits, with the lowest bit corresponding to chunk section 0 (y=0 to y=15)
		// and the highest bit for chunk section 15 (y=240 to 255).
		PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(chunk, false, sectionsMask);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public boolean isSupported(World world, LightType lightType) {
		if (!(world instanceof CraftWorld)) {
			return false;
		}
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		if (lightType == LightType.SKY) {
			return !worldServer.worldProvider.m();
		} else {
			return true;
		}
	}
}
