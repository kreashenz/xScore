package me.kreashenz.xscores;

import java.io.File;
import java.io.IOException;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class xScores extends JavaPlugin implements Listener {

	public static Economy economy = null;

	public void onEnable() {
		setupTimer();
		setupVault(getServer().getPluginManager());
		getServer().getPluginManager().registerEvents(this,this);
		getCommand("clearscores").setExecutor(new xScoresCommand(this));
		getCommand("kdr").setExecutor(new xScoresCommand(this));
		if(!(new File("plugins/xScores/config.yml")).exists()){
			saveResource("config.yml", false);
		}
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void PlayerJoinEvent(PlayerJoinEvent e){
		Player p = e.getPlayer();
		setScoreboard(p);
		if(!getConfig().contains(p.getName())){
			getConfig().set(p.getName() + ".kills", "0");
			getConfig().set(p.getName() + ".deaths", "0");
		}
		saveConfig();
	}

	@EventHandler
	public void PlayerDeathEvent(PlayerDeathEvent e){
		Player p = e.getEntity().getPlayer();
		Player k = p.getKiller();
		if (k instanceof Player){
			setDeaths(p, getDeaths(p.getName()) + 1);
			setKills(k, getKills(k.getName()) + 1);
			setScoreboard(p);
			setScoreboard(k);
		} else {
			if(k != null){
				getConfig().set(p.getName() + ".deaths", getDeaths(p.getName()) + 1);
				getConfig().set(k.getName() + ".kills", getKills(k.getName()) + 1);
			} else {
				getLogger().info("There was no killer for " + p.getName() + "'s death.");
			}
			saveConfig();

			setScoreboard(p);
		}
	}

	public void setScoreboard(Player p){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		if(economy != null){

			Objective objective = board.registerNewObjective("test", "dummy");

			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Stats-Tag")));

			Score kills = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Kills-Tag"))));
			kills.setScore(getKills(p.getName()));

			Score deaths = objective.getScore(Bukkit.getOfflinePlayer((ChatColor.translateAlternateColorCodes('&', getConfig().getString("Deaths-Tag")))));
			deaths.setScore(getDeaths(p.getName()));

			Score bal = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Balance-Tag"))));
			bal.setScore((int) economy.getBalance(p.getName()));

			p.setScoreboard(board);

		} else { 

			Objective objective = board.registerNewObjective("test", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Stats-Tag")));

			Score kills = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Kills-Tag"))));
			kills.setScore(getKills(p.getName()));

			Score deaths = objective.getScore(Bukkit.getOfflinePlayer((ChatColor.translateAlternateColorCodes('&', getConfig().getString("Deaths-Tag")))));
			deaths.setScore(getDeaths(p.getName()));

			p.setScoreboard(board);
		}
	}

	public int getKills(String p){
		if(getConfig().get(p + ".kills") != null){
			return getConfig().getInt(p + ".kills");
		} else {
			return 0;
		}
	}

	public int getDeaths(String p){
		return getConfig().getInt(p + ".deaths");
	}

	public double getKDR(Player p){
		return getConfig().getDouble(p.getName() + ".kills") / getConfig().getDouble(p.getName() + ".deaths");
	}
	public void setKills(Player p, int Kills){
		getConfig().set(p.getName() + ".kills", Kills);
		saveConfig();
	}

	public void setDeaths(Player p, int Deaths){
		getConfig().set(p.getName() + ".deaths", Deaths);
		saveConfig();
	}

	private void setupVault(PluginManager pm) {
		Plugin vault =  pm.getPlugin("Vault");
		if (vault != null && vault instanceof net.milkbowl.vault.Vault) {
			getLogger().info("Loaded Vault v" + vault.getDescription().getVersion());
			if (!setupEconomy()) {
				getLogger().warning("No economy plugin installed.");
			} else {
				getLogger().warning("Vault not loaded, please check your plugins folder or console.");
			}
		}
	}

	public boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		} else {
			getLogger().warning("Vault is not installed, xScores Scoreboard will not be showing Economy.");
			return false;
		}
		return (economy != null);
	}

	public void setupTimer(){
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()){
					setScoreboard(p);
				}
			}
		}, 0L, 10L);
	}
}

