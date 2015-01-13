package net.voidteam.cicon;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class HoldItem
{
  public static ChestIcon plugin;
  public String owner;
  public Item item;
  public Location location;
  public String type;
  public Material material;
  public short data;
  public boolean requiresUpdate = true;

  public HoldItem(ChestIcon p)
  {
    plugin = p;
  }

  public HoldItem(String player, Location loc, String t, Material mat, short d)
  {
    this.owner = player.toLowerCase();
    this.location = new Location(loc.getWorld(), loc.getBlockX() + 0.5D, loc.getBlockY() + 0.9D, loc.getBlockZ() + 0.5D);
    this.type = t;
    this.material = mat;
    this.data = d;

    respawn();
  }

  public void respawn()
  {
    if (this.location.getChunk().isLoaded())
      if (this.item != null) {
        if (this.item.isDead()) {
          if (this.location.getChunk().isLoaded())
          {
            ItemStack stack = new ItemStack(this.material, 1, this.data);
            this.item = this.location.getWorld().dropItem(this.location, stack);

            ItemMeta m = this.item.getItemStack().getItemMeta();
            m.setDisplayName("ChestIcon");
            this.item.getItemStack().setItemMeta(m);

            this.item.setVelocity(new Vector(0, 0, 0));
            removeDuplicateItems();
          }
        }
        else
        {
          destroy();
          if (this.location.getChunk().isLoaded())
          {
            ItemStack stack = new ItemStack(this.material, 1, this.data);
            this.item = this.location.getWorld().dropItem(this.location, stack);

            ItemMeta m = this.item.getItemStack().getItemMeta();
            m.setDisplayName("ChestIcon");
            this.item.getItemStack().setItemMeta(m);

            this.item.setVelocity(new Vector(0, 0, 0));
            removeDuplicateItems();
          }
        }

      }
      else if (this.location.getChunk().isLoaded())
      {
        ItemStack stack = new ItemStack(this.material, 1, this.data);
        this.item = this.location.getWorld().dropItem(this.location, stack);

        ItemMeta m = this.item.getItemStack().getItemMeta();
        m.setDisplayName("ChestIcon");
        this.item.getItemStack().setItemMeta(m);

        this.item.setVelocity(new Vector(0, 0, 0));
        removeDuplicateItems();
      }
  }

  public void destroy()
  {
    removeDuplicateItems();
    this.item.remove();
  }

  private void removeDuplicateItems() {
    for (Entity e : this.item.getNearbyEntities(0.2D, 0.2D, 0.2D))
      if ((e.getLocation().getBlock().equals(this.location.getBlock())) && ((e instanceof Item)) && (!e.equals(this.item)))
        e.remove();
  }
}