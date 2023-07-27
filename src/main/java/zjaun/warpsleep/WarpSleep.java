package zjaun.warpsleep;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class WarpSleep extends JavaPlugin implements Listener {

    String result = "";
    String worldName = "";
    Boolean warpSleepActivated = false;
    BukkitTask warp;
    short ticks = 0;
    long currentTime;
    Player sleepy = null;
    BossBar notif = null;
    int ticksSlept = 0;
    Component message;

    @EventHandler
    public void bedEnterEvent(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        BedEnterResult result = event.getBedEnterResult();
        switch (result) {
            case OK:
                if (!warpSleepActivated) {
                    warpSleep();
                    sleepy = player;
                }
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void bedLeaveEvent(PlayerBedLeaveEvent event) {
        Player awake = event.getPlayer();
        if (warp != null && (awake == sleepy)) {
            World world = Bukkit.getWorld(worldName);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            Sound sound = Sound.sound(Key.key("block.portal.trigger"), Sound.Source.AMBIENT, 1f, 1f);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.stopSound(sound);
                notif.removeViewer(player);
            }
            warp.cancel();
            warp = null;
            warpSleepActivated = false;
            ticks = 0;
        }
    }

    private List<NamedTextColor> textColors() {
        List<NamedTextColor> colorList = new ArrayList<>();
        colorList.add(NamedTextColor.AQUA);
        colorList.add(NamedTextColor.BLUE);
        colorList.add(NamedTextColor.GOLD);
        colorList.add(NamedTextColor.GREEN);
        colorList.add(NamedTextColor.RED);
        colorList.add(NamedTextColor.YELLOW);
        colorList.add(NamedTextColor.LIGHT_PURPLE);
        colorList.add(NamedTextColor.DARK_PURPLE);
        colorList.add(NamedTextColor.DARK_GREEN);
        colorList.add(NamedTextColor.DARK_AQUA);
        return colorList;
    }

    private List<BossBar.Color> bossBarColors() {
        List<BossBar.Color> colorList = new ArrayList<>();
        colorList.add(BossBar.Color.BLUE);
        colorList.add(BossBar.Color.GREEN);
        colorList.add(BossBar.Color.PINK);
        colorList.add(BossBar.Color.PURPLE);
        colorList.add(BossBar.Color.RED);
        colorList.add(BossBar.Color.YELLOW);
        return colorList;
    }
    
    private void warpSleep() {
        warpSleepActivated = true;
        World world = Bukkit.getWorld(worldName);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        currentTime = world.getTime();
        long warpedTime = 24000 - currentTime;
        long addTime = warpedTime / 100;
        message = Component.text("COMMENCING WARPSLEEP", NamedTextColor.AQUA, TextDecoration.BOLD);
        notif = BossBar.bossBar(message, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
        Sound sound = Sound.sound(Key.key("block.portal.trigger"), Sound.Source.AMBIENT, 1f, 1f);
        for (Player player : Bukkit.getOnlinePlayers()) {
            String playerWorld = player.getWorld().getName();
            if (playerWorld.equals(worldName)) {
                player.playSound(sound);
                player.sendActionBar(message);
                notif.addViewer(player);
            }
        }
        warp = Bukkit.getScheduler().runTaskTimer(this, () -> {
            Random random = new Random();
            List<NamedTextColor> textColors = textColors();
            List<BossBar.Color> bossBarColors = bossBarColors();
            NamedTextColor textColor = textColors.get(random.nextInt(10));
            BossBar.Color bossBarColor = bossBarColors.get(random.nextInt(6));
            message = Component.text("COMMENCING WARPSLEEP", textColor, TextDecoration.BOLD);
            notif.name(message);
            notif.color(bossBarColor);
            currentTime += addTime;
            world.setTime(currentTime);
            ticks++;
            if (ticks == 100) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.stopSound(sound);
                    notif.removeViewer(player);
                }
                warp.cancel();
                warp = null;
                notif = null;
                warpSleepActivated = false;
                ticks = 0;
                if (currentTime < 24000) {
                    currentTime += (24000 - currentTime);
                    world.setTime(currentTime);
                }
                world.setStorm(false);
                world.setThundering(false);
            }
        }, 0L, 1L);
    }
    
    @Override
    public void onEnable() {
        try {
            FileInputStream input = new FileInputStream("server.properties");
            Properties props = new Properties();
            props.load(input);
            worldName = props.getProperty("level-name");
            input.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("WarpSleep Loaded");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling WarpSleep...");
    }
}
