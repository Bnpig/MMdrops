package guojtim.mmdrops.background;

import guojtim.mmdrops.MMDropItem.PlayerDrops;
import guojtim.mmdrops.MMdrops;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class BackgroundTimer {
    int taskID;
    public BackgroundTimer(MMdrops plugin){
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MMdrops.getPlugin(MMdrops.class),new Runnable() {
            @Override
            public void run() {
                //
                //每 1/4 秒 偵測物品是否需要消失， 在此順便確定 玩家是否撿起物品
                //
                for (Map.Entry<UUID, PlayerDrops> entry : plugin.cache.entrySet()){
                    entry.getValue().check();
                }
            }
        }, 0L, 2L);
    }
    public void onDisable(){
        Bukkit.getScheduler().cancelTask(this.taskID);
    }


}