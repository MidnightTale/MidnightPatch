package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;

public class UpdraftLoot extends AbstractLootHandler {
    private static final NamespacedKey UPDRAFT_KEY = NamespacedKey.fromString("midnightpatch:updraft");
    private static final Enchantment UPDRAFT_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(UPDRAFT_KEY);
    private static final String[] LOOT_TABLES = {
        "minecraft:chests/jungle_temple",
        "minecraft:chests/stronghold_library",
        "minecraft:chests/ancient_city",
        "minecraft:chests/woodland_mansion"
    };

    @Override
    protected String[] getLootTableKeys() {
        return LOOT_TABLES;
    }

    @Override
    protected ItemStack createLootItem() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.addUnsafeEnchantment(UPDRAFT_ENCHANT, 1);
        return book;
    }

    @Override
    protected boolean shouldAddLoot(Inventory inv) {
        if (!MidnightPatch.instance.getConfig().getBoolean("enableUpdraftLoot", true)) return false;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getEnchantments().containsKey(UPDRAFT_ENCHANT)) {
                return false;
            }
        }
        return true;
    }

} 