package fun.mntale.midnightPatch.farming;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class HarvestingTradeListener implements Listener {
    private static final NamespacedKey HARVESTING_KEY = NamespacedKey.fromString("midnightpatch:harvesting");
    private static final Enchantment HARVESTING_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(HARVESTING_KEY);

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (HARVESTING_ENCHANT == null) return;
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.getProfession() != Villager.Profession.LIBRARIAN) return;

        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
        enchantedBook.addUnsafeEnchantment(HARVESTING_ENCHANT, 1);

        MerchantRecipe recipe = new MerchantRecipe(enchantedBook, 12); // 12 max uses
        recipe.setExperienceReward(true);
        recipe.addIngredient(new ItemStack(Material.EMERALD, 20));
        recipe.addIngredient(new ItemStack(Material.BOOK));

        event.setRecipe(recipe);
    }
} 