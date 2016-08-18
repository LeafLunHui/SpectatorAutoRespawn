package com.xingyi.c01;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.tools.DocumentationTool.Location;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SpectatorAutoRespawn extends JavaPlugin implements Listener
{
	private HashMap<String, Boolean> ON_RESPAWN_PLAYER =new HashMap<>();
	private Boolean isOnCoolDownTime = false;
@Override
public void onEnable() 
{
	System.out.println("[SpectatorAutoRespawn] Plugin has been Enable!");
	System.out.println("author£ºSkyStardust");
	File config = new File(getDataFolder(),"config.yml");
	getServer().getPluginManager().registerEvents(this, this);
	if (!config.exists()) 
	{
		System.out.println("[SpectatorAutoRespawn] configuration file do not exists,save default setting..");
		saveDefaultConfig();
	}
}
@Override
	public void onDisable() 
{
		System.out.println("[SpectatorAutoRespawn] Plugin has been disable");
		System.out.println("author£ºSkyStardust");
	}


public void setOnCoolDownTime(Player player ,Boolean y) 
{
	this.ON_RESPAWN_PLAYER.put(player.getName(), y);
}
public boolean isOnCoolDownTime(Player p) 
{
	return ON_RESPAWN_PLAYER.get(p.getName());
}
@EventHandler
public void onPlayerHurt(EntityDamageEvent e)
{
	if (e.getEntity() instanceof Player&&isOnCoolDownTime(Bukkit.getPlayer(e.getEntity().getName()))) 
	{
		e.setCancelled(true);
	}
}
@EventHandler
public void PlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e)
{
	Boolean canUseCMD = getConfig().getBoolean("Gobal.canUseCMD");
	if (isOnCoolDownTime(e.getPlayer())&&canUseCMD==false) 
	{
		e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.RED+"[SpectatorAutoRespawn]You can not use command during the Death Cooldown");
	}
}
@EventHandler
public void onPlayerInteractEntity(PlayerInteractEvent e) 
{
	if (isOnCoolDownTime(e.getPlayer())) 
	{
		e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.RED+"[SpectatorAutoRespawn]You can not attack entity during the Death Cooldown .");
	}
}

@EventHandler
public void SpectatorTP(PlayerTeleportEvent e)
{
	if (e.getCause()==PlayerTeleportEvent.TeleportCause.SPECTATE&&isOnCoolDownTime(e.getPlayer())) 
	{
		e.setCancelled(true);
		e.getPlayer().sendMessage(ChatColor.RED+"[SpectatorAutoRespawn]Spectator do not be allowed to teleport others");
	}
}

public void respawn(Player p)
{
	p.spigot().respawn();
}
@SuppressWarnings("deprecation")
@EventHandler 
public void onDeathEvent(PlayerDeathEvent e) 
{
	respawn(e.getEntity());
	Boolean EnableTitle = getConfig().getBoolean("Title.Enable");
	String TitleCoolDownMessage = getConfig().getString("Title.CoolDown.Title");
	String SubTitleCoolDownMessage = getConfig().getString("Title.CoolDown.SubTitle");
	String TitleRespawnMessage =  getConfig().getString("Title.ReSpawn.Title");
	String SubTitleRespawnMessage = getConfig().getString("Title.ReSpawn.SubTitle");
	List<String> DieM = getConfig().getStringList("DieM");
	List<String> ReSpawnM = getConfig().getStringList("ReSpawnM");
	Boolean EnableSP = getConfig().getBoolean("EnableSP");
	int RespawnDelay = getConfig().getInt("RespawnDelay");
	int FadeinTime = getConfig().getInt("FadeinTime");
	int FadeoutTime = getConfig().getInt("FadeoutTime");
	int StayTime = getConfig().getInt("StayTime");

	try {
	    Metrics metrics = new Metrics(this);
	    metrics.start();
	} catch (IOException exception) {
	    System.out.println("Could not connect to the Metrics ,Please check your network!");
	}
	
		
		
		Collection<? extends Player> onlinePlayer = Bukkit.getOnlinePlayers();
		GameMode gm = e.getEntity().getGameMode();
			if (EnableTitle) 
			{
				SendTitle.sendTitleMessage(e.getEntity().getPlayer(), FadeinTime, StayTime, FadeoutTime, TitleCoolDownMessage, SubTitleCoolDownMessage);
			}
			else 
			{
				DieM.forEach(tip -> e.getEntity().sendMessage(ChatColor.translateAlternateColorCodes('&', tip)));
			}
			
			if (EnableSP) 
			{
				e.getEntity().setGameMode(GameMode.SPECTATOR);
			}
			else 
			{
				onlinePlayer.forEach(player -> e.getEntity().hidePlayer(player));
				e.getEntity().setAllowFlight(true);
				e.getEntity().setFlying(true);
			}
			
			org.bukkit.Location location = e.getEntity().getLocation();
			new BukkitRunnable() 
			{
				
				@Override
				public void run() 
				{
					e.getEntity().setGameMode(gm);
					e.getEntity().teleport(location);
					setOnCoolDownTime(e.getEntity(),false);
				}
			}.runTaskLater(this, RespawnDelay*20+10);
			for (int i = 0; i < RespawnDelay; i++) 
			{
				int left = RespawnDelay-i;
				getServer().getScheduler().scheduleSyncDelayedTask(this, new BukkitRunnable() 
				{
					
					@Override
					public void run() 
					{
						
						if (EnableTitle) 
						{
							SendTitle.sendTitleMessage(e.getEntity().getPlayer(), FadeinTime, StayTime, FadeoutTime, TitleCoolDownMessage, SubTitleCoolDownMessage.replace("%left%", ""+left));
							SendTitle.sendTitleMessage(e.getEntity().getPlayer(), FadeinTime, StayTime, FadeoutTime, TitleRespawnMessage, SubTitleRespawnMessage.replace("%left%", ""+left));
						}
						else
						{
							ReSpawnM.forEach(mes -> e.getEntity().sendMessage(ChatColor.translateAlternateColorCodes('&', mes).replace("%left%", ""+left)));
						}
						if (EnableSP) 
						{
							
						}
						else
						{
							
						onlinePlayer.forEach(player -> e.getEntity().showPlayer(player));
						e.getEntity().getPlayer().setFlying(false);
						e.getEntity().setAllowFlight(false);
						}
						setOnCoolDownTime(e.getEntity(),true);
					}
				}, RespawnDelay*20-left*20);
			}

			
		
	 
}
}
