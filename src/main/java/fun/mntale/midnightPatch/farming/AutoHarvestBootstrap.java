package fun.mntale.midnightPatch.farming;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlotGroup;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;

public class AutoHarvestBootstrap implements PluginBootstrap {
    public static final String ENCHANT_KEY = "midnightpatch:harvesting";

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
            RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
                event.registry().register(
                    EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:harvesting")),
                    b -> b.description(Component.text("Harvesting"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HOES))
                        .anvilCost(2)
                        .maxLevel(5)
                        .weight(2)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 0))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 0))
                        .activeSlots(EquipmentSlotGroup.HAND)
                );
            })
        );
    }
} 