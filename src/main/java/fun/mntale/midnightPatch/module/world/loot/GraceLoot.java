package fun.mntale.midnightPatch.module.world.loot;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ThreadLocalRandom;

public class GraceLoot extends AbstractLootHandler {
    private static final NamespacedKey GRACE_KEY = NamespacedKey.fromString("midnightpatch:grace");
    private static final Material[] ARMOR_TYPES = {
        Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
        Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS
    };
    private static final String[] LOOT_TABLES = {
        "minecraft:chests/end_city_treasure"
    };

    @Override
    protected String[] getLootTableKeys() {
        return LOOT_TABLES;
    }

    @Override
    protected ItemStack createLootItem() {
        Material armorType = ARMOR_TYPES[ThreadLocalRandom.current().nextInt(ARMOR_TYPES.length)];
        ItemStack armor = new ItemStack(armorType);
        Enchantment grace = org.bukkit.Registry.ENCHANTMENT.get(GRACE_KEY);
        if (grace != null) {
            armor.addUnsafeEnchantment(grace, 1);
        }
        return armor;
    }

    @Override
    protected boolean shouldAddLoot(Inventory inv) {
        Enchantment grace = org.bukkit.Registry.ENCHANTMENT.get(GRACE_KEY);
        if (grace == null) return false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType().isItem() && item.getEnchantments().containsKey(grace)) {
                return false;
            }
        }
        return true;
    }
} 