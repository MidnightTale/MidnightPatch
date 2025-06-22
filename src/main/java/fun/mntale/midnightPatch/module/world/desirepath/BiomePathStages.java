package fun.mntale.midnightPatch.module.world.desirepath;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.HashMap;
import java.util.Map;

public class BiomePathStages {
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
    public static Material[] getStagesForBiome(Biome biome) {
        return BIOME_PATH_STAGES.getOrDefault(biome, BIOME_PATH_STAGES.values().iterator().next());
    }
} 