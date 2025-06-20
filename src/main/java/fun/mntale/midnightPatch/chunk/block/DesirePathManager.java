package fun.mntale.midnightPatch.chunk.block;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.world.ChunkUnloadEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.block.Biome;
import static org.bukkit.block.Biome.*;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

public class DesirePathManager implements Listener {
    // Map: regionKey -> chunkKey -> blockKey -> wear
    private final Map<String, Map<String, Map<String, Integer>>> regionMap = new ConcurrentHashMap<>();
    private final Set<String> dirtyRegions = ConcurrentHashMap.newKeySet();
    private final File dataDir;
    // Biome-based path progressions
    private static final Map<Biome, Material[]> BIOME_PATH_STAGES = new HashMap<>();
    static {
        // Plains, Forest, Meadow
        Material[] plainsStages = { Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.DIRT_PATH, Material.GRAVEL, Material.COBBLESTONE_SLAB };
        // Taiga, Snowy
        Material[] taigaStages = { Material.PODZOL, Material.COARSE_DIRT, Material.GRAVEL, Material.DIRT_PATH, Material.COBBLESTONE_SLAB };
        // Swamp, Mangrove
        Material[] swampStages = { Material.GRASS_BLOCK, Material.MUD, Material.ROOTED_DIRT, Material.DIRT_PATH, Material.PACKED_MUD };
        // Desert, Badlands
        Material[] desertStages = { Material.SAND, Material.RED_SAND, Material.SANDSTONE, Material.SMOOTH_SANDSTONE };
        // Jungle
        Material[] jungleStages = { Material.GRASS_BLOCK, Material.PODZOL, Material.ROOTED_DIRT, Material.DIRT_PATH, Material.MOSS_BLOCK };
        // Mushroom
        Material[] mushroomStages = { Material.MYCELIUM, Material.COARSE_DIRT, Material.DIRT_PATH, Material.GRAVEL };
        // Nether
        Material[] netherWastesStages = { Material.NETHERRACK, Material.GRAVEL, Material.SOUL_SAND, Material.SOUL_SOIL, Material.MAGMA_BLOCK, Material.NETHER_BRICKS };
        Material[] soulSandValleyStages = { Material.SOUL_SAND, Material.SOUL_SOIL, Material.GRAVEL, Material.BASALT, Material.POLISHED_BASALT, Material.MAGMA_BLOCK };
        Material[] crimsonForestStages = { Material.CRIMSON_NYLIUM, Material.NETHERRACK, Material.GRAVEL, Material.CRIMSON_STEM, Material.NETHER_WART_BLOCK, Material.NETHER_BRICKS };
        Material[] warpedForestStages = { Material.WARPED_NYLIUM, Material.NETHERRACK, Material.GRAVEL, Material.WARPED_STEM, Material.WARPED_WART_BLOCK, Material.NETHER_BRICKS };
        Material[] basaltDeltasStages = { Material.BASALT, Material.POLISHED_BASALT, Material.BLACKSTONE, Material.GRAVEL, Material.MAGMA_BLOCK };
        // End
        Material[] endStages = { Material.END_STONE, Material.PURPUR_BLOCK, Material.END_STONE_BRICKS, Material.PURPUR_PILLAR, Material.COBBLESTONE, Material.CHISELED_STONE_BRICKS, Material.OBSIDIAN, Material.CRYING_OBSIDIAN, Material.BLACKSTONE };
        // Default fallback
        Material[] defaultStages = plainsStages;
        // Assign to biomes using Paper registry access
        for (Biome b : RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME)) {
            String name = b.getKey().getKey();
            if (name.equals("plains") || name.equals("forest") || name.equals("birch_forest") || name.equals("dark_forest") || name.equals("flower_forest") || name.equals("meadow")) {
                BIOME_PATH_STAGES.put(b, plainsStages);
            } else if (name.equals("taiga") || name.equals("snowy_taiga") || name.equals("snowy_plains") || name.equals("snowy_slopes") || name.equals("snowy_beach") || name.equals("grove") || name.equals("frozen_river") || name.equals("frozen_ocean")) {
                BIOME_PATH_STAGES.put(b, taigaStages);
            } else if (name.equals("swamp") || name.equals("mangrove_swamp")) {
                BIOME_PATH_STAGES.put(b, swampStages);
            } else if (name.equals("desert") || name.equals("badlands") || name.equals("eroded_badlands") || name.equals("wooded_badlands") || name.equals("savanna") || name.equals("savanna_plateau")) {
                BIOME_PATH_STAGES.put(b, desertStages);
            } else if (name.equals("jungle") || name.equals("bamboo_jungle") || name.equals("sparse_jungle")) {
                BIOME_PATH_STAGES.put(b, jungleStages);
            } else if (name.equals("mushroom_fields") || name.equals("mushroom_field_shore")) {
                BIOME_PATH_STAGES.put(b, mushroomStages);
            } else if (name.equals("nether_wastes")) {
                BIOME_PATH_STAGES.put(b, netherWastesStages);
            } else if (name.equals("soul_sand_valley")) {
                BIOME_PATH_STAGES.put(b, soulSandValleyStages);
            } else if (name.equals("crimson_forest")) {
                BIOME_PATH_STAGES.put(b, crimsonForestStages);
            } else if (name.equals("warped_forest")) {
                BIOME_PATH_STAGES.put(b, warpedForestStages);
            } else if (name.equals("basalt_deltas")) {
                BIOME_PATH_STAGES.put(b, basaltDeltasStages);
            } else if (name.equals("the_end") || name.equals("end_highlands") || name.equals("end_midlands") || name.equals("end_barrens") || name.equals("small_end_islands")) {
                BIOME_PATH_STAGES.put(b, endStages);
            } else {
                BIOME_PATH_STAGES.put(b, defaultStages);
            }
        }
    }
    // How many steps to next stage
    private static final int STAGE_PROGRESS = 256;

    public DesirePathManager() {
        File pluginDir = MidnightPatch.instance.getDataFolder();
        dataDir = new File(pluginDir, "desirepaths");
        if (!dataDir.exists()) dataDir.mkdirs();
    }

    private String getRegionKey(World world, int chunkX, int chunkZ) {
        int regionX = DesirePathRegionIO.getRegionCoord(chunkX);
        int regionZ = DesirePathRegionIO.getRegionCoord(chunkZ);
        return world.getName() + ":" + regionX + "," + regionZ;
    }

    private File getRegionFile(World world, int chunkX, int chunkZ) {
        int regionX = DesirePathRegionIO.getRegionCoord(chunkX);
        int regionZ = DesirePathRegionIO.getRegionCoord(chunkZ);
        File worldDir = new File(dataDir, world.getName());
        if (!worldDir.exists()) worldDir.mkdirs();
        return new File(worldDir, regionX + "_" + regionZ + ".mnt");
    }

    private Map<String, Map<String, Integer>> loadRegion(World world, int chunkX, int chunkZ) {
        String regionKey = getRegionKey(world, chunkX, chunkZ);
        if (!regionMap.containsKey(regionKey)) {
            File file = getRegionFile(world, chunkX, chunkZ);
            Map<String, Map<String, Integer>> regionData = DesirePathRegionIO.loadRegion(file);
            regionMap.put(regionKey, regionData);
        }
        return regionMap.get(regionKey);
    }

    private void saveRegionAsync(World world, int chunkX, int chunkZ) {
        String regionKey = getRegionKey(world, chunkX, chunkZ);
        Map<String, Map<String, Integer>> regionData = regionMap.get(regionKey);
        if (regionData != null) {
            // Deep copy to avoid concurrent modification
            Map<String, Map<String, Integer>> regionDataCopy = new HashMap<>();
            for (Map.Entry<String, Map<String, Integer>> entry : regionData.entrySet()) {
                regionDataCopy.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
            File file = getRegionFile(world, chunkX, chunkZ);
            FoliaScheduler.getAsyncScheduler().runNow(MidnightPatch.instance, (io) -> {
                DesirePathRegionIO.saveRegion(file, regionDataCopy);
            });
            dirtyRegions.remove(regionKey);
        }
    }

    public void saveAllRegions() {
        for (String regionKey : new HashSet<>(dirtyRegions)) {
            String[] parts = regionKey.split(":");
            String worldName = parts[0];
            String[] coords = parts[1].split(",");
            int regionX = Integer.parseInt(coords[0]);
            int regionZ = Integer.parseInt(coords[1]);
            World world = MidnightPatch.instance.getServer().getWorld(worldName);
            if (world != null) {
                saveRegionAsync(world, regionX * DesirePathRegionIO.REGION_SIZE, regionZ * DesirePathRegionIO.REGION_SIZE);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;
        Player player = event.getPlayer();
        final int wearAmount;
        boolean isSprinting = player.isSprinting();
        boolean isSneaking = player.isSneaking();
        @SuppressWarnings("deprecation")
        boolean isJumping = player.getVelocity().getY() > 0.1 && !player.isOnGround();
        boolean isRaining = player.getWorld().hasStorm();
        boolean isThundering = player.getWorld().isThundering();
        int baseWear = 1;
        if (isSprinting) baseWear += 1;
        if (isJumping) baseWear += 2;
        if (isSneaking) baseWear -= 1;
        baseWear = Math.max(1, baseWear);
        if (isThundering) {
            baseWear *= 3;
        } else if (isRaining) {
            baseWear *= 2;
        }
        wearAmount = baseWear;
        // Get the block location below the player
        org.bukkit.Location loc = player.getLocation().subtract(0, 1, 0);
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, task -> {
            Block block = loc.getBlock();
            Material type = block.getType();
            Biome biome = block.getBiome();
            Material[] stages = BIOME_PATH_STAGES.getOrDefault(biome, BIOME_PATH_STAGES.get(PLAINS));
            int stage = 0;
            for (int i = 0; i < stages.length; i++) {
                if (stages[i] == type) {
                    stage = i;
                    break;
                }
            }
            World world = block.getWorld();
            int chunkX = block.getX() >> 4;
            int chunkZ = block.getZ() >> 4;
            String regionKey = getRegionKey(world, chunkX, chunkZ);
            Map<String, Map<String, Integer>> regionData = loadRegion(world, chunkX, chunkZ);
            String chunkKey = DesirePathRegionIO.chunkKey(chunkX, chunkZ);
            Map<String, Integer> chunkMap = regionData.computeIfAbsent(chunkKey, k -> new HashMap<>());
            String blockKey = DesirePathRegionIO.blockKey(block.getX(), block.getY(), block.getZ());
            int wear = chunkMap.getOrDefault(blockKey, 0) + wearAmount;
            int currentStage = wear / STAGE_PROGRESS;
            if (currentStage > stage && currentStage < stages.length) {
                block.setType(stages[currentStage]);
                chunkMap.put(blockKey, currentStage * STAGE_PROGRESS);
                dirtyRegions.add(regionKey);
            } else if (currentStage >= stages.length) {
                block.setType(stages[stages.length - 1]);
                chunkMap.put(blockKey, (stages.length - 1) * STAGE_PROGRESS);
                dirtyRegions.add(regionKey);
            } else {
                chunkMap.put(blockKey, wear);
                dirtyRegions.add(regionKey);
            }
        });
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        saveRegionAsync(world, chunkX, chunkZ);
    }
} 