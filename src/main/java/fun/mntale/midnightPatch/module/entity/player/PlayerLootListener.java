package fun.mntale.midnightPatch.module.entity.player;

import java.util.UUID;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import fun.mntale.midnightPatch.command.ToggleDeathLootGlowCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootInvulnerableCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootNoDespawnCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootLetMobPickupCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootLetPlayerPickupCommand;

public class PlayerLootListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        Boolean keepInventory = world.getGameRuleValue(GameRule.KEEP_INVENTORY);

        ItemStack[] originalInventory = player.getInventory().getContents();

        if (Boolean.TRUE.equals(keepInventory)) {
            player.getInventory().clear();
        } else {
            event.getDrops().clear();
        }

        for (ItemStack itemStack : originalInventory) {
            if (itemStack != null) {
                Item item = player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                item.setGlowing(ToggleDeathLootGlowCommand.isEnabled(player));
                item.setInvulnerable(ToggleDeathLootInvulnerableCommand.isEnabled(player));
                item.setUnlimitedLifetime(ToggleDeathLootNoDespawnCommand.isEnabled(player));
                item.setCanMobPickup(ToggleDeathLootLetMobPickupCommand.isEnabled(player));
                if (!ToggleDeathLootLetPlayerPickupCommand.isEnabled(player)) {
                    item.setMetadata("owner", new FixedMetadataValue(event.getEntity().getServer().getPluginManager().getPlugin("MidnightPatch"), player.getUniqueId().toString()));
                }
                Vector velocity = new Vector(
                        Math.random() * 0.2 - 0.2  / 2,
                        Math.random() * (0.2  / 3) - (0.2  / 3) / 2,
                        Math.random() * 0.2  - 0.2  / 2
                );
                item.setVelocity(velocity);

                int playerTotalExp = ExperienceUtil.getPlayerExp(player);
                int expToDrop = (playerTotalExp * 70) / 100; 

                player.setLevel(0);
                player.setExp(0);

                while (expToDrop > 0) {
                    int orbValue = Math.min(expToDrop, 100);
                    player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(orbValue);
                    expToDrop -= orbValue;
                }
            }
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Item item = event.getItem();
        if (item.hasMetadata("owner")) {
            UUID ownerUUID = UUID.fromString(item.getMetadata("owner").get(0).asString());
            if (!player.getUniqueId().equals(ownerUUID)) {
                event.setCancelled(true);
            }
        }
    }

} 