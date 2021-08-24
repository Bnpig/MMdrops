package guojtim.mmdrops;

import guojtim.mmdrops.Event.Event;
import guojtim.mmdrops.MMDropItem.PlayerDrops;
import guojtim.mmdrops.background.BackgroundTimer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class MMdrops extends JavaPlugin implements Listener {
    public HashMap<UUID, PlayerDrops> cache = new HashMap<>();
    private Event event;
    BackgroundTimer backgroundTimer;
    @Override
    public void onEnable() {
        event = new Event(this);
        getServer().getPluginManager().registerEvents(event,this);
        backgroundTimer = new BackgroundTimer(this);
    }

    @Override
    public void onDisable() {
        backgroundTimer.onDisable();
    }

    public Event getEventManager(){
        return event;
    }

    public PlayerDrops getPlayerDrops(UUID uuid){
        return this.cache.get(uuid);
    }

    public void createPlayerDrops(Player player){
        this.cache.put(player.getUniqueId(),new PlayerDrops(player,this));
    }

    public void removePlayerDrops(UUID uuid){
        this.cache.remove(uuid);
    }

}
