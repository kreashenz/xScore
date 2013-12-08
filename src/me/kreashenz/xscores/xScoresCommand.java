package me.kreashenz.xscores;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class xScoresCommand implements CommandExecutor {

	protected HashMap<String, Boolean> enabled = new HashMap<String, Boolean>();

	private xScores plugin;

	public xScoresCommand(xScores plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String commandLabel, String[] args) {
		if (s instanceof Player) {
			Player p = (Player) s;
			if (cmd.getName().equalsIgnoreCase("clearscores")) {
				if (p.hasPermission("xscores.clearscores")) {
					try {
						FileOutputStream fos = new FileOutputStream(plugin.file);
						fos.flush();
						fos.close();
					}
					catch (IOException ioe) {
					}
					for (Player target : Bukkit.getOnlinePlayers()) {
						plugin.setScoreboard(target);
					}
					p.sendMessage("§cScores cleared.");
				}
				else
					p.sendMessage("§cYou do not have permission to do this.");
			}
			if (cmd.getName().equalsIgnoreCase("kdr")) {
				if (p.hasPermission("xscores.kdr")) {
					if (args.length == 0) {
						p.sendMessage("§aYour KDR is : §9" + plugin.getKDR(p));
					}
					else {
						Player t = Bukkit.getPlayer(args[0]);
						if (t != null) {
							p.sendMessage("§9" + t.getName() + "§a's KDR is §9" + plugin.getKDR(t));
						}
						else
							p.sendMessage("§cThat player is not found.");
					}
				}
				else
					p.sendMessage("§cYou do not have permission to use this command.");
			}
			if (cmd.getName().equalsIgnoreCase("xboard")) {
				if (p.hasPermission("xscores.xboard")) {
					if (p.getScoreboard() != null && p.getScoreboard().getObjective("xScores") != null) {
						if (enabled.get(p.getName()).equals(true)) {
							p.setScoreboard(newBoard());
							p.sendMessage("§aSuccessfully §cdisabled §athe xScores scoreboard. Use §c/xboard §aagain to enable it.");
							enabled.put(p.getName(), false);
						}
						else if (enabled.get(p.getName()).equals(false)) {
							plugin.setScoreboard(p);
							p.sendMessage("§aSuccessfully §cenabled §athe xScores scoreboard. Use §c/xboard §aagain to remove it.");
							enabled.put(p.getName(), true);
						}
						else {
							p.sendMessage("§cSomething is wrong. Uh oh.");
						}
					}
					else
						p.sendMessage("§cxScore boards are not active.");
				}
				else
					p.sendMessage("§cYou do not have permission to use this command.");
			}
			if (cmd.getName().equalsIgnoreCase("xscore")) {
				if (p.hasPermission("xscores.admin")) {
					if (args.length != 1) {
						p.sendMessage("§cInvalid arguments. /xscore <reload | save>");
					}
					else {
						if (args[0].equalsIgnoreCase("reload")) {
							plugin.reloadConfig();
							for (Player ps : Bukkit.getOnlinePlayers()) {
								plugin.setScoreboard(ps);
							}
							p.sendMessage("§aSuccessfully reloaded config.");
						}
						else if (args[0].equalsIgnoreCase("save")) {
							plugin.saveConfig();
							plugin.saveFile();
							p.sendMessage("§aSuccessfully saved configs.");
						}
						else {
							p.sendMessage("§cInvalid arguments. /xscore <reload | save>");
						}
					}
				}
				else
					p.sendMessage("§cYou do not have permission to use this command.");
			}
		}
		return true;
	}

	private Scoreboard newBoard() {
		Scoreboard board = plugin.getServer().getScoreboardManager().getNewScoreboard();
		board.registerNewObjective("xScores", "dummy");
		return board;
	}

}