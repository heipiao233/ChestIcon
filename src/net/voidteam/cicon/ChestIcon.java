package net.voidteam.cicon;

import com.Acrobot.Breeze.Utils.BlockUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;

/**
 * Created by Robby Duke on 1/9/15.
 * Copyright (c) 2015
 */
public class ChestIcon extends JavaPlugin implements Listener {
    private Plugin chestIcon = null;
    public List<HoldItem> HoldItems = new ArrayList<HoldItem>(500);
    public HashSet<String> saveNextItem = new HashSet<String>();

    public static Logger logger = Logger.getLogger("Minecraft");

    public static HashSet<Chunk> itemChunks = new HashSet<Chunk>();

    @Override
    public void onEnable() {
        /**
         * Let's just check that the dependencies are loaded, even though the plugin.yml should do this!
         */
        Plugin chestShop = getServer().getPluginManager().getPlugin("ChestShop");

        if (!chestShop.equals(null)) {
            this.getServer().getPluginManager().registerEvents(this, this);
        } else {
            Bukkit.getLogger().severe("ChestShop not found! Disabling plugin...");
            this.setEnabled(false);
        }
        
        /**
         * Prepare HoldItem Stuff
         */
        load();

        for (HoldItem fi : this.HoldItems) {
          fi.respawn();
        }

        getServer().getPluginManager().registerEvents(new HoldItemListener(this), this);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
          public void run() {
            for (HoldItem fi : ChestIcon.this.HoldItems)
              fi.respawn();
          }
        }
        , 1200L, 36000L);
    }

    @Override
    public void onDisable() {
        save();

        for (HoldItem fi : this.HoldItems)
          fi.destroy();
        
        super.onDisable();
    }
    
    @SuppressWarnings("deprecation")
	void save()
    {
        File folder = getDataFolder();
        if (!folder.exists()) {
          folder.mkdir();
        }

        File datafile = new File(folder.getAbsolutePath() + "/items.csv");
        if (!datafile.exists()) {
          try {
            datafile.createNewFile();
          } catch (Exception e) {
            return;
          }
        }
        try
        {
          FileOutputStream output = new FileOutputStream(datafile.getAbsoluteFile());
          BufferedWriter w = new BufferedWriter(new OutputStreamWriter(output));
          for (HoldItem i : this.HoldItems) {
            try
            {
              String line = "";
              line = line + i.owner + ",";
              line = line + i.location.getWorld().getName() + "," + i.location.getBlockX() + "," + i.location.getBlockY() + "," + i.location.getBlockZ() + ",";
              line = line + i.type + ",";
              line = line + i.material.getId() + ",";
              line = line + i.data;
              line = line + "\n";
              w.write(line);
            } catch (Exception e) {
              logger.info("[ChestIcon] Error saving item: " + i.material.toString() + " " + e.toString());
            }
          }
          w.flush();
          output.close();
        } catch (Exception e) {
          logger.info("[ChestIcon] Error saving file. " + e.toString());
        }
      }

    @SuppressWarnings("deprecation")
	void load() {
        File folder = getDataFolder();
        if (!folder.exists()) {
          folder.mkdir();
        }
        File datafile = new File(folder.getAbsolutePath() + "/items.csv");
        if (datafile.exists())
          try
          {
            FileInputStream input = new FileInputStream(datafile.getAbsoluteFile());
            InputStreamReader ir = new InputStreamReader(input);

            BufferedReader r = new BufferedReader(ir);
            while (true)
            {
              String locline = r.readLine();
              if (locline == null) {
                break;
              }
              String[] line = locline.split(",");

              String player = line[0];

              World world = getServer().getWorld(line[1]);
              Double x = Double.valueOf(line[2]);
              Double y = Double.valueOf(line[3]);
              Double z = Double.valueOf(line[4]);

              String type = line[5];
              Material mat = Material.getMaterial(Integer.valueOf(line[6]).intValue());
              short data = Short.valueOf(line[7]).shortValue();

              Location location = new Location(world, x.doubleValue(), y.doubleValue(), z.doubleValue());

              HoldItem fi = new HoldItem(player, location, type, mat, data);
              this.HoldItems.add(fi);
            }
          }
          catch (Exception e) {
            logger.info("[ChestIcon] Error loading file. " + e.toString());
          }
      }

    @EventHandler
    public void shopCreate(final ShopCreatedEvent event) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.chestIcon, new Runnable() {
            public void run() {
                Location displayLocation = event.getChest().getLocation().add(0, 1, 0);


                String itemCode = event.getSignLine((byte) 3);
                //String[] dataInput = String.valueOf(event.getSignLine((short) 3)).split(":");

                ItemStack displayStack = MaterialUtil.getItem(itemCode);

                /*if( dataInput.length == 2 ) {
                    displayStack.setDurability(Short.valueOf(dataInput[1]));
                }*/


                Item displayItem = event.getPlayer().getWorld().dropItem(displayLocation, displayStack);


                Boolean allowed = true;

                for (int x = 0; x < HoldItemListener.plugin.HoldItems.size(); x++) {
                    HoldItem HoldItem = HoldItemListener.plugin.HoldItems.get(x);
                    Iterator<Entity> entitiesIterator = displayItem.getNearbyEntities(0.5D, 0.5D, 0.5D).iterator();

                    while (entitiesIterator.hasNext()) {
                        Entity entity = (Entity) entitiesIterator.next();
                        if (entity.getLocation().getBlock().equals(HoldItem.location.getBlock()) && entity instanceof Item && !entity.equals(displayItem)) {
                            allowed = false;
                            displayItem.remove();
                        }
                    }
                }

                if (allowed) {
                    HoldItemListener.plugin.HoldItems.add(
                            new HoldItem(event.getPlayer().getName().toLowerCase(),
                                    displayItem.getLocation(), displayItem.getType().name(), displayItem.getItemStack().getType(),
                                    displayItem.getItemStack().getDurability())
                    );

                    displayItem.remove();
                }
            }
        }, 20L);
    }

    @EventHandler
    public void shopDestroy(final ShopDestroyedEvent event) {
        for (int i = 0; i < HoldItemListener.plugin.HoldItems.size(); i++) {
            HoldItem HoldItem = (HoldItem) HoldItemListener.plugin.HoldItems.get(i);

            if (event.getSign().getType() == Material.SIGN_POST || event.getSign().getType() == Material.WALL_SIGN) {
                Chest chest = uBlock.findConnectedChest(event.getSign());

                if (chest != null) {
                    Location chestLocation = chest.getLocation().add(0.5, 1.9, 0.5);
                    if (HoldItem.location.equals(chestLocation)) {
                        HoldItem.destroy();
                        HoldItemListener.plugin.HoldItems.remove(HoldItemListener.plugin.HoldItems.get(i));
                    }
                }
            }
        }
    }

    @EventHandler
    public void createChest(final BlockPlaceEvent event) {
        if (BlockUtil.isChest(event.getBlock())) {
            final Sign sign = uBlock.findAnyNearbyShopSign(event.getBlock());
            if (sign != null) {

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.chestIcon, new Runnable() {
                    public void run() {
                        Location displayLocation = event.getBlock().getLocation().add(0, 1, 0);


                        String itemCode = sign.getLine(3);
                        //String[] dataInput = String.valueOf(sign.getLine(3)).split(":");

                        ItemStack displayStack = MaterialUtil.getItem(itemCode);

		                /*if( dataInput.length == 2 ) {
		                    displayStack.setDurability(Short.valueOf(dataInput[1]));
		                }*/


                        Item displayItem = event.getPlayer().getWorld().dropItem(displayLocation, displayStack);


                        Boolean allowed = true;

                        for (int x = 0; x < HoldItemListener.plugin.HoldItems.size(); x++) {
                            HoldItem HoldItem = HoldItemListener.plugin.HoldItems.get(x);
                            Iterator<Entity> entitiesIterator = displayItem.getNearbyEntities(0.5D, 0.5D, 0.5D).iterator();

                            while (entitiesIterator.hasNext()) {
                                Entity entity = (Entity) entitiesIterator.next();
                                if (entity.getLocation().getBlock().equals(HoldItem.location.getBlock()) && entity instanceof Item && !entity.equals(displayItem)) {
                                    allowed = false;
                                    displayItem.remove();
                                }
                            }
                        }

                        if (allowed) {
                            HoldItemListener.plugin.HoldItems.add(
                                    new HoldItem(event.getPlayer().getName().toLowerCase(),
                                            displayItem.getLocation(), displayItem.getType().name(), displayItem.getItemStack().getType(),
                                            displayItem.getItemStack().getDurability())
                            );

                            displayItem.remove();
                        }
                    }
                }, 20L);
            }
        }
    }

    @EventHandler
    public void chestDestroyed(final BlockBreakEvent event) {
        if (BlockUtil.isChest(event.getBlock())) {
            for (int i = 0; i < HoldItemListener.plugin.HoldItems.size(); i++) {
                HoldItem HoldItem = (HoldItem) HoldItemListener.plugin.HoldItems.get(i);

                Location chestLocation = event.getBlock().getLocation().add(0.5, 1.9, 0.5);
                if (HoldItem.location.equals(chestLocation)) {
                    HoldItem.destroy();
                    HoldItemListener.plugin.HoldItems.remove(HoldItemListener.plugin.HoldItems.get(i));
                }
            }
        }
    }
    

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
      if (!(sender instanceof Player)) {
        sender.sendMessage("[ChestIcon] You can only use this command as a player.");
        return true;
      }
      Player p = (Player)sender;

      if ((cmd.getName().equalsIgnoreCase("chesticon")) && (args[0].equalsIgnoreCase("reload")) && (sender.hasPermission("chesticon.admin") || sender.isOp())) {
        for (HoldItem fi : this.HoldItems) {
          fi.respawn();
        }
        return true;
      }

      if ((cmd.getName().equalsIgnoreCase("chesticon")) && (args[0].equalsIgnoreCase("near")) && (sender.hasPermission("chesticon.admin") || sender.isOp())) {
    	  
        Player player = (Player)sender;
        Object list = new ArrayList();

        for (HoldItem fi : this.HoldItems) {
          if (fi.location.getWorld().equals(player.getLocation().getWorld()))
          {
            if ((fi.location.getWorld().getName().equalsIgnoreCase(player.getLocation().getWorld().getName())) && 
              (fi.location.distanceSquared(player.getLocation()) <= 100.0D)) {
              ((List)list).add(ChatColor.GRAY + "    ICON ID: " + ChatColor.AQUA + this.HoldItems.indexOf(fi) + ChatColor.GRAY + "    OWNER: " + ChatColor.AQUA + fi.owner + ChatColor.GRAY + "    ITEM: " + ChatColor.AQUA + fi.item.getItemStack().getType().name());
            }
          }

        }

        if (((List)list).size() == 0) {
          sender.sendMessage(ChatColor.GRAY + "[ChestIcon]" + ChatColor.AQUA + " No ChestIcons found nearby.");
        } else {
          p.sendMessage(ChatColor.GRAY + "[ChestIcon]" + ChatColor.AQUA + " Nearby ChestIcons within 5 blocks:");
          for (String str : (List<String>)list) {
            p.sendMessage(str);
          }
        }
        return true;
      }

      if ((cmd.getName().equalsIgnoreCase("chesticon")) && ((args[0].equalsIgnoreCase("remove")) || (args[0].equalsIgnoreCase("delete")))) {
        Integer id;
        if (args.length < 2) {
          sender.sendMessage(ChatColor.GRAY + "[ChestIcon]" + ChatColor.RED + " Missing ID. /chesticon remove id.");
          return true;
        }
        try
        {
          id = Integer.valueOf(Integer.parseInt(args[1]));
        }
        catch (Exception e)
        {
          sender.sendMessage(ChatColor.GRAY + "[ChestIcon]" + ChatColor.RED + " Not a valid icon ID.");
          return true;
        }
        if ((this.HoldItems.size() > id.intValue()) && (id.intValue() >= 0)) {
          if (sender.hasPermission("chesticon.admin") || sender.isOp()) {
            sender.sendMessage(ChatColor.GRAY + "[ChestIcon]" + ChatColor.AQUA + " Icon " + ChatColor.GRAY + args[1] + ChatColor.AQUA + " removed.");
            ((HoldItem)this.HoldItems.get(id.intValue())).destroy();
            this.HoldItems.remove(this.HoldItems.get(id.intValue()));
            save();
            return true;
          }
          sender.sendMessage(ChatColor.GRAY + "[ChestIcon]" + ChatColor.RED + " You do not have permission for this command.");
          return true;
        }

        sender.sendMessage(ChatColor.GRAY + "[ChestIcon]" + ChatColor.RED + " Not a valid icon ID.");
        return true;
      }

      sender.sendMessage(ChatColor.GRAY + "[ChestIcon] " + ChatColor.AQUA + "Commands:");
      if (sender.hasPermission("chesticon.admin") || sender.isOp()) {
        sender.sendMessage(ChatColor.RED + "    /chesticon near" + ChatColor.GRAY + " List icons within 5 blocks.");
        sender.sendMessage(ChatColor.RED + "    /chesticon remove [ID]" + ChatColor.GRAY + " Remove an icon.");
      }
      return true;
    }
}
