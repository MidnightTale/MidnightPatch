package fun.mntale.midnightPatch.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlotGroup;

public class MidnightPatchBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
            RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
                // Register Harvesting
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
                // Register Recast
                event.registry().register(
                    EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:recasting")),
                    b -> b.description(Component.text("Recasting"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_FISHING))
                        .anvilCost(2)
                        .maxLevel(1)
                        .weight(2)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 0))
                        .activeSlots(EquipmentSlotGroup.HAND)
                );
                // Register Frostbite
                event.registry().register(
                    EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:frostbite")),
                    b -> b.description(Component.text("Frostbite"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_SHARP_WEAPON))
                        .anvilCost(2)
                        .maxLevel(2)
                        .weight(2)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 0))
                        .activeSlots(EquipmentSlotGroup.HAND)
                        .exclusiveWith(
                            RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.FIRE_ASPECT)
                        )
                );
                // Register Grace
                event.registry().register(
                    EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:grace")),
                    b -> b.description(Component.text("Grace"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_ARMOR))
                        .anvilCost(2)
                        .maxLevel(1)
                        .weight(2)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 0))
                        .activeSlots(EquipmentSlotGroup.ARMOR)
                        .exclusiveWith(
                            RegistrySet.keySet(
                                RegistryKey.ENCHANTMENT,
                                EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:resilience"))
                            )
                        )
                );
                // Register Resilience
                event.registry().register(
                    EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:resilience")),
                    b -> b.description(Component.text("Resilience"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_ARMOR))
                        .anvilCost(2)
                        .maxLevel(1)
                        .weight(2)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 0))
                        .activeSlots(EquipmentSlotGroup.ARMOR)
                        .exclusiveWith(
                            RegistrySet.keySet(
                                RegistryKey.ENCHANTMENT,
                                EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:grace"))
                            )
                        )
                );
                // Register Updraft
                event.registry().register(
                    EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:updraft")),
                    b -> b.description(Component.text("Updraft"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR))
                        .anvilCost(2)
                        .maxLevel(2)
                        .weight(2)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 0))
                        .activeSlots(EquipmentSlotGroup.FEET)
                        .exclusiveWith(
                            RegistrySet.keySet(
                                RegistryKey.ENCHANTMENT,
                                EnchantmentKeys.FEATHER_FALLING,
                                EnchantmentKeys.FROST_WALKER
                            )
                        )
                );
                // Register Undertow
                event.registry().register(
                    EnchantmentKeys.create(NamespacedKey.fromString("midnightpatch:undertow")),
                    b -> b.description(Component.text("Undertow"))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_TRIDENT))
                        .anvilCost(2)
                        .maxLevel(3)
                        .weight(2)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(30, 0))
                        .activeSlots(EquipmentSlotGroup.HAND)
                        .exclusiveWith(
                            RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.RIPTIDE)
                        )
                );
            })
        );
    }
} 