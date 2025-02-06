package me.dotto.CopperKnockbackFFA;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class KnockbackFFAListener implements Listener {
    FileConfiguration config;
    Plugin plugin;

    private final HashMap<String, String> lastDamager = new HashMap<>();
    private final List<String> maps;
    private int currentMapIndex = 0;
    private String currentMap;
    int mapChangeSeconds;


    public int GetMapChangeSeconds() {
        return mapChangeSeconds;
    }

    public KnockbackFFAListener(FileConfiguration Config, Plugin Plugin) {
        config = Config;
        plugin = Plugin;

        maps = config.getStringList("maps");
        mapChangeSeconds = config.getInt("misc.switchtime");
        currentMap = maps.get(currentMapIndex);

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                mapChangeSeconds--;
                if(mapChangeSeconds == 0) mapChangeSeconds = config.getInt("misc.switchtime");
            }
        }, 20L, 20L);

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(currentMapIndex + 1 == maps.size()) {
                    currentMapIndex = 0;
                } else {
                    currentMapIndex++;
                }

                currentMap = maps.get(currentMapIndex);
                Bukkit.getServer().broadcastMessage(config.getString("messages.prefix").replace("&", "§") + config.getString("messages.mapswitch").replace("&", "§"));
                for(Player player: Bukkit.getServer().getOnlinePlayers()) {
                    World currentWorld = Bukkit.getServer().getWorld(currentMap);
                    Location currentWorldLocation = new Location(currentWorld, config.getInt("spawn.x"), config.getInt("spawn.y"), config.getInt("spawn.z"));

                    player.teleport(currentWorldLocation);
                }
            }
        }, 20L * config.getInt("misc.switchtime"), 20L * config.getInt("misc.switchtime"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                World currentWorld = Bukkit.getServer().getWorld(currentMap);
                Location currentWorldLocation = new Location(currentWorld, config.getInt("spawn.x"), config.getInt("spawn.y"), config.getInt("spawn.z"));
                event.getPlayer().teleport(currentWorldLocation);
            }
        }, 10L);

        event.setJoinMessage(config.getString("messages.join").replace("%player%", event.getPlayer().getName()).replace("&", "§"));

        event.getPlayer().getInventory().clear();
        event.getPlayer().setGameMode(GameMode.SURVIVAL);

        if(!event.getPlayer().getInventory().contains(new ItemStack(Material.STICK, 1))) {
            ItemStack Stick = new ItemStack(Material.STICK, 1);
            ItemMeta StickMeta = Stick.getItemMeta();
            StickMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            StickMeta.setDisplayName(config.getString("itemnames.knockbackstick").replace("&", "§"));
            Stick.setItemMeta(StickMeta);

            ItemStack EnderPearl = new ItemStack(Material.ENDER_PEARL, config.getInt("items.enderpearls"));
            ItemMeta EnderPearlMeta = EnderPearl.getItemMeta();
            EnderPearlMeta.setDisplayName(config.getString("itemnames.enderpearls").replace("&", "§"));
            EnderPearl.setItemMeta(EnderPearlMeta);

            ItemStack SandStone = new ItemStack(Material.SANDSTONE, config.getInt("items.blocks"));
            ItemMeta SandStoneMeta = SandStone.getItemMeta();
            SandStoneMeta.setDisplayName(config.getString("itemnames.blocks").replace("&", "§"));
            SandStone.setItemMeta(SandStoneMeta);

            ItemStack Speed = new ItemStack(Material.FEATHER, 1);
            ItemMeta SpeedMeta = Speed.getItemMeta();
            SpeedMeta.setDisplayName(config.getString("itemnames.speedboost").replace("&", "§"));
            Speed.setItemMeta(SpeedMeta);

            ItemStack JumpPad = new ItemStack(Material.SLIME_BALL, 1);
            ItemMeta JumpPadMeta = JumpPad.getItemMeta();
            JumpPadMeta.setDisplayName(config.getString("itemnames.booster").replace("&", "§"));
            JumpPad.setItemMeta(JumpPadMeta);

            event.getPlayer().getInventory().addItem(Stick, EnderPearl, SandStone, Speed, JumpPad);
        }

        PreparedStatement statement = Database.GetConnection().prepareStatement("SELECT * FROM CopperKnockbackFFA WHERE uuid=(?);");
        statement.setString(1, String.valueOf(event.getPlayer().getUniqueId()));
        ResultSet results = statement.executeQuery();

        if(!results.next()) {
            PreparedStatement insertStatement = Database.GetConnection().prepareStatement("INSERT INTO CopperKnockbackFFA(uuid, kills, deaths, killstreak, bounty) VALUES(?, ?, ?, ?, ?);");
            insertStatement.setString(1, String.valueOf(event.getPlayer().getUniqueId()));
            insertStatement.setInt(2, 0);
            insertStatement.setInt(3, 0);
            insertStatement.setInt(4, 0);
            insertStatement.setInt(5, 0);

            insertStatement.executeUpdate();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(config.getString("messages.leave").replace("%player%", event.getPlayer().getName()).replace("&", "§"));
    }

    @EventHandler
    public void onHungerDeplete(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getPlayer().getLocation().getBlockY() > config.getInt("spawn.y") - 5) {
            event.setCancelled(true);
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    event.getBlock().setType(Material.RED_SANDSTONE);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            event.getBlock().setType(Material.AIR);

                            event.getPlayer().getWorld().playEffect(event.getBlock().getLocation(), Effect.SPELL, Integer.MAX_VALUE);
                            event.getPlayer().getWorld().playEffect(event.getBlock().getLocation(), Effect.SPELL, Integer.MAX_VALUE);

                            ItemStack SandStone = new ItemStack(Material.SANDSTONE, 1);
                            ItemMeta SandStoneMeta = SandStone.getItemMeta();
                            SandStoneMeta.setDisplayName(config.getString("itemnames.blocks").replace("&", "§"));
                            SandStone.setItemMeta(SandStoneMeta);

                            if(!event.getPlayer().getInventory().containsAtLeast(SandStone, config.getInt("items.blocks"))) {
                                event.getPlayer().getInventory().addItem(SandStone);
                            }
                        }
                    }, 40L);
                }
            }, 40L);
        }
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(player.getItemInHand().getType() == Material.SLIME_BALL) {
            Vector playerDirection = player.getLocation().getDirection().multiply(4);
            playerDirection.setY(playerDirection.getY() + 1);
            player.setVelocity(playerDirection);
            player.getInventory().remove(Material.SLIME_BALL);
            player.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1F, 1F);
        } else if(player.getItemInHand().getType() == Material.FEATHER) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
            player.getInventory().remove(Material.FEATHER);
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 1F);
        }
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent event) throws SQLException {
        Player player = (Player) event.getEntity();
        if(EntityDamageEvent.DamageCause.VOID == event.getCause()) {
            if(lastDamager.get(event.getEntity().getName()) != null && player.getServer().getPlayer(lastDamager.get(event.getEntity().getName())) != null) {
                Bukkit.getServer().broadcastMessage(config.getString("messages.prefix").replace("&", "§") + config.getString("messages.kill").replace("%player%", player.getName()).replace("%attacker%", lastDamager.get(event.getEntity().getName())).replace("&", "§"));

                PreparedStatement firstStatement = Database.GetConnection().prepareStatement("UPDATE CopperKnockbackFFA SET deaths = deaths + 1, killstreak = 0, bounty = 0 WHERE uuid=(?);");
                firstStatement.setString(1, String.valueOf(player.getUniqueId()));
                firstStatement.executeUpdate();

                PreparedStatement secondStatement = Database.GetConnection().prepareStatement("UPDATE CopperKnockbackFFA SET kills = kills + 1, killstreak = killstreak + 1 WHERE uuid=(?);");
                secondStatement.setString(1, String.valueOf(player.getServer().getPlayer(lastDamager.get(event.getEntity().getName())).getUniqueId()));
                secondStatement.executeUpdate();

                ItemStack EnderPearl = new ItemStack(Material.ENDER_PEARL, config.getInt("items.enderpearls"));
                ItemMeta EnderPearlMeta = EnderPearl.getItemMeta();
                EnderPearlMeta.setDisplayName(config.getString("itemnames.enderpearls").replace("&", "§"));
                EnderPearl.setItemMeta(EnderPearlMeta);

                player.getServer().getPlayer(lastDamager.get(player.getName())).getInventory().addItem(EnderPearl);
            } else {
                Bukkit.getServer().broadcastMessage(config.getString("messages.prefix").replace("&", "§") + config.getString("messages.death").replace("%player%", player.getName()).replace("&", "§"));

                PreparedStatement statement = Database.GetConnection().prepareStatement("UPDATE CopperKnockbackFFA SET deaths = deaths + 1, killstreak = 0, bounty = 0 WHERE uuid=(?);");
                statement.setString(1, String.valueOf(player.getUniqueId()));
                statement.executeUpdate();
            }

            lastDamager.remove(player.getName());

            event.setCancelled(true);
            player.setHealth(20);
            player.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
            player.setFallDistance(0.0F);

            player.sendTitle(config.getString("messages.deathtitle").replace("&", "§"), config.getString("messages.deathsubtitle").replace("&", "§"));

            player.getInventory().clear();

            World currentWorld = Bukkit.getServer().getWorld(currentMap);
            Location currentWorldLocation = new Location(currentWorld, config.getInt("spawn.x"), config.getInt("spawn.y"), config.getInt("spawn.z"));

            player.teleport(currentWorldLocation);

            player.setGameMode(GameMode.SURVIVAL);

            ItemStack Stick = new ItemStack(Material.STICK, 1);
            ItemMeta StickMeta = Stick.getItemMeta();
            StickMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            StickMeta.setDisplayName(config.getString("itemnames.knockbackstick").replace("&", "§"));
            Stick.setItemMeta(StickMeta);

            ItemStack EnderPearl = new ItemStack(Material.ENDER_PEARL, config.getInt("items.enderpearls"));
            ItemMeta EnderPearlMeta = EnderPearl.getItemMeta();
            EnderPearlMeta.setDisplayName(config.getString("itemnames.enderpearls").replace("&", "§"));
            EnderPearl.setItemMeta(EnderPearlMeta);

            ItemStack SandStone = new ItemStack(Material.SANDSTONE, config.getInt("items.blocks"));
            ItemMeta SandStoneMeta = SandStone.getItemMeta();
            SandStoneMeta.setDisplayName(config.getString("itemnames.blocks").replace("&", "§"));
            SandStone.setItemMeta(SandStoneMeta);

            ItemStack Speed = new ItemStack(Material.FEATHER, 1);
            ItemMeta SpeedMeta = Speed.getItemMeta();
            SpeedMeta.setDisplayName(config.getString("itemnames.speedboost").replace("&", "§"));
            Speed.setItemMeta(SpeedMeta);

            ItemStack JumpPad = new ItemStack(Material.SLIME_BALL, 1);
            ItemMeta JumpPadMeta = JumpPad.getItemMeta();
            JumpPadMeta.setDisplayName(config.getString("itemnames.booster").replace("&", "§"));
            JumpPad.setItemMeta(JumpPadMeta);

            player.getInventory().addItem(Stick, EnderPearl, SandStone, Speed, JumpPad);
        } else if(EntityDamageEvent.DamageCause.DROWNING == event.getCause()) {
            event.setCancelled(true);
        } else {
            if(!config.getBoolean("misc.falldamage")) {
                if(event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    player.setFallDistance(0);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void EntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity().getLocation().getBlockY() > config.getInt("spawn.y") - 5) {
            event.setCancelled(true);
        } else {
            if(event.getDamager() != null && event.getEntity().getServer().getPlayer(event.getDamager().getName()) != null) {
                if(lastDamager.containsKey(event.getEntity().getName())) {
                    lastDamager.replace(event.getEntity().getName(), event.getDamager().getName());
                } else {
                    lastDamager.put(event.getEntity().getName(), event.getDamager().getName());
                }
                event.getEntity().getWorld().playEffect(event.getEntity().getLocation(), Effect.FLAME, 2);
                event.getEntity().getServer().getPlayer(event.getEntity().getName()).setHealth(20);
            }
        }
    }
}
