package fun.mntale.midnightPatch.module.entity.armorstand;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.inventory.EquipmentSlot;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.event.player.PlayerTeleportEvent;
import fun.mntale.midnightPatch.command.ToggleChairCommand;

public class ChairListener implements Listener {
    private final Map<Player, ArmorStand> sittingMap = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!player.getInventory().getItemInMainHand().getType().isAir()) return;
        if (player.isInsideVehicle()) return; // Prevent sitting while riding
        if (!ToggleChairCommand.isChairEnabled(player)) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!isChairBlock(block)) return;
        Block above = block.getRelative(BlockFace.UP);
        if (above.getType().isSolid()) return;
        if (sittingMap.containsKey(player)) return;
        // Prevent double sitting
        // Prevent sitting on an occupied chair
        boolean occupied = sittingMap.values().stream().anyMatch(stand -> stand != null && stand.isValid() && stand.getLocation().getBlock().equals(block));
        if (occupied) return;
        // Spawn invisible marker ArmorStand
        ArmorStand stand = block.getWorld().spawn(block.getLocation().add(0.5, getSitYOffset(block), 0.5), ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setMarker(true);
            as.setInvulnerable(true);
            as.setGravity(false);
            as.setSmall(true);
            as.setBasePlate(false);
            as.setArms(false);
            as.setCollidable(false);
            as.setPersistent(false);
        });
        stand.addPassenger(player);
        sittingMap.put(player, stand);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        ArmorStand stand = sittingMap.remove(player);
        if (stand != null && stand.isValid()) {
            FoliaScheduler.getEntityScheduler().run(stand, MidnightPatch.instance, (task) -> stand.remove(), null);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ArmorStand stand = sittingMap.remove(player);
        if (stand != null && stand.isValid()) {
            FoliaScheduler.getEntityScheduler().run(stand, MidnightPatch.instance, (task) -> stand.remove(), null);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        ArmorStand stand = sittingMap.remove(player);
        if (stand != null && stand.isValid()) {
            FoliaScheduler.getEntityScheduler().run(stand, MidnightPatch.instance, (task) -> stand.remove(), null);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        removeChairIfPresent(block);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            removeChairIfPresent(block);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            removeChairIfPresent(block);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            removeChairIfPresent(block);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            removeChairIfPresent(block);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        ArmorStand stand = sittingMap.remove(player);
        if (stand != null && stand.isValid()) {
            FoliaScheduler.getEntityScheduler().run(stand, MidnightPatch.instance, (task) -> stand.remove(), null);
            if (player.isInsideVehicle()) {
                FoliaScheduler.getEntityScheduler().run(player, MidnightPatch.instance, (task) -> player.leaveVehicle(), null);
            }
        }
    }

    private void removeChairIfPresent(Block block) {
        // Use RegionScheduler to ensure Folia safety for block location
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, block.getLocation(), (task) -> {
            sittingMap.entrySet().removeIf(entry -> {
                ArmorStand stand = entry.getValue();
                if (stand != null && stand.isValid()) {
                    Block standBlock = stand.getLocation().getBlock();
                    if (standBlock.equals(block)) {
                        // Use EntityScheduler for entity actions
                        FoliaScheduler.getEntityScheduler().run(stand, MidnightPatch.instance, (entityTask) -> {
                            stand.remove();
                        }, null);
                        if (entry.getKey().isInsideVehicle()) {
                            FoliaScheduler.getEntityScheduler().run(entry.getKey(), MidnightPatch.instance, (entityTask) -> {
                                entry.getKey().leaveVehicle();
                            }, null);
                        }
                        return true;
                    }
                }
                return false;
            });
        });
    }

    private boolean isChairBlock(Block block) {
        Material type = block.getType();
        String name = type.name();
        if (name.endsWith("_STAIRS")) return true;
        if (name.endsWith("_CARPET")) return true;
        if (name.endsWith("_SLAB") && block.getBlockData() instanceof Slab slab && slab.getType() == Slab.Type.BOTTOM) return true;
        return false;
    }

    private double getSitYOffset(Block block) {
        String name = block.getType().name();
        if (name.endsWith("_STAIRS")) return 0.4;
        if (name.endsWith("_CARPET")) return 0.0;
        if (name.endsWith("_SLAB")) return 0.15;
        return 0.0;
    }
} 