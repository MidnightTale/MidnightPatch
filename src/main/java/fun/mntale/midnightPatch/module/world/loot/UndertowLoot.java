package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ThreadLocalRandom;

public class UndertowLoot extends AbstractLootHandler {
    private static final NamespacedKey UNDERTOW_KEY = NamespacedKey.fromString("midnightpatch:undertow");
    private static final Enchantment UNDERTOW_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(UNDERTOW_KEY);
    private static final String[] LOOT_TABLES = {
        "minecraft:chests/buried_treasure",
        "minecraft:chests/shipwreck_treasure",
        "minecraft:chests/shipwreck_supply",
        "minecraft:chests/shipwreck_map",
        "minecraft:chests/underwater_ruin_big",
        "minecraft:chests/underwater_ruin_small"
    };

    @Override
    protected String[] getLootTableKeys() {
        return LOOT_TABLES;
    }

    @Override
    protected ItemStack createLootItem() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        int level = ThreadLocalRandom.current().nextInt(1, 4); // 1 to 3 inclusive
        book.addUnsafeEnchantment(UNDERTOW_ENCHANT, level);
        return book;
    }

    @Override
    protected boolean shouldAddLoot(Inventory inv) {
        if (!MidnightPatch.instance.getConfig().getBoolean("enableUndertowLoot", true)) return false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getEnchantments().containsKey(UNDERTOW_ENCHANT)) {
                return false;
            }
        }
        return true;
    }
} 