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
package ru.beykerykt.lightapi.request;

import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.beykerykt.lightapi.chunks.ChunkInfo;
import ru.beykerykt.lightapi.chunks.ChunkLocation;
import ru.beykerykt.lightapi.chunks.ChunkUpdateInfo;
import ru.beykerykt.lightapi.server.ServerModManager;
import ru.beykerykt.lightapi.server.nms.INMSHandler;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.utils.Debug;

import java.util.*;
import java.util.concurrent.*;

public class RequestSteamMachine implements Runnable {

	private boolean isStarted;
	private Queue<Runnable> REQUEST_QUEUE = new ConcurrentLinkedQueue<Runnable>();
	private int maxIterationsPerTick;
	private Map<ChunkLocation, ChunkUpdateInfo> chunksToUpdate = new HashMap<ChunkLocation, ChunkUpdateInfo>();

	// THREADS
	private ScheduledFuture<?> sch;
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	public void start(int ticks, int maxIterationsPerTick) {
		if (!isStarted) {
			this.maxIterationsPerTick = maxIterationsPerTick;
			sch = executor.scheduleWithFixedDelay(this, 0, 50 * ticks, TimeUnit.MILLISECONDS);
			isStarted = true;
		}
	}

	public void shutdown() {
		if (isStarted) {
			REQUEST_QUEUE.clear();
			maxIterationsPerTick = 0;
			sch.cancel(false);
			isStarted = false;
		}
	}

	@SuppressWarnings("unused")
	public boolean isStarted() {
		return isStarted;
	}

	@SuppressWarnings("UnusedReturnValue")
	public boolean addToQueue(Runnable request) {
		if (request != null) {
			REQUEST_QUEUE.add(request);
			return true;
		}
		return false;
	}

	public void addChunkToUpdate(final ChunkInfo info, final LightType lightType, Collection<? extends Player> receivers) {
		int SectionY = info.getChunkY();
		World world = info.getWorld();
		INMSHandler nmsHandler = ServerModManager.getNMSHandler();
		if (nmsHandler.isValidSectionY(world, SectionY)) {
			final ChunkLocation chunk = new ChunkLocation(info.getWorld(), info.getChunkX(), info.getChunkZ());
			final BitSet sectionYMask = nmsHandler.asSectionMask(world, SectionY);
			final Collection<Player> players = new ArrayList<Player>(
					receivers != null ? receivers : info.getReceivers()
			);
			addToQueue(new Runnable() {
				@Override
				public void run() {
					ChunkUpdateInfo chunkUpdateInfo = chunksToUpdate.get(chunk);
					if (chunkUpdateInfo == null) {
						chunksToUpdate.put(chunk, chunkUpdateInfo = new ChunkUpdateInfo());
					}
					chunkUpdateInfo.add(lightType, sectionYMask, players);
				}
			});
		}
	}

	@Override
	public void run() {
		try {
			boolean debug = Debug.isEnabled();
			int totalSends = 0;
			int totalSections = 0;
			Set<Player> usedPlayers = null;
			int iterationsCount = 0;
			if (debug) {
				usedPlayers = new HashSet<Player>();
			}

			Runnable request;
			while (iterationsCount < maxIterationsPerTick && (request = REQUEST_QUEUE.poll()) != null) {
				request.run();
				iterationsCount++;
			}

			INMSHandler nmsHandler = ServerModManager.getNMSHandler();

			for (Map.Entry<ChunkLocation, ChunkUpdateInfo> item : chunksToUpdate.entrySet()) {
				ChunkLocation chunk = item.getKey();
				ChunkUpdateInfo chunkUpdateInfo = item.getValue();
				BitSet sectionMaskSky = chunkUpdateInfo.getSectionMaskSky();
				BitSet sectionMaskBlock = chunkUpdateInfo.getSectionMaskBlock();
				Collection<? extends Player> players = nmsHandler.filterVisiblePlayers(
						chunk.getWorld(), chunk.getX(), chunk.getZ(), chunkUpdateInfo.getPlayers());
				nmsHandler.sendChunkSectionsUpdate(chunk.getWorld(), chunk.getX(), chunk.getZ(),
						sectionMaskSky, sectionMaskBlock, players);
				if (debug) {
					totalSends += players.size();
					BitSet clone = (BitSet) sectionMaskSky.clone();
					clone.or(sectionMaskBlock);
					totalSections += clone.cardinality();
					usedPlayers.addAll(players);
				}
			}

			if (debug && (iterationsCount > 0 || chunksToUpdate.size() > 0)) {
				Debug.print("requests %d/%d, chunks = %d, total sends = %d, players = %d, total sections = %d",
						iterationsCount,
						REQUEST_QUEUE.size(),
						chunksToUpdate.size(),
						totalSends,
						usedPlayers.size(),
						totalSections
				);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			chunksToUpdate.clear();
		}
	}
}
