package me.kreashenz.xscores;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class xScoresCommand implements CommandExecutor {

	private Boolean enabled = Boolean.valueOf(true);

	private xScores plugin;
	public xScoresCommand(xScores plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(sender instanceof Player){
			Player p = (Player) sender;
			if (cmd.getName().equalsIgnoreCase("clearscores")){
				if (p.hasPermission("xscores.clearscores")){
					try {
						File file = new File("plugins/xScores/stats.yml");
						FileOutputStream fos = new FileOutputStream(file);
						fos.flush();
						fos.close();
					}catch(IOException ioe){}
					for (Player target : Bukkit.getOnlinePlayers()){plugin.setScoreboard(target);}
					p.sendMessage("§cScores cleared.");
				} else sender.sendMessage("§cYou do not have permission to do this.");
			}
			if(cmd.getName().equalsIgnoreCase("kdr")){
				if(sender.hasPermission("xscores.kdr")){
					DecimalFormat d = new DecimalFormat("##.##");
					if(args.length == 0){
						p.sendMessage("§aYour KDR is : §9" + d.format(plugin.getKDR(p)));
					} else {
						Player t = Bukkit.getPlayer(args[0]);
						if(t != null && t.isOnline()){
							p.sendMessage("§9" + t.getName() + "§a's KDR is §9" + d.format(plugin.getKDR(t)));
						} else p.sendMessage("§cThat player is not found.");
					}
				} else p.sendMessage("§cYou do not have permission to use this command.");
			}
			if(cmd.getName().equalsIgnoreCase("xboard")){
				if(p.hasPermission("xscores.xboard")){
					if(p.getScoreboard() != null && p.getScoreboard().getObjective("test") != null){
						if(enabled){
							p.setScoreboard(newBoard());
							p.sendMessage("§aSuccessfully §cremoved §athe xScores scoreboard. Use §f/xboard §aagain to enable it. §7§oYou may have to wait a little.");
							enabled = Boolean.valueOf(false);
						} else {
							plugin.setScoreboard(p);
							p.sendMessage("§aSuccessfully §cenabled §athe xScores scoreboard. Use §f/xboard §aagain to remove it. §7§oYou may have to wait a little.");
							enabled = Boolean.valueOf(true);
						}
					}
				}
			}
		}
		return true;
	}
	
	private Scoreboard newBoard(){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		return board;
	}
	
}