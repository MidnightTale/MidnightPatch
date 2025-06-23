package fun.mntale.midnightPatch.module.world.desirepath;

import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.command.ToggleDesirePathCommand;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.block.Biome;
import org.bukkit.Location;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.block.Container;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DesirePathListener implements Listener {
    private static final int STAGE_PROGRESS = 256;
    private final DesirePathDataManager dataManager = new DesirePathDataManager();

    public void saveAllRegions() {
        for (String regionKey : dataManager.getDirtyRegions()) {
            String[] parts = regionKey.split(":");
            String worldName = parts[0];
            String[] coords = parts[1].split(",");
            int regionX = Integer.parseInt(coords[0]);
            int regionZ = Integer.parseInt(coords[1]);
            World world = MidnightPatch.instance.getServer().getWorld(worldName);
            if (world != null) {
                dataManager.saveRegionAsync(world, regionX * DesirePathRegionIO.REGION_SIZE, regionZ * DesirePathRegionIO.REGION_SIZE);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!ToggleDesirePathCommand.isDesirePathEnabled(player)) return;
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;
        int wearAmount = DesirePathWearCalculator.calculateWear(player);
        Location loc = player.getLocation().subtract(0, 1, 0);
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, task -> {
            Block block = loc.getBlock();
            Material type = block.getType();
            Biome biome = block.getBiome();
            Material[] stages = BiomePathStages.getStagesForBiome(biome);
            int stage = 0;
            for (int i = 0; i < stages.length; i++) {
                if (stages[i] == type) {
                    stage = i;
                    break;
                }
            }
            // Only proceed if the current block is in the stage list
            boolean isValidStageBlock = false;
            for (Material m : stages) {
                if (m == type) {
                    isValidStageBlock = true;
                    break;
                }
            }
            // Extra safety: skip containers (tile entities)
            if (!isValidStageBlock || block.getState() instanceof Container) {
                return;
            }
            World world = block.getWorld();
            int chunkX = block.getX() >> 4;
            int chunkZ = block.getZ() >> 4;
            String regionKey = dataManager.getRegionKey(world, chunkX, chunkZ);
            Map<String, Map<String, Integer>> regionData = dataManager.loadRegion(world, chunkX, chunkZ);
            String chunkKey = DesirePathRegionIO.chunkKey(chunkX, chunkZ);
            Map<String, Integer> chunkMap = regionData.computeIfAbsent(chunkKey, k -> new HashMap<>());
            String blockKey = DesirePathRegionIO.blockKey(block.getX(), block.getY(), block.getZ());
            int wear = chunkMap.getOrDefault(blockKey, 0) + wearAmount;
            int currentStage = wear / STAGE_PROGRESS;
            if (currentStage > stage && currentStage < stages.length) {
                block.setType(stages[currentStage]);
                chunkMap.put(blockKey, currentStage * STAGE_PROGRESS);
                dataManager.markDirty(regionKey);
            } else if (currentStage >= stages.length) {
                block.setType(stages[stages.length - 1]);
                chunkMap.put(blockKey, (stages.length - 1) * STAGE_PROGRESS);
                dataManager.markDirty(regionKey);
            } else {
                chunkMap.put(blockKey, wear);
                dataManager.markDirty(regionKey);
            }
        });
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        dataManager.saveRegionAsync(world, chunkX, chunkZ);
    }

    private void removeWearData(Block block) {
        World world = block.getWorld();
        int chunkX = block.getX() >> 4;
        int chunkZ = block.getZ() >> 4;
        String regionKey = dataManager.getRegionKey(world, chunkX, chunkZ);
        Map<String, Map<String, Integer>> regionData = dataManager.loadRegion(world, chunkX, chunkZ);
        String chunkKey = DesirePathRegionIO.chunkKey(chunkX, chunkZ);
        Map<String, Integer> chunkMap = regionData.get(chunkKey);
        if (chunkMap != null) {
            String blockKey = DesirePathRegionIO.blockKey(block.getX(), block.getY(), block.getZ());
            if (chunkMap.remove(blockKey) != null) {
                dataManager.markDirty(regionKey);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        removeWearData(event.getBlock());
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            removeWearData(block);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            removeWearData(block);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            removeWearData(block);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            removeWearData(block);
        }
    }
} 