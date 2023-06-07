package coalcar.minecraftserver;


import coalcar.minecraftserver.Commands.SpawnCarCommand;
import coalcar.minecraftserver.MySQL.CarSQL;
import coalcar.minecraftserver.MySQL.MySQL;
import coalcar.minecraftserver.litnerner.CarListener;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class MinecraftServer extends JavaPlugin {
    public MySQL SQL;
    public MinecraftServer plugin;
    public SpawnCarCommand data;
    public CarSQL cardata;
    Vector movingVec = null;
    boolean jumPing;
    private static MinecraftServer instance;
    public static MinecraftServer getInstance(){
        return instance;

    }    public boolean jumPing(){
        return jumPing;
    }
    public void ChangeJumping( boolean change ) {
        jumPing = change;

    }
    @Override
    public void onLoad(){
        instance = this;
    }


    ///AutoZeugs///
    //Wichtig um den das Vehicel zu teleportieren obwohl es einen passenger hat
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

    //Damit der movingVector langsam nach zieht
    public Vector getCorrectVec(Vector vec, UUID uuid,Location locar,Location movingLoc){
        ArmorStand aS = (ArmorStand) Bukkit.getServer().getEntity(uuid);
        if (movingLoc.getYaw() < locar.getYaw()){
            movingLoc.setYaw(movingLoc.getYaw()+ 5);
            cardata.setmovingVec(uuid, movingLoc.getYaw());
            vec = movingLoc.getDirection();
            return vec;
        }
        if (movingLoc.getYaw() > locar.getYaw()){
            movingLoc.setYaw(movingLoc.getYaw()- 5);
            cardata.setmovingVec(uuid, movingLoc.getYaw());
            vec = movingLoc.getDirection();
            return vec;
        }
        return vec;
    }

    PluginManager pluginmanager = Bukkit.getPluginManager();

    @Override
    public void onEnable() {
        this.getCommand("SpawnCar").setExecutor(new SpawnCarCommand(this));
        pluginmanager.registerEvents(new CarListener(this), this);
        this.SQL = new MySQL();
        this.cardata = new CarSQL(this);
        //MySQL Zeugs
        //credits an ChatGpT fuer das gedicht
        try {
            SQL.connect();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            Bukkit.getLogger().info(
                    "A database that's not working can be such a pain,\n" +
                            "Data missing or corrupted, driving you insane,\n" +
                            "Queries returning errors, tables hard to find,\n" +
                            "Oh, how frustrating when it's all in a bind.");
        }
        if (SQL.isConnected()) {
            cardata.createTable();

            Bukkit.getLogger().info(
                    "A database that's working is a beautiful thing,\n" +
                            "Organizing information with a digital zing,\n" +
                            "Efficiently storing, retrieving with ease,\n" +
                            "Ensuring accuracy with every new piece.\n");

        }
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
                @Override
                public void onPacketReceiving(PacketEvent e) {
                    PacketContainer packet = e.getPacket();
                    Player p = e.getPlayer();

                    float sideways = packet.getFloat().readSafely(0);
                    float forward = packet.getFloat().readSafely(1);
                    jumPing = packet.getBooleans().readSafely(0);
                    boolean shifTing = packet.getBooleans().readSafely(1);

                    if (shifTing == true) return;
                    if (e.getPlayer().getVehicle().isInvulnerable()) {
                        ArmorStand armorStand = (ArmorStand) p.getVehicle();
                        if (armorStand.getPassenger().equals(e.getPlayer())) {
                            ArmorStand car = (ArmorStand) p.getVehicle();

                            //checken ob cas vehicle auch am boden ist
                            if(!(car.isOnGround())) return;
                            float yawAddition = 10 ;

                            int Fuel = cardata.getFuel(car.getUniqueId());
                            //Fuel anzeigen
                            String actionbarMessage = (String) new String(ChatColor.GRAY + "FUEL:" + ChatColor.AQUA + Fuel);
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbarMessage));

                            //Vector movingVec = new Vector(car.getLocation().getX(), car.getLocation().getY(), car.getLocation().getZ());
                            //Das zeug sollte klarstellen das man sich nur drhen kann wenn man velocity hat ,hat aber im prozess gestoert weswegen ich noch kein ersatz hab D:
                            /*
                            double velocityInChange = 0;
                            if (car.getVelocity().getX() > 0) {
                                velocityInChange = velocityInChange + car.getVelocity().getY();
                                //System.out.println("Cahnge X>0:"+ velocityInChange);
                            }
                            if (car.getVelocity().getX() < 0) {
                                velocityInChange = velocityInChange + car.getVelocity().getY();
                                velocityInChange = velocityInChange * -1;
                                //System.out.println("Cahnge X<0:"+ velocityInChange);
                            }
                            if (car.getVelocity().getZ() > 0) {
                                velocityInChange = velocityInChange + car.getVelocity().getY();
                                //System.out.println("Cahnge Z>0:"+ velocityInChange);
                            }
                            if (car.getVelocity().getZ() < 0) {
                                velocityInChange = velocityInChange + car.getVelocity().getY();
                                velocityInChange = velocityInChange * -1;
                                //System.out.println("Cahnge Z<0:"+ velocityInChange);
                            }
                            float yawAdditonPaste = (float) velocityInChange;
                            yawAdditonPaste = yawAdditonPaste * 10;
                            yawAddition = yawAddition * yawAdditonPaste;

                             */

                            double x = car.getLocation().getX();
                            double y = car.getLocation().getY();
                            double z = car.getLocation().getZ();

                            //Offen fuer schlatung oder stoppen wenn man den
                            if(shifTing == true) {

                                car.setVelocity(car.getVelocity().multiply(0.3));
                            }
                            //velocity am leben halten
                            if (yawAddition > 10 && shifTing == false){
                                yawAddition = yawAddition * 0.2F;
                            }
                            //falls ich mich entscheide das leaven des autos ueber leerzeichen zu machen
                            /*
                            if (jumPing == true ){
                                car.eject();
                                car.removePassenger(p);
                                p.leaveVehicle();

                            }

                             */

                            //nach vorne
                            if (forward > 0) {
                                if(!(car.isOnGround())) return;
                                if(Fuel < 0) return;
                                if(Fuel == 0) return;
                                cardata.setFuel(car.getUniqueId(), Fuel - 1);

                                World world = p.getWorld();
                                Location movingVecLoc = p.getVehicle().getLocation();
                                movingVecLoc.setYaw(cardata.getmovingVec(p.getUniqueId()));
                                //getNiceYaw(movingYaw);
                                //cardata.setmovingVec(car.getUniqueId(), car.getLocation().getYaw());
                                movingVec = new Location(world, movingVecLoc.getX(), movingVecLoc.getX(), movingVecLoc.getX() , cardata.getmovingVec(car.getUniqueId()), movingVecLoc.getPitch()).getDirection();
                                Location movingLoc = new Location(world, movingVecLoc.getX(), movingVecLoc.getX(), movingVecLoc.getX() , cardata.getmovingVec(car.getUniqueId()), movingVecLoc.getPitch());
                                //if(!(getLookAtYaw(movingVec) == cardata.getmovingVec(car.getUniqueId()))) System.console().printf("wtf");
                                /*
                                movingVec = getCorrectVec(movingVec, car.getUniqueId());
                                movingVec = getCorrectVec(movingVec, car.getUniqueId() );
                                movingVec = getCorrectVec(movingVec, car.getUniqueId() );
                                */
                                getCorrectVec(movingVec, car.getUniqueId(), car.getLocation(), movingLoc);

                                car.setVelocity(movingVec.add(car.getVelocity()).multiply(0.5F));

                            }


                            //nach hinten
                            if (forward < 0) {
                                if(Fuel < 0) return;
                                if(Fuel == 0) return;
                                if(!(car.isOnGround())) return;
                                cardata.setFuel(car.getUniqueId(), Fuel - 1);
                                World world = p.getWorld();
                                Location movingVecLoc = p.getVehicle().getLocation();
                                movingVecLoc.setYaw(cardata.getmovingVec(p.getUniqueId()));
                                //cardata.setmovingVec(car.getUniqueId(), car.getLocation().getYaw());
                                movingVec = new Location(world, movingVecLoc.getX(), movingVecLoc.getX(), movingVecLoc.getX() , cardata.getmovingVec(car.getUniqueId()), movingVecLoc.getPitch()).getDirection();
                                Location movingLoc = new Location(world, movingVecLoc.getX(), movingVecLoc.getX(), movingVecLoc.getX() , cardata.getmovingVec(car.getUniqueId()), movingVecLoc.getPitch());
                                getCorrectVec(movingVec, car.getUniqueId(), car.getLocation(), movingLoc);
                                car.setVelocity(movingVec.multiply(-0.3F));


                                //bewegen nach links
                            }
                            if (sideways < 0) {
                                if(!(car.isOnGround())) return;
                                float yaw = (float) car.getLocation().getYaw() + yawAddition ;
                                float pitch = (float) car.getLocation().getPitch();
                                Vector vec1 = car.getVelocity().multiply(0);
                                Location movingVecLoc = p.getVehicle().getLocation();
                                movingVecLoc.setYaw(cardata.getmovingVec(p.getUniqueId()));
                                if (car.getVelocity().equals(vec1)) return;
                                try {
                                    methods[1].invoke(methods[0].invoke(car), x,y,z,yaw,pitch);
                                } catch (Exception ex) {
                                }


                            }
                            //bewegen nach rechts
                            if (sideways > 0) {
                                if(!(car.isOnGround())) return;
                                //if (shifTing == true ) return ;
                                float yaw = (float) car.getLocation().getYaw() - yawAddition;
                                float pitch = (float) car.getLocation().getPitch();
                                Vector vec1 = car.getVelocity().multiply(0);
                                if (car.getVelocity().equals(vec1)) return;
                                Location movingVecLoc = p.getVehicle().getLocation();
                                movingVecLoc.setYaw(cardata.getmovingVec(p.getUniqueId()));
                                if (car.getVelocity().equals(vec1)) return;
                                try {
                                    methods[1].invoke(methods[0].invoke(car), x,y,z,yaw,pitch);
                                } catch (Exception ex) {
                                }

                            }
                        }

                    }
                }
            });
        }
    @Override
    public void onDisable() {
        SQL.disconnect();
    }

}

