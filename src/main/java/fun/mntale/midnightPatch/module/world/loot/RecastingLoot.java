package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;

public class RecastingLoot extends AbstractLootHandler {
    private static final NamespacedKey RECASTING_KEY = NamespacedKey.fromString("midnightpatch:recasting");
    private static final Enchantment RECASTING_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(RECASTING_KEY);
    private static final String[] LOOT_TABLES = {
        "minecraft:chests/underwater_ruin_big",
        "minecraft:chests/underwater_ruin_small",
        "minecraft:chests/buried_treasure",
        "minecraft:chests/shipwreck_treasure",
        "minecraft:chests/shipwreck_supply",
        "minecraft:chests/shipwreck_map"
    };

    @Override
    protected String[] getLootTableKeys() {
        return LOOT_TABLES;
    }

    @Override
    protected ItemStack createLootItem() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.addUnsafeEnchantment(RECASTING_ENCHANT, 1);
        return book;
    }

    @Override
    protected boolean shouldAddLoot(Inventory inv) {
        if (!MidnightPatch.instance.getConfig().getBoolean("enableRecastingLoot", true)) return false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getEnchantments().containsKey(RECASTING_ENCHANT)) {
                return false;
            }
        }
        return true;
    }

} 