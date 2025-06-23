package fun.mntale.midnightPatch.module.entity.minecart;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class MinecartChunkLoadListener implements Listener {
    private final MinecartChunkDataManager dataManager = new MinecartChunkDataManager();

    public MinecartChunkLoadListener() {
        dataManager.loadMinecartData();
    }

    public void shutdown() {
        dataManager.saveMinecartData();
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }
        dataManager.getActiveMinecarts().remove(minecart.getUniqueId());
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }
        if (minecart.getPassengers().isEmpty()) {
            Location loc = minecart.getLocation();
            dataManager.getActiveMinecarts().put(minecart.getUniqueId(), new MinecartChunkData(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ()));
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }
        if (!minecart.getPassengers().isEmpty()) {
            return;
        }
        Location newLocation = minecart.getLocation();
        World world = newLocation.getWorld();
        if (world == null) {
            return;
        }
        dataManager.getActiveMinecarts().put(minecart.getUniqueId(), new MinecartChunkData(world.getName(), newLocation.getX(), newLocation.getY(), newLocation.getZ()));
        dataManager.loadChunksAroundLocation(world, newLocation.getX(), newLocation.getY(), newLocation.getZ());
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (!(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }
        dataManager.getActiveMinecarts().remove(minecart.getUniqueId());
    }
} 