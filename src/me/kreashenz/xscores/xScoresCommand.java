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

public class xScoresCommand implements CommandExecutor {

	public xScores plugin;
	public xScoresCommand(xScores plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("clearscores")){
			if (sender instanceof Player){
				if (p.hasPermission("xscores.clearscores")){
					try {
						File file = new File("plugins/xScores/config.yml");
						FileOutputStream fos = new FileOutputStream(file);
						fos.flush();
						fos.close();
					}catch(IOException ioe){}
					for (Player target : Bukkit.getOnlinePlayers()){plugin.setScoreboard(target);}
					p.sendMessage("§cScores cleared.");
				} else sender.sendMessage("§cYou do not have permission to do this.");
			} else sender.sendMessage("Player only command!");
		}
		if(cmd.getName().equalsIgnoreCase("kdr")){
			if(sender instanceof Player){
				if(sender.hasPermission("xscores.kdr")){
					DecimalFormat d = new DecimalFormat("##.##");
					if(args.length == 0){
						p.sendMessage("§aYour KDR is : §9" + d.format(plugin.getKDR(p)));
					} else {
						try {
							Player t = Bukkit.getPlayer(args[0]);
							if(t != null && t.isOnline()){
								p.sendMessage("§9" + t.getName() + "§a's KDR is §9" + d.format(plugin.getKDR(t)));
							} else p.sendMessage("§cThat player is not found.");
						} catch(NullPointerException npe){p.sendMessage("§cThat player is not found.");}
					}
				} else p.sendMessage("§cYou do not have permission to use this command.");
			} else p.sendMessage("§cYou have to be a player to use this command.");
		}
		return true;
	}
}