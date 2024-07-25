package net.justonedev.mc.noRedstoneLimits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.AnaloguePowerable;
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

    static final boolean DO_DEBUG_PRINTS = true;
    static final int DEBUG_LEVEL = 0;

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

        /*

        print(0, "§eNorth block: " + neighbor);
        print(0, "§bNorth south equals activated: " + neighbor.getRelative(BlockFace.SOUTH).equals(e.getBlock()));
        print(0, "§dNorth isRepeater: " + isRepeater(neighbor));
        if (isRepeater(neighbor)) print(0, "§cNorth isPowered: " + isPowered(neighbor));

        * */

        // Other power source
        Block b = e.getBlock();
        /*
        for (BlockFace face : BlockFace.values()) {
            if (b.isBlockFacePowered(face)) {
                //print(1, "Redstone is powered otherwise");
                //return;
            }
        }

         */
        print(1, "§eWatching: " + isAnyNeighborActiveWatchingRepeater(b));

        if (isAnyNeighborActiveWatchingRepeater(b)) {
            e.setNewCurrent(MAX_CURRENT);
        }
    }

    private static boolean isAnyNeighborActiveWatchingRepeater(Block block) {
        for (BlockFace face : reverseFace.values()) {
            if (isPowering(block.getRelative(face), reverseFace.get(face))) return true;
        }

        /*
        print(0, "Checking validity...:");
        Block neighbor = block.getRelative(BlockFace.NORTH);
        print(0, "§eNorth | isRepeater: §c" + isRepeater(neighbor) + "§e, isPowering: §c" + isPowering(neighbor, BlockFace.SOUTH) + "§e, isPowered: §c" + isPowered(neighbor));
        if (isRepeater(neighbor) && isPowering(neighbor, BlockFace.SOUTH)) {
            return true;
        }

        neighbor = block.getRelative(BlockFace.EAST);
        print(0, "§dEast | isRepeater: §c" + isRepeater(neighbor) + "§d, isPowering: §c" + isPowering(neighbor, BlockFace.WEST) + "§d, isPowered: §c" + isPowered(neighbor));
        if (isRepeater(neighbor) && isPowering(neighbor, BlockFace.WEST)) {
            return true;
        }

        neighbor = block.getRelative(BlockFace.SOUTH);
        print(0, "§aSouth | isRepeater: §c" + isRepeater(neighbor) + "§a, isPowering: §c" + isPowering(neighbor, BlockFace.NORTH) + "§a, isPowered: §c" + isPowered(neighbor));
        if (isRepeater(neighbor) && isPowering(neighbor, BlockFace.NORTH)) {
            return true;
        }

        neighbor = block.getRelative(BlockFace.WEST);
        print(0, "§bWest | isRepeater: §c" + isRepeater(neighbor) + "§b, isPowering: §c" + isPowering(neighbor, BlockFace.EAST) + "§b, isPowered: §c" + isPowered(neighbor));
        if (isRepeater(neighbor) && isPowering(neighbor, BlockFace.EAST)) {
            return true;
        }

         */
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

    private static boolean isRepeater(Block noDelayRepeater) {
        if (noDelayRepeater == null) return false;
        if (noDelayRepeater.getType() != REPEATER_MAT) return false;
        return true;
    }

    private static boolean isPowered(Block noDelayRepeater) {
        return isPowering(noDelayRepeater, BlockFace.SELF);
    }
    private static boolean isPowering(Block noDelayRepeater, BlockFace isLookingTowards) {
        if (noDelayRepeater == null) return false;
        print(0, "§b0");
        if (noDelayRepeater.getType() != REPEATER_MAT) return false;
        print(0, "§b1");

        Directional directional = (Directional) noDelayRepeater.getBlockData();
        print(0, "§b2");
        BlockFace side = directional.getFacing();

        if (!reverseFace.containsKey(side)) return false;
        if (isLookingTowards == BlockFace.SELF) return noDelayRepeater.isBlockPowered();
        if (side != isLookingTowards) return false; // Not looking at us

        print(0, "§b3 + " + side + " - §c" + noDelayRepeater.getType());

        // Todo hmmm

        Block block = noDelayRepeater;
        print(1, "§d4 + " + reverseFace.get(side) + " + " + block.isBlockFacePowered(reverseFace.get(side)) + " + " + block.isBlockFacePowered(side) + " + " + block.isBlockFaceIndirectlyPowered(reverseFace.get(side)) + " + " + block.isBlockFaceIndirectlyPowered(side));


        //block = noDelayRepeater.getRelative(side);
        //print(0, "§b5 + " + reverseFace.get(side) + " + " + block.isBlockFacePowered(reverseFace.get(side)) + " + " + block.isBlockFacePowered(side) + " + " + block.isBlockFaceIndirectlyPowered(reverseFace.get(side)) + " + " + block.isBlockFaceIndirectlyPowered(side));
        return noDelayRepeater.isBlockFacePowered(reverseFace.get(side));
    }

    private void updateNearbyRepeaters(Block b) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            for (BlockFace face : reverseFace.values()) {
                print(2, "§aRelative: " + b.getRelative(face));
                if (!isPowering(b.getRelative(face), face)) continue;



                // Its powered and looking towards this next block:
                Block next = b.getRelative(face).getRelative(face);
                print(2, "§aThe block " + face + " of the set block is a powered repeater. Will be looking at next block " + next);
                if (next.getBlockData() instanceof AnaloguePowerable powerable) {
                    // activate block power.
                    powerable.setPower(powerable.getMaximumPower());
                    next.setBlockData(powerable);
                    print(2, "§aSet next to §ePowered§a. Next is: §c" + next);

                    // Todo this will also cause repeaters to be powerable from the side.
                    // Just ignored for now
                } else print(0, "§aDid §cnot§aset next to §cPowered");
            }
        }, 10);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        print(0, "§e1");
        if (b.getBlockData() instanceof AnaloguePowerable powerable) {
            print(0, "§e2");
            // Is it perhaps powering a repeater?
            updateNearbyRepeaters(b);
            // It it powered by one?
            if (!isAnyNeighborActiveWatchingRepeater(b)) return;
            print(0, "§e3");
            powerable.setPower(powerable.getMaximumPower());
            b.setBlockData(powerable);
            return;
        }

        if (!isRepeater(e.getBlock())) return;

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

        if (!isRepeater(e.getBlock())) return;

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
        _do = !_do;
        print(0, "ok -> " + _do);
        return true;
    }
}
