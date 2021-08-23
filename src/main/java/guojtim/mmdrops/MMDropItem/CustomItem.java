package guojtim.mmdrops.MMDropItem;

import guojtim.mmdrops.MMdrops;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CustomItem {
    private FallingBlock block;
    private EntityItem item;
    private ItemStack itemStack;
    private net.minecraft.world.item.ItemStack nmsItem;
    private Location finalLocation;
    private Long Age;
    private MMdrops plugin;

    public CustomItem(ItemStack itemStack, Location location,Player player,MMdrops plugin){
        this.plugin = plugin;
        this.itemStack = itemStack;
        this.nmsItem = CraftItemStack.asNMSCopy(itemStack);
        this.finalLocation = location;
        this.Age = (System.currentTimeMillis()/1000L) + 45;
        //
        //
        //



        this.item = new EntityItem(((CraftWorld)location.getWorld()).getHandle(),
                location.getX(),
                location.getY(),
                location.getZ(),
                this.nmsItem);
        /**
         * Motion
         */
        double dx,dy,dz;
        dx = (Math.random()*10-5)/10;
        dy = (Math.random()*5+1)/10;
        dz = (Math.random()*10-5)/10;

        Vec3D motion = new Vec3D(dx,dy,dz);


        /**
         * connection packet
         */
        PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
        connection.sendPacket(new PacketPlayOutSpawnEntity(item));
        MobEffect mbeffect = new MobEffect(new MobEffect(MobEffectList.fromId(24),Integer.MAX_VALUE,0,false,false));
        this.item.setFlag(6,true);
        connection.sendPacket(new PacketPlayOutEntityEffect(item.getId(),mbeffect));
        connection.sendPacket(new PacketPlayOutEntityMetadata(item.getId(), item.getDataWatcher(), true));
        connection.sendPacket(new PacketPlayOutEntityVelocity(item.getId(),motion));


        /**
         * Falling Block
         */
        this.block = location.getWorld().spawnFallingBlock(location, Material.LIGHT,(byte)0);
        Entity nmsEntity = ((CraftEntity)this.block).getHandle();
        nmsEntity.setMot(motion);
        ((CraftEntity)this.block).setHandle(nmsEntity);
    }

    /**
     *
     * @return 是否超過生存時間
     */
    public boolean isOvered(){
        return  (System.currentTimeMillis()/1000L > this.Age); // 超過 45秒
    }


    public UUID getFallingBlockUUID(){
        return this.block.getUniqueId();
    }

    public void DestroyItem(Player player){
        PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
        connection.sendPacket(new PacketPlayOutEntityDestroy(item.getId()));
        plugin.getEventManager().removeTrans(this.block.getUniqueId());
        this.block.remove();
    }


    public void setLocation(Location location,Player player){
        //respawn item
        PlayerConnection connection = ((CraftPlayer)player).getHandle().b;
        connection.sendPacket(new PacketPlayOutEntityDestroy(item.getId()));
        this.item = new EntityItem(((CraftWorld)location.getWorld()).getHandle(),
                location.getX(),
                location.getY(),
                location.getZ(),
                this.nmsItem);
        connection.sendPacket(new PacketPlayOutSpawnEntity(item));
        MobEffect mbeffect = new MobEffect(new MobEffect(MobEffectList.fromId(24),Integer.MAX_VALUE,0,false,false));
        this.item.setFlag(6,true);
        connection.sendPacket(new PacketPlayOutEntityEffect(item.getId(),mbeffect));
        connection.sendPacket(new PacketPlayOutEntityMetadata(item.getId(), item.getDataWatcher(), true));


        this.finalLocation = location;
    }

    public Location getFinalLocation(){
        return this.finalLocation;
    }


    /**
     *
     * @param player
     * @return 是否成功撿起
     */
    public boolean pickupItem(Player player){
        if (player.getInventory().firstEmpty() == -1) return false;

        player.getInventory().addItem(itemStack);
        player.sendMessage("You pickup "+(itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : itemStack.getType().name())+" x"+itemStack.getAmount());
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP,10,1);


        return true;
    }

}
