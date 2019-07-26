/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 The ImplexDevOne Project
 * Copyright (c) 2019 Vladimir Mikhailov <beykerykt@gmail.com>
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
package ru.beykerykt.lightapi.server.nms.craftbukkit;

import net.minecraft.server.v1_14_R1.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.beykerykt.lightapi.server.nms.NmsHandlerBase;
import ru.beykerykt.lightapi.utils.Debug;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

public class CraftBukkit_v1_14_R1 extends NmsHandlerBase {

	private boolean reflectionInited = false;
	private Field lightEngine_ThreadedMailbox;
	private Field threadedMailbox_State;
	private Method threadedMailbox_DoLoopStep;

	@Override
	public void createLight(World world, int x, int y, int z, int light) {
		setRawLightLevel(world, LightType.BLOCK, x, y, z, light);
		recalculateLighting(world, LightType.BLOCK, x, y, z);
	}

	@Override
	public void deleteLight(World world, int x, int y, int z) {
		setRawLightLevel(world, LightType.BLOCK, x, y, z, 0);
		recalculateLighting(world, LightType.BLOCK, x, y, z);
	}

	@SuppressWarnings("SameParameterValue")
	private void setRawLightLevel(World world, final LightType type, int blockX, int blockY, int blockZ, int lightlevel) {
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		final BlockPosition position = new BlockPosition(blockX, blockY, blockZ);
		final LightEngineThreaded lightEngine = worldServer.getChunkProvider().getLightEngine();

		final int finalLightLevel = lightlevel < 0 ? 0 : lightlevel > 15 ? 15 : lightlevel;
		executeSync(lightEngine, new Runnable() {
			@Override
			public void run() {
				if (type == LightType.BLOCK) {
					LightEngineBlock leb = (LightEngineBlock) lightEngine.a(EnumSkyBlock.BLOCK);
					if (finalLightLevel == 0) {
						leb.a(position);
					} else if (leb.a(SectionPosition.a(position)) != null) {
						leb.a(position, finalLightLevel);
					}
				} else {
					LightEngineSky les = (LightEngineSky) lightEngine.a(EnumSkyBlock.SKY);
					if (finalLightLevel == 0) {
						les.a(position);
					} else {
						les.a(position, finalLightLevel);
					}
				}
			}
		});
	}

	@Deprecated
	@Override
	public void recalculateLight(World world, int x, int y, int z) {
		recalculateLighting(world, LightType.BLOCK, x, y, z);
	}

	@SuppressWarnings({"SameParameterValue", "unused"})
	private void recalculateLighting(World world, final LightType type, int blockX, int blockY, int blockZ) {
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		final LightEngineThreaded lightEngine = worldServer.getChunkProvider().getLightEngine();

		// Do not recalculate if no changes!
		if (!lightEngine.a()) {
			return;
		}

		executeSync(lightEngine, new Runnable() {
			@Override
			public void run() {
				if (type == LightType.BLOCK) {
					LightEngineBlock leb = (LightEngineBlock) lightEngine.a(EnumSkyBlock.BLOCK);
					leb.a(Integer.MAX_VALUE, true, true);
				} else {
					LightEngineSky les = (LightEngineSky) lightEngine.a(EnumSkyBlock.SKY);
					les.a(Integer.MAX_VALUE, true, true);
				}
			}
		});
	}

	@Override
	public void sendChunkSectionsUpdate(World world, int chunkX, int chunkZ, int sectionsMask, Player player) {
		Chunk chunk = ((CraftWorld) world).getHandle().getChunkAt(chunkX, chunkZ);
		// https://wiki.vg/index.php?title=Pre-release_protocol&oldid=14804#Update_Light
		// https://github.com/flori-schwa/VarLight/blob/b9349499f9c9fb995c320f95eae9698dd85aad5c/v1_14_R1/src/me/florian/varlight/nms/v1_14_R1/NmsAdapter_1_14_R1.java#L451
		//
		// Two last argument is bit-mask what chunk sections to update. Mask containing
		// 18 bits, with the lowest bit corresponding to chunk section -1 (in the void,
		// y=-16 to y=-1) and the highest bit for chunk section 16 (above the world,
		// y=256 to y=271).
		PacketPlayOutLightUpdate packet = new PacketPlayOutLightUpdate(chunk.getPos(), chunk.e(), 0, sectionsMask);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public boolean isValidSectionY(int sectionY) {
		return sectionY >= -1 && sectionY <= 16;
	}

	@Override
	public int asSectionMask(int sectionY) {
		return 1 << sectionY + 1;
	}

	@Override
	protected int getViewDistance(Player player) {
		return player.getClientViewDistance();
	}

	@SuppressWarnings({"StatementWithEmptyBody", "unchecked"})
	private void executeSync(LightEngineThreaded lightEngine, Runnable task) {
		try {
			if (!reflectionInited) {
				threadedMailbox_DoLoopStep = ThreadedMailbox.class.getDeclaredMethod("f");
				threadedMailbox_DoLoopStep.setAccessible(true);
				threadedMailbox_State = ThreadedMailbox.class.getDeclaredField("c");
				threadedMailbox_State.setAccessible(true);
				lightEngine_ThreadedMailbox = LightEngineThreaded.class.getDeclaredField("b");
				lightEngine_ThreadedMailbox.setAccessible(true);
				reflectionInited = true;
			}

			// ##### STEP 1: Pause light engine mailbox to process its tasks. #####
			ThreadedMailbox<Runnable> threadedMailbox = (ThreadedMailbox<Runnable>) lightEngine_ThreadedMailbox
					.get(lightEngine);
			// State flags bit mask:
			// 0x0001 - Closing flag (ThreadedMailbox is closing if non zero).
			// 0x0002 - Busy flag (ThreadedMailbox performs a task from queue if non zero).
			AtomicInteger stateFlags = (AtomicInteger) threadedMailbox_State.get(threadedMailbox);
			int flags; // to hold values from stateFlags
			long timeToWait = -1;
			// Trying to set bit 1 in state bit mask when it is not set yet.
			// This will break the loop in other thread where light engine mailbox processes the taks.
			while (!stateFlags.compareAndSet(flags = stateFlags.get() & ~2, flags | 2)) {
				if ((flags & 1) != 0) {
					// ThreadedMailbox is closing. The light engine mailbox may also stop processing tasks.
					// The light engine mailbox can be close due to server shutdown or unloading (closing) the world.
					// I am not sure is it unsafe to process our tasks while the world is closing is closing,
					// but will try it (one can throw exception here if it crashes the server).
					if (timeToWait == -1) {
						// Try to wait 3 seconds until light engine mailbox is busy.
						timeToWait = System.currentTimeMillis() + 3 * 1000;
						Debug.print("ThreadedMailbox is closing. Will wait...");
					} else if (System.currentTimeMillis() >= timeToWait) {
						throw new RuntimeException("Failed to enter critical section while ThreadedMailbox is closing");
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException ignored) {
					}
				}
			}
			try {
				// ##### STEP 2: Safely running the task while the mailbox process is stopped. #####
				task.run();
			} finally {
				// STEP 3: ##### Continue light engine mailbox to process its tasks. #####
				// Firstly: Clearing busy flag to allow ThreadedMailbox to use it for running light engine tasks.
				while (!stateFlags.compareAndSet(flags = stateFlags.get(), flags & ~2)) ;
				// Secondly: IMPORTANT! The main loop of ThreadedMailbox was broken. Not completed tasks may still be
				// in the queue. Therefore, it is important to start the loop again to process tasks from the queue.
				// Otherwise, the main server thread may be frozen due to tasks stuck in the queue.
				threadedMailbox_DoLoopStep.invoke(threadedMailbox);
			}
		} catch (IllegalAccessException e) {
			throw new UnsupportedOperationException("Something went wrong: access denied", e);
		} catch (NoSuchFieldException e) {
			throw new UnsupportedOperationException("Something went wrong: no such field", e);
		} catch (NoSuchMethodException e) {
			throw new UnsupportedOperationException("Something went wrong: no such method", e);
		} catch (InvocationTargetException e) {
			throw new UnsupportedOperationException("Something went wrong: invocation target", e);
		}
	}
}
