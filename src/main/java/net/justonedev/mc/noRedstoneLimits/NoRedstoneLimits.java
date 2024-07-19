package net.justonedev.mc.noRedstoneLimits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
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

        /*
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (int i = 0; i < locs.size(); ++i) {
                Location loc = locs.get(i);
                Block block = loc.getBlock();
                AnaloguePowerable powerable = (AnaloguePowerable) block.getBlockData();
                powerable.setPower(MAX_CURRENT);
                block.setBlockData(powerable);
            }
            Bukkit.broadcastMessage("§ePulse for §c" + blocks.size() + " §eblocks.");
        }, 0, 60);
         */
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
        //Bukkit.broadcastMessage("§eChange: " + e.getOldCurrent() + " -> " + e.getNewCurrent());

        if (!_do) return;

        /*

        Bukkit.broadcastMessage("§eNorth block: " + neighbor);
        Bukkit.broadcastMessage("§bNorth south equals activated: " + neighbor.getRelative(BlockFace.SOUTH).equals(e.getBlock()));
        Bukkit.broadcastMessage("§dNorth isRepeater: " + isRepeater(neighbor));
        if (isRepeater(neighbor)) Bukkit.broadcastMessage("§cNorth isPowered: " + isPowered(neighbor));

        * */

        Block b = e.getBlock();
        Block neighbor = b.getRelative(BlockFace.NORTH);
        if (isRepeater(neighbor) && isPowered(neighbor, BlockFace.SOUTH)) {
            e.setNewCurrent(MAX_CURRENT);
        }

        neighbor = b.getRelative(BlockFace.EAST);
        if (isRepeater(neighbor) && isPowered(neighbor, BlockFace.WEST)) {
            e.setNewCurrent(MAX_CURRENT);
        }

        neighbor = b.getRelative(BlockFace.SOUTH);
        if (isRepeater(neighbor) && isPowered(neighbor, BlockFace.NORTH)) {
            e.setNewCurrent(MAX_CURRENT);
        }

        neighbor = b.getRelative(BlockFace.WEST);
        if (isRepeater(neighbor) && isPowered(neighbor, BlockFace.EAST)) {
            e.setNewCurrent(MAX_CURRENT);
        }

        /*
        e.setNewCurrent(e.getOldCurrent());

        final Block block = e.getBlock();
        blocks.add(block);
        if (!locs.contains(block.getLocation())) return;
        locs.add(block.getLocation());
         */
        /*
        final AnaloguePowerable powerable = (AnaloguePowerable) block.getBlockData();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            //if (e.getNewCurrent() <= e.getOldCurrent()) return; // turning off or same
            //e.setNewCurrent(MAX_CURRENT);    // turning on, set to full power
            powerable.setPower(MAX_CURRENT);
            block.setBlockData(powerable);
            block.getState().update();
        }, 0, 60);

         */
    }

    private static final Material REPEATER_MAT = Material.JACK_O_LANTERN;

    static final Map<BlockFace, BlockFace> reverseFace = new HashMap<>();
    static {
        reverseFace.put(BlockFace.NORTH, BlockFace.SOUTH);
        reverseFace.put(BlockFace.EAST, BlockFace.WEST);
        reverseFace.put(BlockFace.SOUTH, BlockFace.NORTH);
        reverseFace.put(BlockFace.WEST, BlockFace.EAST);
    }

    private boolean isRepeater(Block noDelayRepeater) {
        if (noDelayRepeater == null) return false;
        if (noDelayRepeater.getType() != REPEATER_MAT) return false;
        return true;
    }

    private boolean isPowered(Block noDelayRepeater) {
        return isPowered(noDelayRepeater, BlockFace.SELF);
    }
    private boolean isPowered(Block noDelayRepeater, BlockFace isLookingTowards) {
        if (noDelayRepeater == null) return false;
        if (noDelayRepeater.getType() != REPEATER_MAT) return false;

        Directional directional = (Directional) noDelayRepeater.getBlockData();
        BlockFace side = directional.getFacing();
        if (!reverseFace.containsKey(side)) return false;

        Block block = noDelayRepeater.getRelative(side);
        return block.isBlockFacePowered(reverseFace.get(side))
                && (isLookingTowards == BlockFace.SELF || isLookingTowards == side);    // Either self (default) or direction we want
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!isRepeater(e.getBlock())) return;

        Directional directional = (Directional) e.getBlock().getBlockData();
        BlockFace side = directional.getFacing();
        Block block = e.getBlock().getRelative(side);
        block.setType(Material.EMERALD_BLOCK);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        _do = !_do;
        Bukkit.broadcastMessage("ok -> " + _do);
        return true;
    }
}
