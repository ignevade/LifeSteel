package me.evade.LifeSteel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
public class LifestealCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    public LifestealCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setmaxhealth <player> <amount>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }
        try {
            double amount = Double.parseDouble(args[1]);
            if (amount < 1.0) {
                amount = 1.0;
            }
            target.setMaxHealth(amount);
            sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s max health to " + amount);
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number: " + args[1]);
            return true;
        }
    }
}