package fun.mntale.midnightPatch;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;

import io.papermc.paper.command.brigadier.BasicCommand;
import fun.mntale.midnightPatch.command.DieCommand;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;
import fun.mntale.midnightPatch.module.world.reacharound.ReachAroundBlockListener;
import fun.mntale.midnightPatch.module.entity.player.projectile.ProjectileDamageListener;
import fun.mntale.midnightPatch.module.entity.player.task.PlayerTaskManager;
import fun.mntale.midnightPatch.module.world.fertilizer.FertilizerListener;
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
import fun.mntale.midnightPatch.module.world.enchantment.ResilienceEnchantment;
import fun.mntale.midnightPatch.module.world.enchantment.UndertowEnchantment;
import fun.mntale.midnightPatch.module.entity.player.MendingRepair;
import fun.mntale.midnightPatch.bootstrap.MidnightPatchExpansion;
import fun.mntale.midnightPatch.bootstrap.MidnightPatchStartupJoinDelay;
import fun.mntale.midnightPatch.command.PlayerCommand;
import fun.mntale.midnightPatch.command.TaskCommand;
import fun.mntale.midnightPatch.module.entity.armorstand.ChairListener;

public final class MidnightPatch extends JavaPlugin implements Listener {
    public static MidnightPatch instance;
    public FoliaLib foliaLib;

    @Override
    public void onEnable() {
        instance = this;
        foliaLib = new FoliaLib(this);
        
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new MidnightPatchExpansion().register();
        }
        getServer().getPluginManager().registerEvents(new ReachAroundBlockListener(), this);
        BasicCommand ToggleReachAroundCommand = new ToggleReachAroundCommand();
        registerCommand("togglereacharound", ToggleReachAroundCommand);

        getServer().getPluginManager().registerEvents(new FertilizerListener(), this);
        getServer().getPluginManager().registerEvents(new MossBlockFertilizerListener(), this);
        getServer().getPluginManager().registerEvents(new PoseArmorStandListener(), this);
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

        BasicCommand dieCommand = new DieCommand();
        registerCommand("die", dieCommand);

        getServer().getPluginManager().registerEvents(new ProjectileDamageListener(), this);

        getServer().getPluginManager().registerEvents(new DesirePathListener(), this);

        getServer().getPluginManager().registerEvents(new BedrockDeathCameraListener(), this);

        getServer().getPluginManager().registerEvents(new MendingRepair(), this);

        BasicCommand playerCommand = new PlayerCommand();
        registerCommand("player", playerCommand);

        getServer().getPluginManager().registerEvents(new PlayerTaskManager(), this);
        BasicCommand taskCommand = new TaskCommand();
        registerCommand("task", taskCommand);

        getServer().getPluginManager().registerEvents(new ChairListener(), this);

        MidnightPatchStartupJoinDelay.START_TIME = System.currentTimeMillis();
    }


}
