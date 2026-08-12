package me.poma123.globalwarming.commands.subcommands;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.poma123.globalwarming.GlobalWarmingPlugin;
import me.poma123.globalwarming.commands.GlobalWarmingCommand;
import me.poma123.globalwarming.commands.SubCommand;
import com.github.drakescraft_labs.slimefun4.libraries.dough.common.ChatColors;

/**
 * Silencia o vuelve a activar los mensajes del clima, jugador a jugador.
 *
 * POR QUE
 *
 * El plugin escribe por dos vias: los boletines de contaminacion y los avisos de fenomenos
 * (olas de calor, tormentas...). Con el servidor lleno eso es ruido para quien no le interesa, y
 * sin forma de callarlo la unica salida del jugador es pedir que se quite el plugin entero --
 * que es exactamente lo que estaba pasando.
 *
 * La preferencia se guarda en disco: quien lo silencia no tiene que repetirlo en cada reinicio.
 */
public class SilenciarCommand extends SubCommand {

    @ParametersAreNonnullByDefault
    public SilenciarCommand(GlobalWarmingPlugin plugin, GlobalWarmingCommand cmd) {
        super(plugin, cmd, "silenciar",
                "Activa o desactiva los mensajes del clima para ti", false);
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColors.color("&cEste comando solo lo puede usar un jugador."));
            return;
        }

        boolean silenciado = GlobalWarmingPlugin.getSilenciados().alternar(p);

        if (silenciado) {
            p.sendMessage(ChatColors.color(
                    "&7Ya no recibiras mensajes del clima. Usa &e/globalwarming silenciar &7para volver a activarlos."));
        } else {
            p.sendMessage(ChatColors.color("&aVuelves a recibir los mensajes del clima."));
        }
    }
}
