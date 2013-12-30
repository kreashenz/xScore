package me.kreashenz.xscores;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class xScores extends JavaPlugin {

	public HashMap<String, Integer> streak = new HashMap<String, Integer>();

	public Economy economy = null;

	protected File file;
	public FileConfiguration conf;

	protected xScoresCommand cmdExe;

	public void onEnable() {
		saveDefaultConfig();
		saveResource("stats.yml", false);

		file = new File(getDataFolder(), "stats.yml");
		conf = YamlConfiguration.loadConfiguration(file);

		cmdExe = new xScoresCommand(this);

		setupVault();

		getServer().getPluginManager().registerEvents(new Events(this), this);

		getCommand("clearscores").setExecutor(cmdExe);
		getCommand("kdr").setExecutor(cmdExe);
		getCommand("xboard").setExecutor(cmdExe);
		getCommand("xscore").setExecutor(cmdExe);

		try {
			new Metrics(this).start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		runTimer();
	}

	public void setScoreboard(Player p) {
		Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective objective = board.registerNewObjective("xScores", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(format(getConfig().getString("Stats-Tag")).replace("{NAME}", p.getName()));
		objective.getScore(g(getConfig().getString("Kills-Tag"))).setScore(getKills(p));
		objective.getScore(g(getConfig().getString("Deaths-Tag"))).setScore(getDeaths(p));
		objective.getScore(g(getConfig().getString("Streak-Tag"))).setScore(getStreaks(p));
		objective.getScore(g(getConfig().getString("Enderman-Tag"))).setScore((Integer) get(p.getName() + ".enderman"));
		objective.getScore(g(getConfig().getString("CaveSpider-Tag"))).setScore((Integer) get(p.getName() + ".cavespider"));
		objective.getScore(g(getConfig().getString("Creeper-Tag"))).setScore((Integer) get(p.getName() + ".creeper"));
		objective.getScore(g(getConfig().getString("Pigzombie-Tag"))).setScore((Integer) get(p.getName() + ".pigzombie"));
		objective.getScore(g(getConfig().getString("Skeleton-Tag"))).setScore((Integer) get(p.getName() + ".skeleton"));
		objective.getScore(g(getConfig().getString("Spider-Tag"))).setScore((Integer) get(p.getName() + ".spider"));
		objective.getScore(g(getConfig().getString("Zombie-Tag"))).setScore((Integer) get(p.getName() + ".zombie"));
		objective.getScore(g(getConfig().getString("Other-Tag"))).setScore((Integer) get(p.getName() + ".other"));

		if (economy != null) {
			objective.getScore(g(getConfig().getString("Balance-Tag"))).setScore((int) economy.getBalance(p.getName()));
		}
		p.setScoreboard(board);
	}

	public void updateScoreboard(Player p) {
		Scoreboard b = p.getScoreboard();
		if (b.getObjective("xScores") != null) {
			Objective ob = b.getObjective("xScores");
			ob.getScore(g(getConfig().getString("Kills-Tag"))).setScore(getKills(p));
			ob.getScore(g(getConfig().getString("Deaths-Tag"))).setScore(getDeaths(p));
			ob.getScore(g(getConfig().getString("Streak-Tag"))).setScore(getStreaks(p));
			ob.getScore(g(getConfig().getString("Enderman-Tag"))).setScore((Integer) get(p.getName() + ".enderman"));
			ob.getScore(g(getConfig().getString("CaveSpider-Tag"))).setScore((Integer) get(p.getName() + ".cavespider"));
			ob.getScore(g(getConfig().getString("Creeper-Tag"))).setScore((Integer) get(p.getName() + ".creeper"));
			ob.getScore(g(getConfig().getString("Pigzombie-Tag"))).setScore((Integer) get(p.getName() + ".pigzombie"));
			ob.getScore(g(getConfig().getString("Skeleton-Tag"))).setScore((Integer) get(p.getName() + ".skeleton"));
			ob.getScore(g(getConfig().getString("Spider-Tag"))).setScore((Integer) get(p.getName() + ".spider"));
			ob.getScore(g(getConfig().getString("Zombie-Tag"))).setScore((Integer) get(p.getName() + ".zombie"));
			ob.getScore(g(getConfig().getString("Other-Tag"))).setScore((Integer) get(p.getName() + ".other"));
			if (economy != null) {
				ob.getScore(g("Balance-Tag")).setScore((int) economy.getBalance(p.getName()));
			}
		}
		else
			setScoreboard(p);
	}

	public OfflinePlayer g(String name) {
		return Bukkit.getOfflinePlayer(format(name));
	}

	public int getKills(Player p) {
		return (get(p.getName() + ".kills") != null ? conf.getInt(p.getName() + ".kills") : 0);
	}

	public int getDeaths(Player p) {
		return (get(p.getName() + ".deaths") != null ? conf.getInt(p.getName() + ".deaths") : 0);
	}

	public int getStreaks(Player p) {
		return (streak.containsKey(p.getName()) ? streak.get(p.getName()) : 0);
	}

	public void clearStreaks(Player p) {
		streak.put(p.getName(), 0);
	}

	public double getKDR(Player p) {
		int kills = getKills(p);
		int deaths = getDeaths(p);
		int kdr = something(kills, deaths);
		if (kdr != 0) {
			kills = kills / kdr;
			deaths = deaths / kdr;
		}
		double ratio = Math.round(((double) kills / (double) deaths) * 100D) / 100D;
		if (kills == 0) {
			ratio = 0.0;
		}
		else if (deaths == 0) {
			ratio = kills;
		}
		return ratio;
	}

	public void setKills(Player p, int kills) {
		set(p.getName() + ".kills", kills);
	}

	public void setDeaths(Player p, int deaths) {
		set(p.getName() + ".deaths", deaths);
	}

	public void setStreaks(Player p) {
		streak.put(p.getName(), streak.containsKey(p.getName()) ? streak.get(p.getName()) + 1 : 1);
	}

	public void setupVault() {
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		if (vault != null && vault instanceof net.milkbowl.vault.Vault) {
			getLogger().info("Loaded Vault v" + vault.getDescription().getVersion());
			if (!setupEconomy()) {
				getLogger().warning("No economy plugin installed; xScores Scoreboard will not be showing Economy.");
			}
			else {
				getLogger().warning("Vault not loaded, please check your plugins folder or console.");
			}
		}
	}

	public boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		else {
			return false;
		}
		return (economy != null);
	}

	protected void saveFile() {
		try {
			conf.save(file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int something(int a, int b) {
		if (b == 0)
			return a;
		return something(b, a % b);
	}

	public String format(String input) {
		return ChatColor.translateAlternateColorCodes('&', input);
	}

	public Object get(String path) {
		if (conf.get(path) instanceof Integer) {
			return conf.getInt(path);
		}
		if (conf.get(path) instanceof String) {
			return conf.getString(path);
		}
		return conf.get(path);
	}

	public void set(String path, Object value) {
		conf.set(path, value);
		saveFile();
	}

	public void runTimer() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					updateScoreboard(p);
				}
			}
		}, 0l, 20l);
	}

}
