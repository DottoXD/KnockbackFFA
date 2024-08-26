package me.dotto.CopperKnockbackFFA;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Database {
    static Connection connection;

    public Database(FileConfiguration Config, Logger GetLogger) {
        try {
            String url = "jdbc:mysql://" + Config.getString("database.host") + ":" + Config.getString("database.port") + "/" + Config.getString("database.database") + "?autoReconnect=true";
            connection = DriverManager.getConnection(url, Config.getString("database.username"), Config.getString("database.password"));
            GetLogger.info("Successfully connected to your MySQL database!");
            PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS CopperKnockbackFFA(uuid varchar(36), kills int, deaths int, killstreak int, bounty int);");
            statement.executeUpdate();
        } catch(SQLException error) {
            GetLogger.info("There was an error while connecting to MySQL!");
        }
    }

    void Disconnect() {
        try {
            if (connection!=null && !connection.isClosed()) {
                connection.close();
            }
        } catch(Exception ignored) {}
    }

    static Connection GetConnection() {
        return connection;
    }
}
