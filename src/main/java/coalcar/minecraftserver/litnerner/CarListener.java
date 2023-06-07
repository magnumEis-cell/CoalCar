package coalcar.minecraftserver.litnerner;

import coalcar.minecraftserver.MinecraftServer;
import coalcar.minecraftserver.MySQL.CarSQL;
import coalcar.minecraftserver.MySQL.MySQL;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class CarListener implements Listener {
    public MySQL SQL;
    public CarSQL data;
    public MinecraftServer plugin;
    public CarListener(MinecraftServer plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void youWannaManipulate(PlayerArmorStandManipulateEvent e){
        ArmorStand car = e.getRightClicked();
        if(!(car.isInvulnerable()))return;
        e.setCancelled(true);


    }
    /*
    @EventHandler
    public void youWannaLeave(EntityDismountEvent e) {
        boolean jumPing = MinecraftServer.getInstance().jumPing();

        Entity car = e.getDismounted();
        if (car.isInvulnerable()) {
            if (jumPing == false) {
                e.setCancelled(true);
                MinecraftServer.getInstance().ChangeJumping(false);
            }
        }


    }
    /*
       @EventHandler
       public void youWannaDrag(InventoryDragEvent e) {
           Inventory inventory = e.getInventory();
           Inventory playerinventory = e.getWhoClicked().getInventory();
           if (inventory.getTitle().equals("Kohlemotor")) {
               Integer newinteger = e.getNewItems().size();
               Set<Integer> integer = e.getInventorySlots();
               Map<Integer, ItemStack> itemsinteger = e.getNewItems();
               //if(integer.contains(9)) e.setCancelled(true);
               if(!(itemsinteger.containsKey(Material.COAL))) e.setCancelled(true);
               if(!(e.getType().equals(Material.COAL))) e.setCancelled(true);
           }
       }

     */
    @EventHandler
    public void onlyCoal(InventoryClickEvent e){
        if (!(e.getInventory().getTitle().equals("Kohlemotor"))) return;
        if (!(e.getCurrentItem() == null)) {
            Material urPresnt = e.getCurrentItem().getType();
            if (urPresnt == Material.COAL || e.getCursor().getType() == Material.COAL){
                //e.setCancelled(false);
            }else e.setCancelled(true);
        }

    }
    @EventHandler
    public void youWannaDrag(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getTitle().equals("Kohlemotor")) {
            List<String> loreID = new ArrayList<String>(2);
            loreID.add(inv.getItem(8).getLore().get(0));
            UUID uuid = UUID.fromString(loreID.get(0));
            ItemStack[] coals = inv.getContents();
            int Fuel = 0;
            for (int i = 0;i < 8 ; i = i + 1) {
                if (!(inv.getItem(i) == null)) {
                    if (inv.getItem(i).getType() == Material.COAL) {
                        ItemMeta coal = inv.getItem(i).getItemMeta();
                        ItemStack coalStack = inv.getItem(i);
                        Fuel = coalStack.getAmount() + Fuel;
                        //System.out.println(Fuel);
                    }
                }
            }
            plugin.cardata.setFuel(uuid, Fuel);
        }
    }


    /*
   @EventHandler
   public void youWannaDrag(InventoryMoveItemEvent e){
       Inventory inventory = e.getInitiator();
       if (inventory.getTitle().equals("Kohlemotor")) {

       }
   }
   */
    @EventHandler
    public void youWannaDragCoal(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand && e.getRightClicked().isInvulnerable()) {


            World world = e.getPlayer().getWorld();
            Player p = (Player) e.getPlayer();
            if (p.isSneaking() == true) {
                List<String> loreID = new ArrayList<String>(2);
                loreID.add(0, e.getRightClicked().getUniqueId().toString());
                ItemStack IDpaper = new ItemStack(Material.PAPER);
                ItemMeta meta = IDpaper.getItemMeta();
                meta.setDisplayName(ChatColor.GRAY + "CarId");
                meta.setLore(Collections.singletonList(loreID.get(0)));
                IDpaper.setItemMeta(meta);
                Inventory inventory = Bukkit.createInventory(null, 9, "Kohlemotor");
                inventory.setItem(8, IDpaper);
                UUID uuid = e.getRightClicked().getUniqueId();
                int Fuel = plugin.cardata.getFuel(uuid);
                for (int i = 0; i < 8; i++){
                    ItemStack coal = new ItemStack(Material.COAL);
                    if (Fuel > 64) {
                        coal.setAmount(64);
                        inventory.setItem(i, coal);
                        Fuel = Fuel - 64;
                    }else {
                        coal.setAmount(Fuel);
                        inventory.setItem(i, coal);
                        Fuel = Fuel - Fuel;
                    }
                }
                p.openInventory(inventory);
            }
        }
    }


    @EventHandler
    public void youWannaEnter(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand && e.getRightClicked().isInvulnerable()) {
            World world = e.getPlayer().getWorld();
            Player p = (Player) e.getPlayer();

            if (p.isSneaking() == false) {
                ArmorStand car = (ArmorStand) e.getRightClicked();
                UUID uuid = car.getUniqueId();
                if (!(p.getInventory().getItemInMainHand().getType() == Material.IRON_INGOT)) return;
                if ((p.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).equals(car.getUniqueId().toString()))) {
                    //if (!(p.getInventory().getItemInMainHand().equals(stack))) return;
                    ItemStack ingot = p.getInventory().getItemInMainHand();
                    Location woIsn = p.getLocation();
                    double fehler;
                    fehler = 1.5;
                    double x = woIsn.getBlockX();
                    double z = woIsn.getBlockZ();
                    double y = woIsn.getBlockY();
                    Location location = new Location(p.getWorld(), x, y - fehler, z);
                    if ((car.getPassengers().size() == 0)) {
                        car.setPassenger(p);
                        Double yRefiner = car.getLocation().getY();
                        car.getLocation().setY(yRefiner - 1);

                    }
                } else {
                    String actionbarMessage = (String) new String(ChatColor.GRAY + "Bro, u cant enter another car ;-;");
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbarMessage));
                }
            }

        }

    }
}