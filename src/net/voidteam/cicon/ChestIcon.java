package net.voidteam.cicon;

import com.Acrobot.Breeze.Utils.BlockUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Utils.uBlock;
import me.nighteyes604.ItemStay.ItemStay;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * Created by Robby Duke on 1/9/15.
 * Copyright (c) 2015
 */
public class ChestIcon extends JavaPlugin implements Listener {
    private Plugin itemStayPlugin = null;
    private ItemStay itemStay = null;

    @Override
    public void onEnable() {
        /**
         * Let's just check that the dependencies are loaded, even though the plugin.yml should do this!
         */
        itemStayPlugin = getServer().getPluginManager().getPlugin("ItemStay");
        Plugin chestShop = getServer().getPluginManager().getPlugin("ChestShop");

        if (itemStayPlugin instanceof ItemStay && !chestShop.equals(null)) {
            this.getServer().getPluginManager().registerEvents(this, this);
            itemStay = (ItemStay)itemStayPlugin;
        } else {
            Bukkit.getLogger().severe("ItemStay and/or ChestShop not found! Disabling plugin...");
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
                Location displayLocation = event.getContainer().getLocation().add(0, 1, 0);


                String itemCode = event.getSignLine((byte) 3);

                ItemStack displayStack = MaterialUtil.getItem(itemCode);

                /*if( dataInput.length == 2 ) {
                    displayStack.setDurability(Short.valueOf(dataInput[1]));
                }*/

                
                Item displayItem = event.getPlayer().getWorld().dropItem(displayLocation, displayStack);
                displayItem.remove();
                itemStay.registerItem(event.getPlayer().getDisplayName(), displayItem);
            }
        }, 20L);
    }

    @EventHandler
    public void shopDestroy(final ShopDestroyedEvent event) {
        itemStay.deregisterItem(event.getContainer().getLocation().add(0, 1, 0));
    }

    @EventHandler
    public void createChest(final BlockPlaceEvent event) {
        if (BlockUtil.isChest(event.getBlock())) {
            final Sign sign = uBlock.findAnyNearbyShopSign(event.getBlock());
            if (sign != null) {

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.itemStay, new Runnable() {
                    public void run() {
                        Location displayLocation = event.getBlock().getLocation().add(0, 1, 0);


                        String itemCode = sign.getLine(3);

                        ItemStack displayStack = MaterialUtil.getItem(itemCode);

                /*if( dataInput.length == 2 ) {
                    displayStack.setDurability(Short.valueOf(dataInput[1]));
                }*/


                        Item displayItem = event.getPlayer().getWorld().dropItem(displayLocation, displayStack);
                        itemStay.registerItem(event.getPlayer().getDisplayName(), displayItem);
                    }
                }, 20L);
            }
        }
    }

    @EventHandler
    public void chestDestroyed(final BlockBreakEvent event) {
        if(Arrays.asList(event.getBlock().getType().createBlockData().getClass().getInterfaces()).contains(Container.class)){
            Location loc = event.getBlock().getLocation().add(0, 1, 0);
            itemStay.deregisterItem(loc);
        }
    }
}
