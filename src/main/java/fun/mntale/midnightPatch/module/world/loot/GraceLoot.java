package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ThreadLocalRandom;

public class GraceLoot extends AbstractLootHandler {
    private static final NamespacedKey GRACE_KEY = NamespacedKey.fromString("midnightpatch:grace");
    private static final Enchantment GRACE_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(GRACE_KEY);
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
        armor.addUnsafeEnchantment(GRACE_ENCHANT, 1);
        return armor;
    }

    @Override
    protected boolean shouldAddLoot(Inventory inv) {
        if (!MidnightPatch.instance.getConfig().getBoolean("enableGraceLoot", true)) return false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getEnchantments().containsKey(GRACE_ENCHANT)) {
                return false;
            }
        }
        return true;
    }
}