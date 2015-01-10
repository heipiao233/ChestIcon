package net.voidteam.cicon;

import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import me.nighteyes604.ItemStay.FrozenItem;
import me.nighteyes604.ItemStay.ItemStayListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

/**
 * Created by Robby Duke on 1/9/15.
 * Copyright (c) 2015
 */
public class ChestIcon extends JavaPlugin implements Listener {
    private Plugin itemStay = null;

    @Override
    public void onEnable() {
        /**
         * Let's just check that the dependencies are loaded, even though the plugin.yml should do this!
         */
        itemStay = getServer().getPluginManager().getPlugin("ItemStay");
        Plugin chestShop = getServer().getPluginManager().getPlugin("ChestShop");

        if (!itemStay.equals(null) && !itemStay.equals(null)) {
            this.getServer().getPluginManager().registerEvents(this, this);
        } else {
            Bukkit.getLogger().severe("ItemStay/ChestShop not found! Disabling plugin...");
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void shopCreate(final ShopCreatedEvent event) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.itemStay, new Runnable() {
            public void run() {
                Location displayLocation = event.getChest().getLocation().add(0, 1, 0);

                ItemStack displayStack = new ItemStack(Material.matchMaterial(event.getSignLine((short) 3)));
                Item displayItem = event.getPlayer().getWorld().dropItem(displayLocation, displayStack);

                Boolean allowed = true;

                for (int x = 0; x < ItemStayListener.plugin.frozenItems.size(); x++) {
                    FrozenItem frozenItem = ItemStayListener.plugin.frozenItems.get(x);
                    Iterator entitiesIterator = displayItem.getNearbyEntities(0.5D, 0.5D, 0.5D).iterator();

                    while (entitiesIterator.hasNext()) {
                        Entity entity = (Entity) entitiesIterator.next();
                        if (entity.getLocation().getBlock().equals(frozenItem.location.getBlock()) && entity instanceof Item && !entity.equals(displayItem)) {
                            allowed = false;
                            displayItem.remove();
                        }
                    }
                }

                if (allowed.booleanValue()) {
                    ItemStayListener.plugin.frozenItems.add(
                            new FrozenItem(event.getPlayer().getName().toLowerCase(),
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
        for (int i = 0; i < ItemStayListener.plugin.frozenItems.size(); i++) {
            FrozenItem frozenItem = (FrozenItem) ItemStayListener.plugin.frozenItems.get(i);

            if (event.getSign().getType() == Material.SIGN_POST || event.getSign().getType() == Material.WALL_SIGN) {
                Sign s = (Sign) event.getSign().getBlock().getState().getData();
                Block attachedBlock = event.getSign().getBlock().getRelative(s.getAttachedFace());

                Location chestLocation = attachedBlock.getLocation().add(0.5, 1.9, 0.5);
                if (frozenItem.location.equals(chestLocation)) {
                    frozenItem.destroy();
                    ItemStayListener.plugin.frozenItems.remove(ItemStayListener.plugin.frozenItems.get(i));
                }
            }
        }
    }
}
