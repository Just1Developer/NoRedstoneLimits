package net.justonedev.mc.noRedstoneLimits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NoRedstoneLimits extends JavaPlugin implements Listener {

    public static NoRedstoneLimits instance;
    private static final int MAX_CURRENT = 15;

    boolean _do = true;

    static final boolean DO_DEBUG_PRINTS = false;
    static final int DEBUG_LEVEL = 2;

    private static void print(int level, String msg) {
        if (!DO_DEBUG_PRINTS) return;
        if (level < DEBUG_LEVEL) return;
        Bukkit.broadcastMessage(msg);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("change").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    Set<Block> blocks = new HashSet<>();
    List<Location> locs = new ArrayList<>();

    @EventHandler
    public void onRedstone(BlockRedstoneEvent e) {
        if (e.getBlock().getType() != Material.REDSTONE_WIRE) return;
        // Other power source
        Block b = e.getBlock();
        print(3, "§eWatching: " + isAnyNeighborActiveWatchingRepeater(b));
        updateNearbyRepeatersNearlyInstant(b);
        if (isAnyNeighborActiveWatchingRepeater(b)) {
            e.setNewCurrent(MAX_CURRENT);
        }
    }

    private static boolean isAnyNeighborActiveWatchingRepeater(Block block) {
        for (BlockFace face : reverseFace.values()) {
            if (isPowering(block.getRelative(face), reverseFace.get(face))) return true;
        }
        return false;
    }

    private static final Material REPEATER_MAT = Material.JACK_O_LANTERN;

    static final Map<BlockFace, BlockFace> reverseFace = new HashMap<>();
    static {
        reverseFace.put(BlockFace.NORTH, BlockFace.SOUTH);
        reverseFace.put(BlockFace.EAST, BlockFace.WEST);
        reverseFace.put(BlockFace.SOUTH, BlockFace.NORTH);
        reverseFace.put(BlockFace.WEST, BlockFace.EAST);
    }

    private static boolean isNotRepeater(Block noDelayRepeater) {
        if (noDelayRepeater == null) return true;
        return noDelayRepeater.getType() != REPEATER_MAT;
    }

    private static boolean isPowered(Block noDelayRepeater) {
        return isPowering(noDelayRepeater, BlockFace.SELF);
    }
    private static boolean isPowering(Block noDelayRepeater, BlockFace isLookingTowards) {
        if (noDelayRepeater == null) return false;
        if (noDelayRepeater.getType() != REPEATER_MAT) return false;

        Directional directional = (Directional) noDelayRepeater.getBlockData();
        BlockFace side = directional.getFacing();

        if (!reverseFace.containsKey(side)) return false;
        if (isLookingTowards == BlockFace.SELF) return noDelayRepeater.isBlockPowered();
        if (side != isLookingTowards) return false; // Not looking at us

        BlockFace reverse = reverseFace.get(side);
        BlockData data = noDelayRepeater.getRelative(reverse).getBlockData();

        return noDelayRepeater.isBlockFacePowered(reverse)
                && data instanceof AnaloguePowerable
                && ((AnaloguePowerable) data).getPower() > 0;
    }

    private void updateNearbyRepeaters(Block b) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            updateNearbyRepeatersInstant(b);
        }, 1);
    }

    private void updateNearbyRepeatersNearlyInstant(Block b) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            updateNearbyRepeatersInstant(b);
        }, 0);
    }

    private void updateNearbyRepeatersInstant(Block b) {
        for (BlockFace face : reverseFace.values()) {
            if (!isPowering(b.getRelative(face), face)) continue;

            // Its powered and looking towards this next block:
            Block next = b.getRelative(face).getRelative(face);
            if (next.getBlockData() instanceof AnaloguePowerable powerable) {
                // activate block power.
                powerable.setPower(powerable.getMaximumPower());
                next.setBlockData(powerable);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        if (b.getBlockData() instanceof AnaloguePowerable powerable) {
            // Is it perhaps powering a repeater?
            updateNearbyRepeaters(b);
            // It it powered by one?
            if (!isAnyNeighborActiveWatchingRepeater(b)) return;
            powerable.setPower(powerable.getMaximumPower());
            b.setBlockData(powerable);
            return;
        }

        if (isNotRepeater(e.getBlock())) return;

        Directional directional = (Directional) e.getBlock().getBlockData();
        BlockFace side = directional.getFacing();
        if (!isPowering(b, side)) return;
        Block block = e.getBlock().getRelative(side);
        if (block.getBlockData() instanceof RedstoneWire redstoneWire) {
            redstoneWire.setPower(redstoneWire.getMaximumPower());
            block.setBlockData(redstoneWire);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (b.getBlockData() instanceof AnaloguePowerable powerable) {
            updateNearbyRepeaters(b);
            return;
        }

        if (isNotRepeater(e.getBlock())) return;

        Directional directional = (Directional) e.getBlock().getBlockData();
        BlockFace side = directional.getFacing();
        if (!isPowering(b, side)) return;

        Block block = e.getBlock().getRelative(side);
        if (block.getBlockData() instanceof RedstoneWire redstoneWire) {
            redstoneWire.setPower(redstoneWire.getMaximumPower() - 10);  // Trigger update but dont remove all power
            block.setBlockData(redstoneWire);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cNo.");
            return;
        }
        _do = !_do;
        print(0, "ok -> " + _do);
        return true;
    }
}
