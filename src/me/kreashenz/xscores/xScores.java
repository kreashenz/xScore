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
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class xScores extends JavaPlugin implements Listener {

	private HashMap<String, Integer> streak = new HashMap<String, Integer>();

	public Economy economy = null;
	public FileConfiguration a;

	protected File file;
	private FileConfiguration conf;

	private xScoresCommand cmdExe;

	public void onEnable() {
		a = getConfig();
		file = new File(getDataFolder() + File.separator + "stats.yml");
		conf = YamlConfiguration.loadConfiguration(file);

		cmdExe = new xScoresCommand(this);

		setupVault();

		getServer().getPluginManager().registerEvents(this,this);

		getCommand("clearscores").setExecutor(cmdExe);
		getCommand("kdr").setExecutor(cmdExe);
		getCommand("xboard").setExecutor(cmdExe);
		getCommand("xscore").setExecutor(cmdExe);

		saveDefaultConfig();
		saveResource("stats.yml", false);

		try {
			new Metrics(this).start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		runSaveTimer();
	}

	@EventHandler
	public void PlayerJoinEvent(PlayerJoinEvent e){
		Player p = e.getPlayer();
		if(conf.get(p.getName() + ".kills") == null){
			conf.set(p.getName() + ".kills", 0);
			conf.set(p.getName() + ".deaths", 0);
			try {
				conf.save(file);
			} catch(IOException ex){
				ex.printStackTrace();
			}
		}

		streak.put(p.getName(), 0);
		cmdExe.enabled.put(p.getName(), true);

		setScoreboard(p);
	}

	@EventHandler
	public void PlayerDeathEvent(PlayerDeathEvent e){
		Player p = e.getEntity().getPlayer();
		if (p.getKiller() != null && p.getKiller() instanceof Player){
			Player k = p.getKiller();
			setKills(k, getKills(k) + 1);
			setStreaks(k);
			setDeaths(p, getDeaths(p) + 1);
			clearStreaks(p);
			setScoreboard(k);
		} else {
			setDeaths(p, getDeaths(p) + 1);
			clearStreaks(p);
		}
		setScoreboard(p);
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent e){
		for(Player p : Bukkit.getOnlinePlayers()){
			setScoreboard(p);
		}
	}

	public void setScoreboard(Player p){
		Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

		Objective objective = board.registerNewObjective("xScores", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		String b = ChatColor.translateAlternateColorCodes('&', a.getString("Stats-Tag"));
		b = b.replace("{NAME}", p.getName());
		objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', b));

		objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Kills-Tag")))).setScore(getKills(p));

		objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Deaths-Tag")))).setScore(getDeaths(p));

		objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Streak-Tag")))).setScore(getStreaks(p));

		if(economy != null){
			objective.getScore(Bukkit.getOfflinePlayer(ChatColor.translateAlternateColorCodes('&', a.getString("Balance-Tag")))).setScore((int) economy.getBalance(p.getName()));
		}
		p.setScoreboard(board);
	}

	public int getKills(Player p){
		return (conf.get(p.getName() + ".kills") != null ? conf.getInt(p.getName() + ".kills") : 0);
	}

	public int getDeaths(Player p){
		return (conf.get(p.getName() + ".deaths") != null ? conf.getInt(p.getName() + ".deaths") : 0);
	}

	public int getStreaks(Player p){
		return (streak.containsKey(p.getName()) ? streak.get(p.getName()) : 0);
	}

	public void clearStreaks(Player p){
		streak.put(p.getName(), 0);
	}

	public double getKDR(Player p){
		int kills = getKills(p);
		int deaths = getDeaths(p);
		int kdr = something(kills, deaths);
		if(kdr != 0){
			kills = kills/kdr;
			deaths = deaths/kdr;
		}
		double ratio = Math.round(((double)kills/(double)deaths) * 100D) / 100D;
		if(kills == 0){
			ratio = 0.0;
		} else if(deaths == 0){
			ratio = kills;
		}
		return ratio;
	}

	public void setKills(Player p, int kills){
		conf.set(p.getName() + ".kills", kills);
		saveFile();
	}

	public void setDeaths(Player p, int deaths){
		conf.set(p.getName() + ".deaths", deaths);
		saveFile();
	}

	public void setStreaks(Player p){
		streak.put(p.getName(), streak.containsKey(p.getName()) ? streak.get(p.getName()) +1 : 1);
	}

	private void setupVault() {
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
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

	protected void saveFile(){
		try {
			conf.save(file);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	private void runSaveTimer(){
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				for(Player p : Bukkit.getOnlinePlayers()){
					conf.set(p.getName() + ".kills", getKills(p));
					conf.set(p.getName() + ".deaths", getDeaths(p));
					saveFile();
				}
			}
		}, 0L, getConfig().getInt("Stats-File-Save-Time"));
	}

	public int something(int a, int b){
		if (b==0) return a;
		return something(b,a % b);
	}

}

