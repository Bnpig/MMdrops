package guojtim.mmdrops.MMDropItem;

import guojtim.mmdrops.MMdrops;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
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
    public List<UUID> addItems(@NotNull List<ItemStack> items, Location location, MythicMob mythicMob){
        List<UUID> FallingBlockUUIDs = new ArrayList<>();
        for (ItemStack item : items){
            CustomItem customItem = new CustomItem(item,location,player,plugin,mythicMob);
            this.customItem.put(customItem.getFallingBlockUUID(),customItem);
            FallingBlockUUIDs.add(customItem.getFallingBlockUUID());
        }
        return FallingBlockUUIDs;
    }


    /**
     * 設定 final location 和 校正位置
     * @param fallingBlock Falling Block 最後消失地方
     */
    public void setFinalLocation(@NotNull FallingBlock fallingBlock){
        if (customItem.containsKey(fallingBlock.getUniqueId())){
            Location finalLocation = fallingBlock.getLocation();

            if (isWaterLogged(finalLocation)){
                //校正
                finalLocation = finalLocation.add(0,1-(finalLocation.getY()-Math.floor(finalLocation.getY())),0);
            }

            // 累加到 final Location 為非 water Block
            while(isWaterLogged(finalLocation)){
                finalLocation = finalLocation.add(0,1,0);
            }

            customItem.get(fallingBlock.getUniqueId()).setLocation(finalLocation,player);
        }
    }


    /**
     * 偵測玩家剪取 對應到 Background Timer class
     * 和超時部分
     */
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


    /**
     * 偵測目前位置的方塊 是否包含水
     * @param blockLocation 對應位置
     * @return true 為真
     */
    private boolean isWaterLogged(Location blockLocation){
        Block block = blockLocation.getBlock();
        BlockData blockData = block.getBlockData();

        if (isWaterBlocks(block.getType())) return true;

        if (blockData instanceof Waterlogged){
            return ((Waterlogged) blockData).isWaterlogged();
        }
        return false;
    }

    /**
     *
     * @param block 材料
     * @return 是否為 水方塊
     */
    private boolean isWaterBlocks(Material block){
        return block.equals(Material.WATER) | block.equals(Material.KELP)
                | block.equals(Material.SEAGRASS) | block.equals(Material.TALL_SEAGRASS)
                | block.equals(Material.KELP_PLANT);
    }






}
