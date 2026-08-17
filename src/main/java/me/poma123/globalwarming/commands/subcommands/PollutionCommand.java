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
        super(plugin, cmd, "pollution", "Le permite modificar manualmente los valores de contaminación.", false);
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        if (sender.hasPermission("globalwarming.command.pollution") || !(sender instanceof Player)) {
            if (args.length > 2) {
                World world = Bukkit.getWorld(args[2]);

                if (world != null && GlobalWarmingPlugin.getRegistry().isWorldEnabled(world.getName())) {
                    if (args[1].equalsIgnoreCase("get")) {
                        double pollution = TemperatureManager.fixDouble(PollutionManager.getPollutionInWorld(world), 2);

                        sender.sendMessage(ChatColors.color("&bmundo &a" + world.getName() + " &bEl valor de la contaminación es: &a" + pollution));
                    } else if (args[1].equalsIgnoreCase("set")) {
                        if (args.length > 3) {
                            setPollution(sender, world, args);
                        } else {
                            sender.sendMessage(ChatColors.color("&4uso: &c/globalwarming pollution <set> <world> <amount>"));
                        }
                    }
                } else {
                    sender.sendMessage(ChatColors.color("&4Este comando no se puede utilizar en este mundo."));
                }
            } else {
                sender.sendMessage(ChatColors.color("&uso: &c/globalwarming pollution <set|get> <world>"));
            }
        } else {
            sender.sendMessage(ChatColors.color("&4No tienes permisos suficientes para ejecutar este comando"));
        }
    }

    private void setPollution(CommandSender sender, World world, String[] args) {
        int amount = parseAmount(args);

        if (amount > -1) {
            if (PollutionManager.setPollutionInWorld(world, amount)) {
                sender.sendMessage(ChatColors.color("&bconjunto mundial '&a%world%&b' El valor de la contaminación es '&a%newValue%&b'").replace("%newValue%", amount + "").replace("%world%", world.getName()));
            } else {
                // This is nearly impossible, but let us check
                sender.sendMessage(ChatColors.color("&4Este comando no se puede utilizar en este mundo."));
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
