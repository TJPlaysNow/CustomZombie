package me.Sebastian.Custom_Zombie;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.Sebastian.CustomZombie.Pathfinding.AStar;
import me.Sebastian.CustomZombie.Pathfinding.AStar.InvalidPathException;
import me.Sebastian.CustomZombie.Pathfinding.PathingResult;
import me.Sebastian.CustomZombie.Pathfinding.Tile;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_10_R1.AttributeInstance;
import net.minecraft.server.v1_10_R1.AttributeModifier;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.GenericAttributes;

public class Main extends JavaPlugin{
	
	public static Main main;
	public static boolean useCustomAI = true;
	
	public Main(){
		main = this;
	}
	
	public static Main getMainClass(){
		return main;
	}
	
	PluginDescriptionFile pdfFile = getDescription();
	public final Logger logger = Logger.getLogger("Minecraft");
	
	public void onDisable(){
		logger.info("Custom Zombies has been disabled");
		
	}
	
	public void onEnable(){
		main = this;
		logger.info("Custom Zombies has been enabled");
	}
	
	Location loc1, loc2;
	
	private static final UUID followRangeUID = UUID.fromString("1737400d-3c18-41ba-8314-49a158481e1e");
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]){
		if(sender instanceof Player){
			final Player player = (Player) sender;
			if(!player.hasPermission("group.admin")){
				return false;
			}
			if(label.equalsIgnoreCase("killall")){
				
				for(Zombie z : player.getWorld().getEntitiesByClass(Zombie.class)){
					z.remove();
				}
				
			}
			if(label.equalsIgnoreCase("loc1")){
				loc1 = player.getLocation();
				player.sendMessage("Set location 1");
			}
			if(label.equalsIgnoreCase("loc2")){
				loc2 = player.getLocation();
				player.sendMessage("Set location 2");

			}
			if(label.equalsIgnoreCase("customzombie")){
				World world = player.getWorld();
				
				final CustomZombie zombie = new CustomZombie(world , 1.2);
				EntityTypes.spawnEntity(zombie, player.getLocation());
				
				final Zombie bukkitZombie = (Zombie) zombie.getBukkitEntity();
				if(bukkitZombie != null){
					bukkitZombie.setCustomName(ChatColor.RED+"Custom Zombie");
					bukkitZombie.setCustomNameVisible(true);
				}
				
				final Zombie normalZombie = (Zombie) world.spawnEntity(player.getLocation(), EntityType.ZOMBIE);
				
				normalZombie.setCustomName(ChatColor.GRAY+"Zombie");
				normalZombie.setCustomNameVisible(true);
				normalZombie.setRemoveWhenFarAway(false);
				
				EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity)normalZombie).getHandle();
				AttributeInstance followRangeAttribute = nmsEntity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
				AttributeModifier modifier = new AttributeModifier(followRangeUID, "Nazi Zombies Follow Range Modifier", 100, 0);
				followRangeAttribute.b(modifier);
				followRangeAttribute.a(modifier);
				
				new BukkitRunnable(){
					public void run(){
						//zombie.setTargetLocation(player.getLocation());
						//player.teleport(zombie.getBukkitZombie().getLocation());
						//zombie.setTarget(player);
						zombie.setTarget(player);
						normalZombie.setTarget(player);
						//bukkitZombie.setTarget(player);
					}
				}.runTaskLater(this, 100);
				
				player.sendMessage(ChatColor.GREEN+"Zombies spawned!");
			}
			if(label.equalsIgnoreCase("testpath")){
				
				if(loc1 == null || loc2 == null){
					player.sendMessage("Missing locs");

					return false;
				}
				
				final Location start = loc1.clone();
				Location end = loc2.clone();
				
				try {
					long currentMils = System.currentTimeMillis();
					
					AStar astarPath = new AStar(start, end, 100);
					final List<Tile> walkNodes = astarPath.iterate();
					PathingResult result = astarPath.getPathingResult();
					
					if(result != PathingResult.SUCCESS || walkNodes == null){
						Bukkit.getLogger().info("Pathing is not good or walkNodes are null");
						return false;
					}
					long afterMils = System.currentTimeMillis();
					
					Bukkit.getLogger().info("Took "+(afterMils-currentMils)+" !");
					
					for(Tile t : walkNodes){
						Block b = t.getLocation(start).getBlock();
						if(b.getType() == Material.AIR){
							b.setType(Material.DIAMOND_BLOCK);
						}
					}
					
					new BukkitRunnable(){
						public void run(){
							for(Tile t : walkNodes){
								Block b = t.getLocation(start).getBlock();
								if(b.getType() == Material.DIAMOND_BLOCK){
									b.setType(Material.AIR);
								}
							}
						}
					}.runTaskLater(this, 200);
					
				} catch (InvalidPathException e1) {
					Bukkit.getLogger().info("Invalid path exception");
					//start or end is air
					return false;
				}
				
			}
			/*if(label.equalsIgnoreCase("spawnzombie")){
				Zombie zombie = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
				
				EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity)zombie).getHandle();
				AttributeInstance followRangeAttribute = nmsEntity.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
				AttributeModifier modifier = new AttributeModifier(followRangeUID, "Nazi Zombies Follow Range Modifier", 100, 0);
				
				followRangeAttribute.b(modifier);
				followRangeAttribute.a(modifier);
				
				if(zombie.isBaby()) zombie.setBaby(false);
				if(zombie.isVillager()){
					zombie.remove();
				}
				
				zombie.setCanPickupItems(false);
				zombie.setPassenger(null);
				zombie.setRemoveWhenFarAway(false);
			}*/
			if(label.equalsIgnoreCase("target")){
				if(args.length > 0){
					Player targetPlayer = Bukkit.getPlayer(args[0]);
					if(targetPlayer == null){
						player.sendMessage(ChatColor.RED+"Input Player is not online");
						return false;
					}
					for(org.bukkit.entity.Entity en : player.getWorld().getEntities()){
						if(en instanceof Zombie){
							Zombie zombie = (Zombie) en;
							zombie.setTarget(targetPlayer);
							targetPlayer.sendMessage(ChatColor.GREEN+"Zombie has targeted you!");
							targetPlayer.setGlowing(true);
						}
					}
				}else{
					player.sendMessage(ChatColor.RED+"Please specify a Player!");
				}
			}
		}
		
		return false;
	}
	public enum EntityTypes
	{
		CUSTOM_ZOMBIE("Zombie", 54, CustomZombie.class);
		
		EntityTypes(String name, int id, Class<CustomZombie> custom){
			addToMaps(custom, name, id);
		}

	  public static void spawnEntity(Entity entity, Location loc)
	   {
		 //entity.teleport(loc);
	     entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
	     ((CraftWorld)loc.getWorld()).getHandle().addEntity((Entity) entity);
	   }

	    @SuppressWarnings("unchecked")
		private static void addToMaps(Class<CustomZombie> clazz, String name, int id)
	    {
	        //getPrivateField is the method from above.
	        //Remove the lines with // in front of them if you want to override default entities (You'd have to remove the default entity from the map first though).
	        ((Map<String, Class<CustomZombie>>)getPrivateField("c", net.minecraft.server.v1_10_R1.EntityTypes.class, null)).put(name, clazz);
	        ((Map<Class<CustomZombie>, String>)getPrivateField("d", net.minecraft.server.v1_10_R1.EntityTypes.class, null)).put(clazz, name);
	        //((Map)getPrivateField("e", net.minecraft.server.v1_7_R4.EntityTypes.class, null)).put(Integer.valueOf(id), clazz);
	        ((Map<Class<CustomZombie>, Integer>)getPrivateField("f", net.minecraft.server.v1_10_R1.EntityTypes.class, null)).put(clazz, Integer.valueOf(id));
	        //((Map)getPrivateField("g", net.minecraft.server.v1_7_R4.EntityTypes.class, null)).put(name, Integer.valueOf(id));
	    }
	}
	public static Object getPrivateField(String fieldName, Class<? extends net.minecraft.server.v1_10_R1.EntityTypes> clazz, Object object)
    {
        Field field;
        Object o = null;

        try
        {
            field = clazz.getDeclaredField(fieldName);

            field.setAccessible(true);

            o = field.get(object);
        }
        catch(NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return o;
    }
}
