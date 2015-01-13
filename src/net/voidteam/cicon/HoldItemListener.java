package net.voidteam.cicon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class HoldItemListener
  implements Listener
{
  public static List<HoldItem> HoldItems = new ArrayList<HoldItem>(500);
  public static ChestIcon plugin;

  public HoldItemListener(ChestIcon p)
  {
    plugin = p;
  }

  @EventHandler(priority=EventPriority.HIGH)
  public void itemDespawn(ItemDespawnEvent event) {
    for (HoldItem fi : plugin.HoldItems)
      if (event.getEntity().equals(fi.item)) {
        event.setCancelled(true);
        fi.respawn();
      }
  }

  @EventHandler(priority=EventPriority.HIGH)
  public void itemRespawnOnLoad(ChunkLoadEvent event)
  {
    for (HoldItem fi : plugin.HoldItems)
      if (fi.location.getChunk() == event.getChunk())
        fi.respawn();
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerThrow(final PlayerDropItemEvent event)
  {
    if (plugin.saveNextItem.contains(event.getPlayer().getName().toLowerCase()))
      plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
      {
        public void run()
        {
          Item item = event.getItemDrop();
          Boolean allowed = Boolean.valueOf(true);
          Iterator localIterator2;
          for (Iterator localIterator1 = HoldItemListener.plugin.HoldItems.iterator(); localIterator1.hasNext(); 
            localIterator2.hasNext())
          {
            HoldItem fi = (HoldItem)localIterator1.next();
            localIterator2 = item.getNearbyEntities(0.5D, 0.5D, 0.5D).iterator();
            Entity e = (Entity)localIterator2.next();
            if ((e.getLocation().getBlock().equals(fi.location.getBlock())) && ((e instanceof Item)) && (!e.equals(item))) {
              allowed = Boolean.valueOf(false);
              item.remove();
            }
          }

          if (allowed.booleanValue()) {
            HoldItemListener.plugin.HoldItems.add(new HoldItem(event.getPlayer().getName().toLowerCase(), item.getLocation(), item.getType().name(), item.getItemStack().getType(), item.getItemStack().getDurability()));
            HoldItemListener.plugin.save();

            item.remove();
            HoldItemListener.plugin.getServer().dispatchCommand(event.getPlayer(), "HoldItem reload");
            HoldItemListener.plugin.saveNextItem.remove(event.getPlayer().getName().toLowerCase());
          } else {
            event.getPlayer().sendMessage("[HoldItem]" + ChatColor.GRAY + " There is already an item frozen there.");
          }
        }
      }
      , 20L);
  }

  @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
  public void preventPickup(PlayerPickupItemEvent event)
  {
    if (!event.getItem().getItemStack().hasItemMeta()) {
      return;
    }
    if (!event.getItem().getItemStack().getItemMeta().hasDisplayName()) {
      return;
    }
    if (event.getItem().getItemStack().getItemMeta().getDisplayName().equals("HoldItem")) {
      event.setCancelled(true);
      return;
    }
  }
}