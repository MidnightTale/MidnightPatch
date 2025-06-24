package fun.mntale.midnightPatch.module.world.desirepath;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class BiomePathStages {
    private static final Map<Biome, Material[]> BIOME_PATH_STAGES = new ConcurrentHashMap<>();
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
        for (Biome b : RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME)) {
            String name = b.getKey().getKey();
            switch (name) {
                case "taiga", "snowy_taiga", "snowy_plains", "snowy_slopes", "snowy_beach", "grove", "frozen_river",
                     "frozen_ocean" -> BIOME_PATH_STAGES.put(b, taigaStages);
                case "swamp", "mangrove_swamp" -> BIOME_PATH_STAGES.put(b, swampStages);
                case "desert", "badlands", "eroded_badlands", "wooded_badlands", "savanna", "savanna_plateau" ->
                        BIOME_PATH_STAGES.put(b, desertStages);
                case "jungle", "bamboo_jungle", "sparse_jungle" -> BIOME_PATH_STAGES.put(b, jungleStages);
                case "mushroom_fields", "mushroom_field_shore" -> BIOME_PATH_STAGES.put(b, mushroomStages);
                case "nether_wastes" -> BIOME_PATH_STAGES.put(b, netherWastesStages);
                case "soul_sand_valley" -> BIOME_PATH_STAGES.put(b, soulSandValleyStages);
                case "crimson_forest" -> BIOME_PATH_STAGES.put(b, crimsonForestStages);
                case "warped_forest" -> BIOME_PATH_STAGES.put(b, warpedForestStages);
                case "basalt_deltas" -> BIOME_PATH_STAGES.put(b, basaltDeltasStages);
                case "the_end", "end_highlands", "end_midlands", "end_barrens", "small_end_islands" ->
                        BIOME_PATH_STAGES.put(b, endStages);
                default -> BIOME_PATH_STAGES.put(b, plainsStages);
            }
        }
    }
    public static Material[] getStagesForBiome(Biome biome) {
        return BIOME_PATH_STAGES.getOrDefault(biome, BIOME_PATH_STAGES.values().iterator().next());
    }
} 