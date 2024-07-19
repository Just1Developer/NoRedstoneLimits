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

        if (!_do) return;

        /*

        Bukkit.broadcastMessage("§eNorth block: " + neighbor);
        Bukkit.broadcastMessage("§bNorth south equals activated: " + neighbor.getRelative(BlockFace.SOUTH).equals(e.getBlock()));
        Bukkit.broadcastMessage("§dNorth isRepeater: " + isRepeater(neighbor));
        if (isRepeater(neighbor)) Bukkit.broadcastMessage("§cNorth isPowered: " + isPowered(neighbor));

        * */

        Block b = e.getBlock();
        if (isAnyNeighborActiveWatchingRepeater(b)) {
            e.setNewCurrent(MAX_CURRENT);
        }
    }

    private static boolean isAnyNeighborActiveWatchingRepeater(Block block) {
        Block neighbor = block.getRelative(BlockFace.NORTH);
        Bukkit.broadcastMessage("§eNorth | isRepeater: §c" + isRepeater(neighbor) + "§e, isPowering: §c" + isPowering(neighbor, BlockFace.SOUTH) + "§e, isPowered: §c" + isPowered(neighbor));
        if (isRepeater(neighbor) && isPowering(neighbor, BlockFace.SOUTH)) {
            return true;
        }

        neighbor = block.getRelative(BlockFace.EAST);
        Bukkit.broadcastMessage("§dEast | isRepeater: §c" + isRepeater(neighbor) + "§d, isPowering: §c" + isPowering(neighbor, BlockFace.WEST) + "§d, isPowered: §c" + isPowered(neighbor));
        if (isRepeater(neighbor) && isPowering(neighbor, BlockFace.WEST)) {
            return true;
        }

        neighbor = block.getRelative(BlockFace.SOUTH);
        Bukkit.broadcastMessage("§aSouth | isRepeater: §c" + isRepeater(neighbor) + "§a, isPowering: §c" + isPowering(neighbor, BlockFace.NORTH) + "§a, isPowered: §c" + isPowered(neighbor));
        if (isRepeater(neighbor) && isPowering(neighbor, BlockFace.NORTH)) {
            return true;
        }

        neighbor = block.getRelative(BlockFace.WEST);
        Bukkit.broadcastMessage("§bWest | isRepeater: §c" + isRepeater(neighbor) + "§b, isPowering: §c" + isPowering(neighbor, BlockFace.EAST) + "§b, isPowered: §c" + isPowered(neighbor));
        if (isRepeater(neighbor) && isPowering(neighbor, BlockFace.EAST)) {
            return true;
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
        Bukkit.broadcastMessage("0");
        if (noDelayRepeater.getType() != REPEATER_MAT) return false;
        Bukkit.broadcastMessage("1");

        Directional directional = (Directional) noDelayRepeater.getBlockData();
        Bukkit.broadcastMessage("2");
        BlockFace side = directional.getFacing();
        if (!reverseFace.containsKey(side)) return false;
        Bukkit.broadcastMessage("3 + " + side);

        // Todo hmmm

        Block block = noDelayRepeater.getRelative(side);
        Bukkit.broadcastMessage("4 + " + reverseFace.get(side) + " + " + block.isBlockFacePowered(reverseFace.get(side)));
        return block.isBlockFacePowered(reverseFace.get(side))
                && (isLookingTowards == BlockFace.SELF || isLookingTowards == side);    // Either self (default) or direction we want
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        Bukkit.broadcastMessage("1");
        if (b.getBlockData() instanceof AnaloguePowerable powerable) {
            Bukkit.broadcastMessage("2");
            if (!isAnyNeighborActiveWatchingRepeater(b)) return;
            Bukkit.broadcastMessage("3");
            powerable.setPower(powerable.getMaximumPower());
            b.setBlockData(powerable);
        }

        if (!isRepeater(e.getBlock())) return;

        // Doesn't matter if it's powered or not
        // If it isn't the power is gone so fast you don't even know it's there
        // Also, if we do check, it hasn't properly updated yet so the last check in isPowered doesn't work

        Directional directional = (Directional) e.getBlock().getBlockData();
        BlockFace side = directional.getFacing();
        Block block = e.getBlock().getRelative(side);
        if (block.getBlockData() instanceof RedstoneWire redstoneWire) {
            redstoneWire.setPower(redstoneWire.getMaximumPower());
            block.setBlockData(redstoneWire);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        _do = !_do;
        Bukkit.broadcastMessage("ok -> " + _do);
        return true;
    }
}
