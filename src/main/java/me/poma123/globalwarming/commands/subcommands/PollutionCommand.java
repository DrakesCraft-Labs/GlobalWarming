package me.poma123.globalwarming.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.drakescraft_labs.slimefun4.libraries.dough.common.ChatColors;
import com.github.drakescraft_labs.slimefun4.libraries.dough.common.CommonPatterns;

import me.poma123.globalwarming.GlobalWarmingPlugin;
import me.poma123.globalwarming.TemperatureManager;
import me.poma123.globalwarming.api.PollutionManager;
import me.poma123.globalwarming.commands.GlobalWarmingCommand;
import me.poma123.globalwarming.commands.SubCommand;

class PollutionCommand extends SubCommand {

    PollutionCommand(GlobalWarmingPlugin plugin, GlobalWarmingCommand cmd) {
        super(plugin, cmd, "pollution", "Permite cambiar a mano el valor de contaminación", false);
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        if (sender.hasPermission("globalwarming.command.pollution") || !(sender instanceof Player)) {
            if (args.length > 2) {
                World world = Bukkit.getWorld(args[2]);

                if (world != null && GlobalWarmingPlugin.getRegistry().isWorldEnabled(world.getName())) {
                    if (args[1].equalsIgnoreCase("get")) {
                        double pollution = TemperatureManager.fixDouble(PollutionManager.getPollutionInWorld(world), 2);

                        sender.sendMessage(ChatColors.color("&bMundo &a" + world.getName() + " &btiene una contaminación de: &a" + pollution));
                    } else if (args[1].equalsIgnoreCase("set")) {
                        if (args.length > 3) {
                            setPollution(sender, world, args);
                        } else {
                            sender.sendMessage(ChatColors.color("&4Uso: &c/globalwarming pollution <set> <mundo> <cantidad>"));
                        }
                    }
                } else {
                    sender.sendMessage(ChatColors.color("&4Ese comando no se puede usar en este mundo"));
                }
            } else {
                sender.sendMessage(ChatColors.color("&Uso: &c/globalwarming pollution <set|get> <mundo>"));
            }
        } else {
            sender.sendMessage(ChatColors.color("&4No tienes permiso para usar este comando"));
        }
    }

    private void setPollution(CommandSender sender, World world, String[] args) {
        int amount = parseAmount(args);

        if (amount > -1) {
            if (PollutionManager.setPollutionInWorld(world, amount)) {
                sender.sendMessage(ChatColors.color("&bLa contaminación del mundo '&a%world%&b' ahora es '&a%newValue%&b'").replace("%newValue%", amount + "").replace("%world%", world.getName()));
            } else {
                // This is nearly impossible, but let us check
                sender.sendMessage(ChatColors.color("&4Ese comando no se puede usar en este mundo"));
            }
        } else {
            sender.sendMessage(ChatColors.color("&4%amount% &cno es un valor válido").replace("%amount%", amount + ""));
        }
    }

    private int parseAmount(String[] args) {
        int amount = -1;

        if (args.length == 4 && CommonPatterns.NUMERIC.matcher(args[3]).matches()) {
            amount = Integer.parseInt(args[3]);
        }

        return amount;
    }
}
