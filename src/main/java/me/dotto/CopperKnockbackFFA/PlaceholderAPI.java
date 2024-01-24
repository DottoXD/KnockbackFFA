package me.dotto.CopperKnockbackFFA;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaceholderAPI extends PlaceholderExpansion {
    private final KnockbackFFA plugin;
    private KnockbackFFAListener listener;

    public PlaceholderAPI(KnockbackFFA Plugin, KnockbackFFAListener Listener) {
        this.plugin = Plugin;
        this.listener = Listener;
    }

    @Override
    public String getAuthor() {
        return "Dotto";
    }

    @Override
    public String getIdentifier() {
        return "copperknockbackffa";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player Player, String params) {
        if(params.equalsIgnoreCase("kills")) {
            try {
                ResultSet Results = GetPlayerData(Player);
                while (Results.next()) {
                    return String.valueOf(Results.getInt(2));
                }
                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if(params.equalsIgnoreCase("deaths")) {
            try {
                ResultSet Results = GetPlayerData(Player);
                while (Results.next()) {
                    return String.valueOf(Results.getInt(3));
                }
                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if(params.equalsIgnoreCase("killstreak")) {
            try {
                ResultSet Results = GetPlayerData(Player);
                while (Results.next()) {
                    return String.valueOf(Results.getInt(4));
                }
                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if(params.equalsIgnoreCase("bounty")) {
            try {
                ResultSet Results = GetPlayerData(Player);
                while (Results.next()) {
                    return String.valueOf(Results.getInt(5));
                }
                return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if(params.equalsIgnoreCase("switchtime")) {
            int Seconds = listener.GetMapChangeSeconds();
            int Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            String FinalSeconds = String.valueOf(Seconds);
            if(Seconds < 10) FinalSeconds = "0" + Seconds;

            return Minutes + ":" + FinalSeconds;
        }

        return null;
    }

    ResultSet GetPlayerData(Player Player) throws SQLException {
        PreparedStatement statement = Database.GetConnection().prepareStatement("SELECT * FROM CopperKnockbackFFA WHERE uuid=(?);");
        statement.setString(1, String.valueOf(Player.getUniqueId()));
        ResultSet results = statement.executeQuery();

        return results;
    }
}
