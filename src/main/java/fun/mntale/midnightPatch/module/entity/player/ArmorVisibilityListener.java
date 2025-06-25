package fun.mntale.midnightPatch.module.entity.player;

import fun.mntale.midnightPatch.command.ToggleArmorVisibilityCommand;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ArmorVisibilityListener implements Listener {

    /**
     * Handles player join events. Applies armor visibility settings.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FoliaScheduler.getEntityScheduler().run(player, fun.mntale.midnightPatch.MidnightPatch.instance, (task) -> {
            updateArmorVisibility(player);
        }, null);
    }

    /**
     * Handles player quit events. Restores original armor and cleans up.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Restore original armor before player quits
        updateArmorVisibility(player);
    }

    /**
     * Handles armor change events. Updates armor visibility when armor is equipped or removed.
     */
    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        FoliaScheduler.getEntityScheduler().run(player, fun.mntale.midnightPatch.MidnightPatch.instance, (task) -> {
            updateArmorVisibility(player);
        }, null);
    }

    /**
     * Handles inventory clicks to restore item models when items are taken out of armor slots.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if clicking from armor slots (slots 5-8 in player inventory)
        int clickedSlot = event.getRawSlot();
        if (clickedSlot >= 5 && clickedSlot <= 8) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                // Restore the item model when taking it out of armor slot
                ItemStack restoredItem = restoreVisibleItem(clickedItem);
                event.setCurrentItem(restoredItem);
            }
        }
    }

    /**
     * Handles player drop events to restore item models when items are dropped.
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (droppedItem != null && droppedItem.getType() != Material.AIR) {
            // Restore the item model when dropping
            ItemStack restoredItem = restoreVisibleItem(droppedItem);
            event.getItemDrop().setItemStack(restoredItem);
        }
    }

    /**
     * Handles player pickup events to restore item models when items are picked up.
     */
    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        ItemStack pickedUpItem = event.getItem().getItemStack();
        if (pickedUpItem != null && pickedUpItem.getType() != Material.AIR) {
            // Restore the item model when picking up
            ItemStack restoredItem = restoreVisibleItem(pickedUpItem);
            event.getItem().setItemStack(restoredItem);
        }
    }

    /**
     * Handles player death events to restore item models when items drop on death.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Check if armor visibility is disabled for this player
        if (ToggleArmorVisibilityCommand.isArmorVisibilityEnabled(player)) return;
        
        // Restore models for all dropped items
        for (ItemStack droppedItem : event.getDrops()) {
            if (droppedItem != null && droppedItem.getType() != Material.AIR) {
                ItemStack restoredItem = restoreVisibleItem(droppedItem);
                // Replace the item in the drops list
                int index = event.getDrops().indexOf(droppedItem);
                if (index != -1) {
                    event.getDrops().set(index, restoredItem);
                }
            }
        }
    }

    /**
     * Updates armor visibility for a player based on their settings.
     * If armor visibility is disabled, armor pieces are made invisible using the item model API.
     */
    public void updateArmorVisibility(Player player) {
        ItemStack[] currentArmor = player.getInventory().getArmorContents();
        
        if (!ToggleArmorVisibilityCommand.isArmorVisibilityEnabled(player)) {
            // Make armor invisible
            ItemStack[] invisibleArmor = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                ItemStack armorPiece = currentArmor[i];
                if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                    invisibleArmor[i] = createInvisibleItem(armorPiece);
                } else {
                    invisibleArmor[i] = armorPiece;
                }
            }
            player.getInventory().setArmorContents(invisibleArmor);
        } else {
            // Restore armor by removing custom item model
            ItemStack[] restoredArmor = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                ItemStack armorPiece = currentArmor[i];
                if (armorPiece != null && armorPiece.getType() != Material.AIR) {
                    restoredArmor[i] = restoreVisibleItem(armorPiece);
                } else {
                    restoredArmor[i] = armorPiece;
                }
            }
            player.getInventory().setArmorContents(restoredArmor);
        }
    }

    /**
     * Creates a button version of an item using the item model API.
     * The item retains its properties but appears as a button based on armor type.
     */
    private ItemStack createInvisibleItem(ItemStack originalItem) {
        ItemStack buttonItem = originalItem.clone();
        ItemMeta meta = buttonItem.getItemMeta();
        if (meta != null) {            
            // Choose button model based on armor type
            String buttonModel = getButtonModelForArmor(originalItem.getType());
            NamespacedKey modelKey = NamespacedKey.fromString(buttonModel);
            meta.setItemModel(modelKey);
            
            buttonItem.setItemMeta(meta);
        }
        return buttonItem;
    }

    /**
     * Restores a visible version of an item by setting the item model to match the item's material.
     * The item retains its original properties and appearance.
     */
    private ItemStack restoreVisibleItem(ItemStack item) {
        ItemStack restoredItem = item.clone();
        ItemMeta meta = restoredItem.getItemMeta();
        if (meta != null) {
            meta.displayName(null); // Remove custom display name
            // Set item model to match the item's material
            NamespacedKey materialKey = new NamespacedKey("minecraft", item.getType().name().toLowerCase());
            meta.setItemModel(materialKey);
            restoredItem.setItemMeta(meta);
        }
        return restoredItem;
    }

    /**
     * Gets the appropriate button model based on the armor type.
     */
    private String getButtonModelForArmor(Material armorType) {
        return switch (armorType) {
            // Helmets
            case NETHERITE_HELMET -> "minecraft:blackstone_button";
            case DIAMOND_HELMET -> "minecraft:warped_button";
            case IRON_HELMET -> "minecraft:pale_button";
            case GOLDEN_HELMET -> "minecraft:bamboo_button";
            case CHAINMAIL_HELMET -> "minecraft:stone_button";
            case LEATHER_HELMET -> "minecraft:spruce_button";
            
            // Chestplates
            case NETHERITE_CHESTPLATE -> "minecraft:blackstone_button";
            case DIAMOND_CHESTPLATE -> "minecraft:warped_button";
            case IRON_CHESTPLATE -> "minecraft:pale_button";
            case GOLDEN_CHESTPLATE -> "minecraft:bamboo_button";
            case CHAINMAIL_CHESTPLATE -> "minecraft:stone_button";
            case LEATHER_CHESTPLATE -> "minecraft:spruce_button";
            
            // Leggings
            case NETHERITE_LEGGINGS -> "minecraft:blackstone_button";
            case DIAMOND_LEGGINGS -> "minecraft:warped_button";
            case IRON_LEGGINGS -> "minecraft:pale_button";
            case GOLDEN_LEGGINGS -> "minecraft:bamboo_button";
            case CHAINMAIL_LEGGINGS -> "minecraft:stone_button";
            case LEATHER_LEGGINGS -> "minecraft:spruce_button";

            // Boots
            case NETHERITE_BOOTS -> "minecraft:blackstone_button";
            case DIAMOND_BOOTS -> "minecraft:warped_button";
            case IRON_BOOTS -> "minecraft:pale_button";
            case GOLDEN_BOOTS -> "minecraft:bamboo_button";
            case CHAINMAIL_BOOTS -> "minecraft:stone_button";
            case LEATHER_BOOTS -> "minecraft:spruce_button";
            
            // Default fallback
            default -> "minecraft:stone_button";
        };
    }
} 