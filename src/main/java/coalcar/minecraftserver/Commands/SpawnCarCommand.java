package coalcar.minecraftserver.Commands;

import coalcar.minecraftserver.MinecraftServer;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class SpawnCarCommand implements CommandExecutor {

    private MinecraftServer plugin;
    public SpawnCarCommand (MinecraftServer plugin){
        this.plugin = plugin;
    }
    private final Method[] methods = ((Supplier<Method[]>) () -> {
        try {
            Method getHandle = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftEntity").getDeclaredMethod("getHandle");
            return new Method[] {
                    getHandle, getHandle.getReturnType().getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class)
            };
        } catch (Exception ex) {
            return null;
        }
    }).get();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if (!(sender instanceof Player)) {
            sender.sendMessage("U ARE A RROBOT");
        } else {
            PreparedStatement ps;
            try {
                ps = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cars "
                        + "(MOVINGVEC FLOAT(10,7),UUID VARCHAR(100),FUEL INT(100),PRIMARY KEY(UUID))");
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            Location woIsn = p.getLocation();

            double x = woIsn.getBlockX();
            double z = woIsn.getBlockZ();
            double y = woIsn.getBlockY();
            Location location = new Location(p.getWorld(), x, y, z);
            World world = p.getWorld();


            ArmorStand car = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
            ItemStack sword = new ItemStack(Material.IRON_SWORD);
            ItemMeta swordmeta = sword.getItemMeta();
            short lol = 2;
            sword.setDurability(lol);
            car.setGravity(true);
            car.setVisible(false);
            car.setInvulnerable(true);
            car.setItemInHand(sword);
            car.setCustomName(p.getDisplayName().toString() +"`s"+ " Car");
            car.setCustomNameVisible(true);
            /*
            try {
                methods[1].invoke(methods[0].invoke(car), location);
            } catch (Exception ex) {
            }

             */

            ItemStack Carkey = new ItemStack(Material.IRON_INGOT);
            ItemMeta meta = Carkey.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + p.getDisplayName() + "`s Carkey");
            //StringList loreID = null;
            List<String> loreID = new ArrayList<String>(2);
            //List loreID = new ArrayList();
            loreID.add(0, car.getUniqueId().toString());
            meta.setLore(Collections.singletonList(loreID.get(0).toString()));
            Carkey.setItemMeta(meta);
            p.getInventory().addItem(Carkey);
            //plugin.cardata.createTable();
            plugin.cardata.createCar(car);
        }
        return false;
    }
}