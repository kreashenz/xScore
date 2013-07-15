package me.kreashenz.xscores;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

	private HashMap<String, Integer> streak = new HashMap<String, Integer>();
	private HashMap<String, Integer> kills = new HashMap<String, Integer>();
	private HashMap<String, Integer> deaths = new HashMap<String, Integer>();

	public Economy economy = null;
	public FileConfiguration a;

	private File file;
	private FileConfiguration conf;

	private xScoresCommand cmdExe;

	public void onEnable() {
		a = getConfig();
		file = new File(getDataFolder() + File.separator + "stats.yml");
		conf = YamlConfiguration.loadConfiguration(file);

		cmdExe = new xScoresCommand(this);

		setupTimer();
		setupVault(getServer().getPluginManager());
		getServer().getPluginManager().registerEvents(this,this);
		getCommand("clearscores").setExecutor(cmdExe);
		getCommand("kdr").setExecutor(cmdExe);

		saveDefaultConfig();

		try {
			new Metrics(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		runSaveTimer();
		
		if(!a.contains("Streak-Tag")){
			a.set("Streak-Tag", "&aStreak&7: &c");
			saveConfig();
		}

	}

	@EventHandler
	public void PlayerJoinEvent(PlayerJoinEvent e){
		Player p = e.getPlayer();
		setScoreboard(p);
		if(!a.contains(p.getName())){
			a.set(p.getName() + ".kills", "0");
			a.set(p.getName() + ".deaths", "0");
			try {
				conf.save(file);
			} catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}

	@EventHandler
	public void PlayerDeathEvent(PlayerDeathEvent e){
		Player p = e.getEntity().getPlayer();
		Player k = p.getKiller();
		if (k instanceof Player && k != null){
			setKills(k.getName(), getKills(k.getName()) + 1);
			setStreaks(k.getName(), getStreaks(k.getName()) +1);
			setDeaths(p.getName(), getDeaths(p.getName()) + 1);
			clearStreaks(p);
		}
	}

	public void setScoreboard(Player p){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		if(economy != null){

			Objective objective = board.registerNewObjective("test", "dummy");

			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', a.getString("Stats-Tag")));

			Score kills = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Kills-Tag"))));
			kills.setScore(getKills(p.getName()));

			Score deaths = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Deaths-Tag"))));
			deaths.setScore(getDeaths(p.getName()));

			Score streak = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Streak-Tag"))));
			streak.setScore(getStreaks(p.getName()));

			Score bal = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Balance-Tag"))));
			bal.setScore((int) economy.getBalance(p.getName()));
		} else {

			Objective objective = board.registerNewObjective("test", "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', a.getString("Stats-Tag")));

			Score kills = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Kills-Tag"))));
			kills.setScore(getKills(p.getName()));

			Score deaths = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Deaths-Tag"))));
			deaths.setScore(getDeaths(p.getName()));

			Score streak = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Streak-Tag"))));
			streak.setScore(getStreaks(p.getName()));
		}
		p.setScoreboard(board);
	}

	public int getKills(String p){
		if(kills.containsKey(p)){
			return kills.get(p);
		} else {
			return 0;
		}
	}

	public int getDeaths(String p){
		if(deaths.containsKey(p)){
			return deaths.get(p);
		} else {
			return 0;
		}
	}

	public int getStreaks(String p){
		if(streak.containsKey(p)){
			return streak.get(p);
		} else {
			return 0;
		}
	}

	public void clearStreaks(Player p){
		streak.put(p.getName(), 0);
	}

	public double getKDR(Player p){
		return Double.valueOf(getKills(p.getName()) / getDeaths(p.getName()));
	}
	public void setKills(String p, int Kills){
		if(!kills.containsKey(p)){
			kills.put(p, Kills);
		} else {
			int a = kills.get(p);
			kills.remove(p);
			kills.put(p, a+1);
		}
	}

	public void setDeaths(String p, int Deaths){
		if(!deaths.containsKey(p)){
			deaths.put(p, Deaths);
		} else {
			int a = deaths.get(p);
			deaths.remove(p);
			deaths.put(p, a+1);
		}
	}

	public void setStreaks(String p, int streaks){
		if(!streak.containsKey(p)){
			streak.put(p, streaks);
		} else {
			int a = streak.get(p);
			streak.remove(p);
			streak.put(p, a+1);
		}
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

	private boolean setupEconomy(){
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
		}, 0L, getConfig().getInt("Scoreboard-Update-Time")*20);
	}

	private void runSaveTimer(){
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				try {
					for(Player p : Bukkit.getOnlinePlayers()){
						conf.set(p.getName() + ".kills", getKills(p.getName()));
						conf.set(p.getName() + ".deaths", getDeaths(p.getName()));

						conf.save(file);
					}
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}, 0L, getConfig().getInt("Stats-File-Save-Time"));
	}
}

