package fun.mntale.midnightPatch.module.entity.player.task.effect;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public class BlockBreakingUtil {
    
    public static int estimateBreakTime(Player player, Block block) {
        float hardness = getBlockHardness(block);
        ItemStack tool = player.getInventory().getItemInMainHand();
        float toolSpeed = getToolSpeed(tool, block, player);
        boolean canHarvest = canHarvestBlock(tool, block);

        double damage = toolSpeed / hardness;
        if (canHarvest) {
            damage /= 30.0;
        } else {
            damage /= 100.0;
        }

        if (damage >= 1.0) return 1;
        int ticks = (int) Math.ceil(1.0 / damage);
        return Math.max(ticks, 1);
    }

    private static float getBlockHardness(Block block) {
        return block.getType().getHardness();
    }

    @SuppressWarnings("deprecation")
    private static float getToolSpeed(ItemStack tool, Block block, Player player) {
        float toolSpeed = 1.0f;
        String toolName = tool != null ? tool.getType().name() : "";
        boolean isBestTool = false;

        if (isPickaxeBlock(block)) {
            isBestTool = toolName.contains("PICKAXE");
            toolSpeed = getPickaxeSpeed(toolName);
        } else if (isShovelBlock(block)) {
            isBestTool = toolName.contains("SHOVEL");
            toolSpeed = getShovelSpeed(toolName);
        } else if (isAxeBlock(block)) {
            isBestTool = toolName.contains("AXE");
            toolSpeed = getAxeSpeed(toolName);
        } else {
            toolSpeed = 1.0f;
        }

        if (!isBestTool) toolSpeed = 1.0f;

        int efficiency = tool != null ? tool.getEnchantmentLevel(Enchantment.EFFICIENCY) : 0;
        if (isBestTool && efficiency > 0) {
            toolSpeed += (efficiency * efficiency + 1);
        }

        int haste = 0;
        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            haste = player.getPotionEffect(PotionEffectType.HASTE).getAmplifier() + 1;
        }
        toolSpeed *= (1 + 0.2 * haste);

        int fatigue = 0;
        if (player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
            fatigue = player.getPotionEffect(PotionEffectType.MINING_FATIGUE).getAmplifier() + 1;
        }
        if (fatigue > 0) {
            toolSpeed *= Math.pow(0.3, Math.min(fatigue, 4));
        }

        if (player.isInWater() && !player.hasPotionEffect(PotionEffectType.WATER_BREATHING)) {
            boolean hasAquaAffinity = tool != null && tool.containsEnchantment(Enchantment.AQUA_AFFINITY);
            if (!hasAquaAffinity) toolSpeed *= 0.2;
        }
        if (!player.isOnGround()) {
            toolSpeed *= 0.2;
        }

        return toolSpeed;
    }

    private static boolean isPickaxeBlock(Block block) {
        switch (block.getType()) {
            case STONE, COBBLESTONE, OBSIDIAN, COAL_ORE, IRON_ORE, GOLD_ORE, DIAMOND_ORE, EMERALD_ORE, REDSTONE_ORE, LAPIS_ORE, NETHERITE_BLOCK, DEEPSLATE, ANCIENT_DEBRIS:
                return true;
            default:
                return false;
        }
    }

    private static boolean isShovelBlock(Block block) {
        switch (block.getType()) {
            case DIRT, GRASS_BLOCK, SAND, GRAVEL, CLAY, SOUL_SAND, SOUL_SOIL, SNOW, SNOW_BLOCK, PODZOL, COARSE_DIRT, ROOTED_DIRT:
                return true;
            default:
                return false;
        }
    }

    private static boolean isAxeBlock(Block block) {
        switch (block.getType()) {
            case OAK_LOG, BIRCH_LOG, SPRUCE_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, MANGROVE_LOG, CHERRY_LOG, CRIMSON_STEM, WARPED_STEM:
            case STRIPPED_OAK_LOG, STRIPPED_BIRCH_LOG, STRIPPED_SPRUCE_LOG, STRIPPED_JUNGLE_LOG, STRIPPED_ACACIA_LOG, STRIPPED_DARK_OAK_LOG, STRIPPED_MANGROVE_LOG, STRIPPED_CHERRY_LOG, STRIPPED_CRIMSON_STEM, STRIPPED_WARPED_STEM:
            case OAK_WOOD, BIRCH_WOOD, SPRUCE_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD, MANGROVE_WOOD, CHERRY_WOOD, CRIMSON_HYPHAE, WARPED_HYPHAE:
            case STRIPPED_OAK_WOOD, STRIPPED_BIRCH_WOOD, STRIPPED_SPRUCE_WOOD, STRIPPED_JUNGLE_WOOD, STRIPPED_ACACIA_WOOD, STRIPPED_DARK_OAK_WOOD, STRIPPED_MANGROVE_WOOD, STRIPPED_CHERRY_WOOD, STRIPPED_CRIMSON_HYPHAE, STRIPPED_WARPED_HYPHAE:
                return true;
            default:
                return false;
        }
    }

    private static float getPickaxeSpeed(String toolName) {
        if (toolName.contains("WOODEN")) return 2f;
        if (toolName.contains("STONE")) return 4f;
        if (toolName.contains("IRON")) return 6f;
        if (toolName.contains("DIAMOND")) return 8f;
        if (toolName.contains("NETHERITE")) return 9f;
        if (toolName.contains("GOLDEN")) return 12f;
        return 1f;
    }
    
    private static float getShovelSpeed(String toolName) {
        if (toolName.contains("WOODEN")) return 2f;
        if (toolName.contains("STONE")) return 4f;
        if (toolName.contains("IRON")) return 6f;
        if (toolName.contains("DIAMOND")) return 8f;
        if (toolName.contains("NETHERITE")) return 9f;
        if (toolName.contains("GOLDEN")) return 12f;
        return 1f;
    }
    
    private static float getAxeSpeed(String toolName) {
        if (toolName.contains("WOODEN")) return 2f;
        if (toolName.contains("STONE")) return 4f;
        if (toolName.contains("IRON")) return 6f;
        if (toolName.contains("DIAMOND")) return 8f;
        if (toolName.contains("NETHERITE")) return 9f;
        if (toolName.contains("GOLDEN")) return 12f;
        return 1f;
    }

    private static boolean canHarvestBlock(ItemStack tool, Block block) {
        if (tool == null) return false;
        String toolName = tool.getType().name();
        switch (block.getType()) {
            case STONE, COBBLESTONE, OBSIDIAN:
                return toolName.contains("PICKAXE");
            case DIRT, GRASS_BLOCK, SAND, GRAVEL:
                return toolName.contains("SHOVEL");
            case OAK_LOG, BIRCH_LOG, SPRUCE_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG:
                return toolName.contains("AXE");
            default:
                return false;
        }
    }
} 