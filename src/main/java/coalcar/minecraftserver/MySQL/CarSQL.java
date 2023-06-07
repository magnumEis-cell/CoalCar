package coalcar.minecraftserver.MySQL;

import coalcar.minecraftserver.MinecraftServer;
import org.bukkit.entity.Entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;



public class CarSQL {
    public MinecraftServer plugin;


    public MySQL SQL;
    public CarSQL (MinecraftServer plugin){
        this.plugin = plugin;
    }
    public void setFuel(UUID uuid, int fuel) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE cars SET FUEL=? WHERE UUID=?");
            ps.setInt(1, (fuel));
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public int getFuel(UUID uuid){

        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT FUEL FROM cars WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            int fuel = 0;
            if(rs.next()){
                fuel = rs.getInt("FUEL");
                return fuel;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public float getmovingVec(UUID uuid){

        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT MOVINGVEC FROM cars WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            float movingvec = 0;
            if(rs.next()){
                movingvec = rs.getInt("MOVINGVEC");
                return movingvec;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void setmovingVec(UUID uuid, float movingvec) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE cars SET MOVINGVEC=? WHERE UUID=?");
            ps.setFloat(1, (movingvec));
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void createTable(){
        PreparedStatement ps;
        try {
            ps = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS cars "
                    + "(MOVINGVEC FLOAT(10,7),UUID VARCHAR(100),FUEL INT(100),PRIMARY KEY(UUID))");
            ps.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void createCar(Entity entity ) {
        try {
            UUID uuid = entity.getUniqueId();

            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("INSERT IGNORE INTO cars" +
                    "(MOVINGVEC,UUID,FUEL) VALUES (?,?,?)");
            ps.setFloat(1, entity.getLocation().getYaw());
            ps.setString(2, uuid.toString());
            ps.setInt(3, 0);
            ps.executeUpdate();
            //}
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }








    public boolean exists(UUID uuid){
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM cars WHERE UUID=?");
            ps.setString(2, uuid.toString());

            ResultSet result = ps.executeQuery();
            if(result.next()){
                return true;
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

}
