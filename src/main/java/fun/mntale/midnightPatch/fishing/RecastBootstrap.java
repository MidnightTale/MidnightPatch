package fun.mntale.midnightPatch.fishing;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlotGroup;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;

public class RecastBootstrap implements PluginBootstrap {
    public static final String ENCHANT_KEY = "midnightpatch:recasting";

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
            RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
                event.registry().register(
                    EnchantmentKeys.create(NamespacedKey.fromString(ENCHANT_KEY)),
                    b -> b.description(Component.text("Recasting"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_FISHING))
                        .anvilCost(2)
                        .maxLevel(1)
                        .weight(2)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 0))
                        .activeSlots(EquipmentSlotGroup.HAND)
                );
            })
        );
    }
}
