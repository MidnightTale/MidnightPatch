package fun.mntale.midnightPatch.module.entity.player;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import fun.mntale.midnightPatch.MidnightPatch;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Location;

public class PlayerLootListener implements Listener {

    private static final java.util.Map<UUID, Vector> lastHitDirection = new ConcurrentHashMap<>();
    private static final java.util.Map<UUID, ItemStack[]> deathInventories = new ConcurrentHashMap<>();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (t) -> {
            Vector hitDirection = player.getLocation().toVector().subtract(event.getDamager().getLocation().toVector()).normalize();
            lastHitDirection.put(player.getUniqueId(), hitDirection);
            }
        );
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        Boolean keepInventory = world.getGameRuleValue(GameRule.KEEP_INVENTORY);
        UUID uuid = player.getUniqueId();

        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (t) -> {
            ItemStack[] originalInventory = player.getInventory().getContents();
            deathInventories.put(uuid, originalInventory);
            if (Boolean.TRUE.equals(keepInventory)) {
                player.getInventory().clear();
            } else {
                event.getDrops().clear();
            }

            Vector dropDirection = lastHitDirection.getOrDefault(uuid, new Vector(0, 0.2, 0));
            Vector baseVelocity = dropDirection.clone().multiply(-0.4);

            MidnightPatch.instance.foliaLib.getScheduler().runAtLocation(player.getLocation(), (t2) -> {
                ItemStack[] inventory = deathInventories.remove(uuid);
                if (inventory == null) return;
                for (ItemStack itemStack : inventory) {
                    if (itemStack != null) {
                        Location dropLocation = player.getLocation().clone();
                        dropLocation.setY(dropLocation.getY() + 0.5);
                        Item item = player.getWorld().dropItemNaturally(dropLocation, itemStack);
                        item.setGlowing(true);
                        item.setInvulnerable(true);
                        item.setUnlimitedLifetime(true);
                        item.setCanMobPickup(false);
                        item.setMetadata("owner", new FixedMetadataValue(Objects.requireNonNull(event.getEntity().getServer().getPluginManager().getPlugin("MidnightPatch")), uuid.toString()));

                        Vector randomSpread = new Vector(
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.2
                        );
                        Vector finalVelocity = baseVelocity.clone().add(randomSpread);
                        item.setVelocity(finalVelocity);

                        int playerTotalExp = ExperienceUtil.getPlayerExp(player);
                        int expToDrop = (playerTotalExp * 70) / 100;

                        player.setLevel(0);
                        player.setExp(0);

                        while (expToDrop > 0) {
                            int orbValue = Math.min(expToDrop, 100);
                            Location orbLocation = player.getLocation().clone();
                            orbLocation.setY(orbLocation.getY() + 0.5);
                            player.getWorld().spawn(orbLocation, ExperienceOrb.class).setExperience(orbValue);
                            expToDrop -= orbValue;
                        }
                    }
                }
                lastHitDirection.remove(uuid);
            });
        });
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Item item = event.getItem();
        if (item.hasMetadata("owner")) {
            UUID ownerUUID = UUID.fromString(item.getMetadata("owner").getFirst().asString());
            if (!player.getUniqueId().equals(ownerUUID)) {
                event.setCancelled(true);
            }
        }
    }

} 