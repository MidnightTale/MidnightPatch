package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GraceLoot extends AbstractLootHandler {
    private static final NamespacedKey GRACE_KEY = NamespacedKey.fromString("midnightpatch:grace");
    private static final Enchantment GRACE_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(GRACE_KEY);

    private static final String[] LOOT_TABLES = {
        "minecraft:chests/end_city_treasure"
    };

    @Override
    protected String[] getLootTableKeys() {
        return LOOT_TABLES;
    }

    @Override
    protected ItemStack createLootItem() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.addUnsafeEnchantment(GRACE_ENCHANT, 1);
        return book;
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