package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;

public class FrostbiteLoot extends AbstractLootHandler {
    private static final NamespacedKey FROSTBITE_KEY = NamespacedKey.fromString("midnightpatch:frostbite");
    private static final Enchantment FROSTBITE_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(FROSTBITE_KEY);
    private static final String[] LOOT_TABLES = {
        "minecraft:chests/ancient_city",
        "minecraft:chests/igloo_chest",
        "minecraft:chests/ice_spikes"
    };

    @Override
    protected String[] getLootTableKeys() {
        return LOOT_TABLES;
    }

    @Override
    protected ItemStack createLootItem() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.addUnsafeEnchantment(FROSTBITE_ENCHANT, 1);
        return book;
    }

    @Override
    protected boolean shouldAddLoot(Inventory inv) {
        if (!MidnightPatch.instance.getConfig().getBoolean("enableFrostbiteLoot", true)) return false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getEnchantments().containsKey(FROSTBITE_ENCHANT)) {
                return false;
            }
        }
        return true;
    }
} 