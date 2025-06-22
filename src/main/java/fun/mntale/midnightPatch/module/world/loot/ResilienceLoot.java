package fun.mntale.midnightPatch.module.world.loot;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ThreadLocalRandom;

public class ResilienceLoot extends AbstractLootHandler {
    private static final NamespacedKey RESILIENCE_KEY = NamespacedKey.fromString("midnightpatch:resilience");
    private static final Material[] ARMOR_TYPES = {
        Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
        Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS
    };
    private static final String[] LOOT_TABLES = {
        "minecraft:chests/ancient_city",
        "minecraft:chests/bastion_treasure",
        "minecraft:chests/woodland_mansion"
    };

    @Override
    protected String[] getLootTableKeys() {
        return LOOT_TABLES;
    }

    @Override
    protected ItemStack createLootItem() {
        Material armorType = ARMOR_TYPES[ThreadLocalRandom.current().nextInt(ARMOR_TYPES.length)];
        ItemStack armor = new ItemStack(armorType);
        Enchantment resilience = org.bukkit.Registry.ENCHANTMENT.get(RESILIENCE_KEY);
        if (resilience != null) {
            armor.addUnsafeEnchantment(resilience, 1);
        }
        return armor;
    }

    @Override
    protected boolean shouldAddLoot(Inventory inv) {
        Enchantment resilience = org.bukkit.Registry.ENCHANTMENT.get(RESILIENCE_KEY);
        if (resilience == null) return false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType().isItem() && item.getEnchantments().containsKey(resilience)) {
                return false;
            }
        }
        return true;
    }
} 