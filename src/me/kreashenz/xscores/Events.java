package me.kreashenz.xscores;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Events implements Listener {

	private xScores plugin;

	public Events(xScores plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent e) {
		Entity ent = e.getEntity();
		if (e.getEntity().getKiller() != null) {
			Player p = (Player) e.getEntity().getKiller();
			String n = p.getName();
			switch (ent.getType()) {
			case CAVE_SPIDER:
				String cs = n + ".cavespider";
				plugin.set(cs, (Integer) plugin.get(cs) + 1);
			break;
			case CREEPER:
				String c = n + ".creeper";
				plugin.set(c, (Integer) plugin.get(c) + 1);
			break;
			case ENDERMAN:
				String en = n + ".enderman";
				plugin.set(en, (Integer) plugin.get(en) + 1);
			break;
			case PIG_ZOMBIE:
				String s = n + ".pigzombie";
				plugin.set(s, (Integer) plugin.get(s) + 1);
			break;
			case SKELETON:
				String sk = n + ".skeleton";
				plugin.set(sk, (Integer) plugin.get(sk) + 1);
			break;
			case SPIDER:
				String sp = n + ".spider";
				plugin.set(sp, (Integer) plugin.get(sp) + 1);
			break;
			case ZOMBIE:
				String z = n + ".zombie";
				plugin.set(z, (Integer) plugin.get(z) + 1);
			break;
			case PLAYER:
			break; // Just do nothing..
			default:
				String o = n + ".other";
				plugin.set(o, (Integer) plugin.get(o) + 1);
			break;
			}
			plugin.updateScoreboard(p);
		}
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (plugin.get(p.getName() + ".kills") == null) {
			plugin.set(p.getName() + ".kills", 0);
			plugin.set(p.getName() + ".deaths", 0);
			plugin.set(p.getName() + ".other", 0);
			plugin.set(p.getName() + ".zombie", 0);
			plugin.set(p.getName() + ".spider", 0);
			plugin.set(p.getName() + ".skeleton", 0);
			plugin.set(p.getName() + ".pigzombie", 0);
			plugin.set(p.getName() + ".enderman", 0);
			plugin.set(p.getName() + ".cavespider", 0);
			plugin.set(p.getName() + ".creeper", 0);
			plugin.saveFile();
		}

		plugin.streak.put(p.getName(), 0);
		plugin.cmdExe.enabled.put(p.getName(), true);

		plugin.updateScoreboard(p);
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent e) {
		Player p = e.getEntity().getPlayer();
		if (p.getKiller() != null && p.getKiller() instanceof Player) {
			Player k = p.getKiller();
			plugin.setKills(k, plugin.getKills(k) + 1);
			plugin.setStreaks(k);
			plugin.setDeaths(p, plugin.getDeaths(p) + 1);
			plugin.clearStreaks(p);
			plugin.updateScoreboard(k);
		}
		else {
			plugin.setDeaths(p, plugin.getDeaths(p) + 1);
			plugin.clearStreaks(p);
		}
		plugin.updateScoreboard(p);
	}

}
