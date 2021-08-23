package guojtim.mmdrops.MMDropItem;

import guojtim.mmdrops.MMdrops;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerDrops {
    /**
     * <Falling Block UUID>,<Custom Item Class>
     */
    private HashMap<UUID,CustomItem> customItem = new HashMap<>();
    private final Player player;
    private final MMdrops plugin;

    public PlayerDrops(Player player,MMdrops plugin){
        this.plugin = plugin;
        this.player = player;
    }


    /**
     * @param items <新增掉落物>
     * @param location <怪物死亡地點>
     */
    public List<UUID> addItems(@NotNull List<ItemStack> items, Location location){
        List<UUID> FallingBlockUUIDs = new ArrayList<>();
        for (ItemStack item : items){
            CustomItem customItem = new CustomItem(item,location,player,plugin);
            this.customItem.put(customItem.getFallingBlockUUID(),customItem);
            FallingBlockUUIDs.add(customItem.getFallingBlockUUID());
        }
        return FallingBlockUUIDs;
    }

    public void setFinalLocation(@NotNull FallingBlock fallingBlock){
        if (customItem.containsKey(fallingBlock.getUniqueId())){
            customItem.get(fallingBlock.getUniqueId()).setLocation(fallingBlock.getLocation(),player);
        }
    }

    public void check(){
        if (this.customItem.isEmpty()) return;
        Location location = player.getLocation();

        for (CustomItem item:this.customItem.values()){
            if (location.distance(item.getFinalLocation()) < 1.5){
                if (item.pickupItem(this.player)){
                    item.DestroyItem(this.player);
                    this.customItem.remove(item.getFallingBlockUUID());
                    break; // Debug
                }
            }else if (item.isOvered()){
                item.DestroyItem(this.player);
                this.customItem.remove(item.getFallingBlockUUID());
                break;
            }
        }
    }
}
