package guojtim.mmdrops.Event;

import guojtim.mmdrops.MMDropItem.PlayerDrops;
import guojtim.mmdrops.MMdrops;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDespawnEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Event implements Listener {
    /**
     * <FallingBlock UUID>,<PLAYER UUID>
     */
    private HashMap<UUID,UUID> trans = new HashMap<>();
    private HashMap<UUID, Set<UUID>> killer = new HashMap<>();
    private MMdrops plugin;
    public Event(MMdrops plugin){
        //instance
        this.plugin = plugin;
    }

    /**
     * 當玩家 登出登入
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        this.plugin.createPlayerDrops(player);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        this.plugin.removePlayerDrops(player.getUniqueId());
    }

    /**
     * 當MM怪物生成時 給予killer 紀錄
     */
    @EventHandler
    public void onMMspawned(MythicMobSpawnEvent event){
        this.killer.put(event.getEntity().getUniqueId(),new HashSet<>());
    }


    /**
     * 當MM怪物被殺死時
     */
    @EventHandler
    public void onMMDeath(MythicMobDeathEvent event){
        List<ItemStack> drops = event.getDrops();
        for (UUID uuid: this.killer.get(event.getEntity().getUniqueId())){


            List<UUID> fallingBlockUUIDs =
                    plugin.getPlayerDrops(uuid).addItems(drops,event.getEntity().getLocation());
            for (UUID fallingblockuuid:fallingBlockUUIDs) {
                this.trans.put(fallingblockuuid, uuid);
            }
        }
        drops.clear();
        event.setDrops(drops);
        this.killer.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onMMDespawned(MythicMobDespawnEvent event){

        this.killer.remove(event.getEntity().getUniqueId());
    }

    /**
     * 當 掉落沙子(定位物品) 放置方塊時
     */
    @EventHandler
    public void onFallingBlockPlaced(EntityChangeBlockEvent event){
        if (event.getEntity() instanceof FallingBlock){
            FallingBlock fallingBlock = (FallingBlock) event.getEntity();
            if (trans.containsKey(fallingBlock.getUniqueId())){
                plugin.getPlayerDrops(trans.get(fallingBlock.getUniqueId())).setFinalLocation(fallingBlock);
                trans.remove(fallingBlock.getUniqueId());
            }
            event.setCancelled(true);
        }
    }

    /**
     * 當 掉落沙子(定位物品) 變成物品時 (放置失敗)
     */
    @EventHandler
    public void onFallingBlockDeath(EntityDropItemEvent event){
        if (event.getEntity() instanceof FallingBlock){
            FallingBlock fallingBlock = (FallingBlock) event.getEntity();
            if (trans.containsKey(fallingBlock.getUniqueId())){
                plugin.getPlayerDrops(trans.get(fallingBlock.getUniqueId())).setFinalLocation(fallingBlock);
                trans.remove(fallingBlock.getUniqueId());
            }
            event.setCancelled(true);
        }
    }

    public void removeTrans(UUID uuid){
        this.trans.remove(uuid);
    }

    public int getAmount(){
        return this.trans.size();
    }

    /**
     * 當 MM 怪物被攻擊時
     */
    @EventHandler
    public void onMMDamaged(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player && MythicMobs.inst().getMobManager().isActiveMob(event.getEntity().getUniqueId())){
            this.killer.get(event.getEntity().getUniqueId()).add(event.getDamager().getUniqueId());
        }

    }

}
