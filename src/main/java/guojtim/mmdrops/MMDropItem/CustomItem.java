package guojtim.mmdrops.MMDropItem;

import guojtim.mmdrops.MMdrops;
import io.lumine.xikage.mythicmobs.io.MythicConfig;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderMeta;
import io.lumine.xikage.mythicmobs.utils.config.ConfigurationSection;
import io.lumine.xikage.mythicmobs.utils.config.file.FileConfiguration;
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
    private MythicMob mythicMob;

    public CustomItem(ItemStack itemStack, Location location, Player player, MMdrops plugin, MythicMob mythicMob){
        this.mythicMob = mythicMob;
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

        MythicConfig config = this.mythicMob.getConfig();
        String mobname = this.mythicMob.getInternalName();
        ConfigurationSection configuration = config.getFileConfiguration().getConfigurationSection(mobname);

        Integer xa = configuration.contains("MMDrops.x-mot.random") ?configuration.getInt("MMDrops.x-mot.random") : 10
                ,xb = configuration.contains("MMDrops.x-mot.distance") ?configuration.getInt("MMDrops.x-mot.distance") : -5
                ,xc = configuration.contains("MMDrops.x-mot.division") ?configuration.getInt("MMDrops.x-mot.division") : 10;

        Integer ya = configuration.contains("MMDrops.y-mot.random") ?configuration.getInt("MMDrops.y-mot.random") : 5
                ,yb = configuration.contains("MMDrops.y-mot.distance") ?configuration.getInt("MMDrops.y-mot.distance") : 1
                ,yc = configuration.contains("MMDrops.y-mot.division") ?configuration.getInt("MMDrops.y-mot.division") : 10;

        Integer za = configuration.contains("MMDrops.z-mot.random") ?configuration.getInt("MMDrops.z-mot.random") : 10
                ,zb = configuration.contains("MMDrops.z-mot.distance") ?configuration.getInt("MMDrops.z-mot.distance") : -5
                ,zc = configuration.contains("MMDrops.z-mot.division") ?configuration.getInt("MMDrops.z-mot.division") : 10;



        dx = (Math.random()*xa+xb)/xc;
        dy = (Math.random()*ya+yb)/yc;
        dz = (Math.random()*za+zb)/zc;




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
        this.item.setNoGravity(true);
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
