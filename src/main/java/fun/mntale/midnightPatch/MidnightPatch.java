package fun.mntale.midnightPatch;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;

import io.papermc.paper.command.brigadier.BasicCommand;
import fun.mntale.midnightPatch.command.KillCommand;
import fun.mntale.midnightPatch.command.ToggleArmorStandPoseCommand;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;
import fun.mntale.midnightPatch.command.ToggleHealthIndicatorCommand;
import fun.mntale.midnightPatch.command.ToggleLootChestProtectionCommand;
import fun.mntale.midnightPatch.command.TogglePhantomIsolationCommand;
import fun.mntale.midnightPatch.command.ToggleProjectileDamageCommand;
import fun.mntale.midnightPatch.command.ToggleDesirePathCommand;
import fun.mntale.midnightPatch.command.ToggleFakePlayerOnJoinLeaveCommand;
import fun.mntale.midnightPatch.command.ToggleDeathCameraCommand;
import fun.mntale.midnightPatch.module.world.reacharound.ReachAroundBlockListener;
import fun.mntale.midnightPatch.module.entity.minecart.MinecartChunkLoadListener;
import fun.mntale.midnightPatch.module.entity.player.LootMobTargetListener;
import fun.mntale.midnightPatch.module.entity.player.PhantomIsolation;
import fun.mntale.midnightPatch.module.entity.player.PlayerLootListener;
import fun.mntale.midnightPatch.module.entity.player.fakeplayer.MobSpawnerPlayer;
import fun.mntale.midnightPatch.module.entity.player.indicator.HealthDamageIndicatorListener;
import fun.mntale.midnightPatch.module.entity.player.projectile.ProjectileDamageListener;
import fun.mntale.midnightPatch.module.entity.player.task.PlayerTaskManager;
import fun.mntale.midnightPatch.module.world.fertilizer.FertilizerListener;
import fun.mntale.midnightPatch.module.entity.babymob.BabyMobListener;
import fun.mntale.midnightPatch.module.world.death.BedrockDeathCameraListener;
import fun.mntale.midnightPatch.module.world.desirepath.DesirePathListener;
import fun.mntale.midnightPatch.module.world.fertilizer.MossBlockFertilizerListener;
import fun.mntale.midnightPatch.module.world.loot.FrostbiteLoot;
import fun.mntale.midnightPatch.module.world.loot.GraceLoot;
import fun.mntale.midnightPatch.module.world.loot.HarvestingLoot;
import fun.mntale.midnightPatch.module.world.loot.RecastingLoot;
import fun.mntale.midnightPatch.module.world.loot.ResilienceLoot;
import fun.mntale.midnightPatch.module.world.loot.UndertowLoot;
import fun.mntale.midnightPatch.module.world.loot.UpdraftLoot;
import fun.mntale.midnightPatch.module.entity.armorstand.PoseArmorStandListener;
import fun.mntale.midnightPatch.module.world.enchantment.FrostbiteEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.GraceEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.UpdraftEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.HarvestingEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.RecastingEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.RelimitAnvil;
import fun.mntale.midnightPatch.module.world.enchantment.ResilienceEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.UndertowEnchantment;
import fun.mntale.midnightPatch.module.entity.player.MendingRepair;
import fun.mntale.midnightPatch.command.ToggleMendingRepairCommand;
import fun.mntale.midnightPatch.bootstrap.MidnightPatchExpansion;
import fun.mntale.midnightPatch.bootstrap.MidnightPatchStartupJoinDelay;
import fun.mntale.midnightPatch.command.PlayerCommand;
import fun.mntale.midnightPatch.command.TaskCommand;
import fun.mntale.midnightPatch.module.entity.player.locatorbar.LocatorBar;
import fun.mntale.midnightPatch.command.ToggleLocatorBarCommand;

public final class MidnightPatch extends JavaPlugin implements Listener {
    public static MidnightPatch instance;

    @Override
    public void onEnable() {
        instance = this;
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new MidnightPatchExpansion().register();
        }
        getServer().getPluginManager().registerEvents(new ReachAroundBlockListener(), this);
        BasicCommand ToggleReachAroundCommand = new ToggleReachAroundCommand();
        registerCommand("togglereacharound", ToggleReachAroundCommand);

        getServer().getPluginManager().registerEvents(new MinecartChunkLoadListener(), this);
        getServer().getPluginManager().registerEvents(new FertilizerListener(), this);
        getServer().getPluginManager().registerEvents(new LootMobTargetListener(), this);
        getServer().getPluginManager().registerEvents(new BabyMobListener(), this);
        getServer().getPluginManager().registerEvents(new MossBlockFertilizerListener(), this);
        getServer().getPluginManager().registerEvents(new PoseArmorStandListener(), this);
        getServer().getPluginManager().registerEvents(new RelimitAnvil(), this);
        getServer().getPluginManager().registerEvents(new MidnightPatchStartupJoinDelay(), this);
        
        

        getServer().getPluginManager().registerEvents(new FrostbiteEnchantment(), this);
        getServer().getPluginManager().registerEvents(new FrostbiteLoot(), this);

        getServer().getPluginManager().registerEvents(new UpdraftEnchantment(), this);
        getServer().getPluginManager().registerEvents(new RecastingLoot(), this);

        getServer().getPluginManager().registerEvents(new HarvestingEnchantment(), this);
        getServer().getPluginManager().registerEvents(new HarvestingLoot(), this);

        getServer().getPluginManager().registerEvents(new RecastingEnchantment(), this);
        getServer().getPluginManager().registerEvents(new UpdraftLoot(), this);

        getServer().getPluginManager().registerEvents(new GraceEnchantment(), this);
        getServer().getPluginManager().registerEvents(new GraceLoot(), this);

        getServer().getPluginManager().registerEvents(new ResilienceEnchantment(), this);
        getServer().getPluginManager().registerEvents(new ResilienceLoot(), this);

        getServer().getPluginManager().registerEvents(new UndertowEnchantment(), this);
        getServer().getPluginManager().registerEvents(new UndertowLoot(), this);

        PhantomIsolation.start(this);
        
        
        BasicCommand killCommand = new KillCommand();
        registerCommand("kill", killCommand);

        getServer().getPluginManager().registerEvents(new HealthDamageIndicatorListener(), this);
        BasicCommand toggleIndicatorCommand = new ToggleHealthIndicatorCommand();
        registerCommand("toggleindicator", toggleIndicatorCommand);
        
        getServer().getPluginManager().registerEvents(new ProjectileDamageListener(), this);
        BasicCommand toggleProjectileDamageCommand = new ToggleProjectileDamageCommand();
        registerCommand("toggleprojectiledamage", toggleProjectileDamageCommand);

        getServer().getPluginManager().registerEvents(new DesirePathListener(), this);
        BasicCommand toggleDesirePathCommand = new ToggleDesirePathCommand();
        registerCommand("toggledesirepath", toggleDesirePathCommand);

        getServer().getPluginManager().registerEvents(new BedrockDeathCameraListener(), this);
        BasicCommand toggleDeathCameraCommand = new ToggleDeathCameraCommand();
        registerCommand("toggledeathcamera", toggleDeathCameraCommand);

        BasicCommand toggleArmorStandPoseCommand = new ToggleArmorStandPoseCommand();
        registerCommand("togglearmorstandpose", toggleArmorStandPoseCommand);

        BasicCommand toggleLootChestProtectionCommand = new ToggleLootChestProtectionCommand();
        registerCommand("toggletargetloot", toggleLootChestProtectionCommand);

        BasicCommand togglePhantomIsolationCommand = new TogglePhantomIsolationCommand();
        registerCommand("togglephantom", togglePhantomIsolationCommand);

        getServer().getPluginManager().registerEvents(new PlayerLootListener(), this);
        BasicCommand toggleDeathLootGlowCommand = new fun.mntale.midnightPatch.command.ToggleDeathLootGlowCommand();
        registerCommand("toggledeathlootglow", toggleDeathLootGlowCommand);
        BasicCommand toggleDeathLootInvulnerableCommand = new fun.mntale.midnightPatch.command.ToggleDeathLootInvulnerableCommand();
        registerCommand("toggledeathlootinvulnerable", toggleDeathLootInvulnerableCommand);
        BasicCommand toggleDeathLootNoDespawnCommand = new fun.mntale.midnightPatch.command.ToggleDeathLootNoDespawnCommand();
        registerCommand("toggledeathlootnodespawn", toggleDeathLootNoDespawnCommand);
        BasicCommand toggleDeathLootLetMobPickupCommand = new fun.mntale.midnightPatch.command.ToggleDeathLootLetMobPickupCommand();
        registerCommand("toggledeathlootletmobpickup", toggleDeathLootLetMobPickupCommand);
        BasicCommand toggleDeathLootLetPlayerPickupCommand = new fun.mntale.midnightPatch.command.ToggleDeathLootLetPlayerPickupCommand();
        registerCommand("toggledeathlootletplayerpickup", toggleDeathLootLetPlayerPickupCommand);

        getServer().getPluginManager().registerEvents(new MendingRepair(), this);
        BasicCommand toggleMendingRepairCommand = new ToggleMendingRepairCommand();
        registerCommand("togglemendingrepair", toggleMendingRepairCommand);

        getServer().getPluginManager().registerEvents(new MobSpawnerPlayer(), this);
        BasicCommand playerCommand = new PlayerCommand();
        registerCommand("player", playerCommand);
        BasicCommand toggleFakePlayerOnJoinLeaveCommand = new ToggleFakePlayerOnJoinLeaveCommand();
        registerCommand("toggleshadowplayer", toggleFakePlayerOnJoinLeaveCommand);

        getServer().getPluginManager().registerEvents(new PlayerTaskManager(), this);
        BasicCommand taskCommand = new TaskCommand();
        registerCommand("task", taskCommand);

        // Initialize LocatorBar
        LocatorBar.start();
        getServer().getPluginManager().registerEvents(new LocatorBar(), this);
        BasicCommand toggleLocatorBarCommand = new ToggleLocatorBarCommand();
        registerCommand("togglelocatorbarposition", toggleLocatorBarCommand);

        MidnightPatchStartupJoinDelay.START_TIME = System.currentTimeMillis();
    }


}
