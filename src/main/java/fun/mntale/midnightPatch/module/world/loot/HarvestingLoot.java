package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;

public class HarvestingLoot extends AbstractLootHandler {
    private static final NamespacedKey HARVESTING_KEY = NamespacedKey.fromString("midnightpatch:harvesting");
    private static final Enchantment HARVESTING_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(HARVESTING_KEY);
    private static final String[] LOOT_TABLES = {
        "minecraft:chests/abandoned_mineshaft",
        "minecraft:chests/woodland_mansion",
        "minecraft:chests/stronghold_library"
    };

    @Override
    protected String[] getLootTableKeys() {
        return LOOT_TABLES;
    }

    @Override
    protected ItemStack createLootItem() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        int level = ThreadLocalRandom.current().nextInt(1, 4); // 1 to 3 inclusive
        book.addUnsafeEnchantment(HARVESTING_ENCHANT, level);
        return book;
    }

    @Override
    protected boolean shouldAddLoot(Inventory inv) {
        if (!MidnightPatch.instance.getConfig().getBoolean("enableHarvestingLoot", true)) return false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getEnchantments().containsKey(HARVESTING_ENCHANT)) {
                return false;
            }
        }
        return true;
    }

} 