package com.sucy.skill.cmd;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.language.RPGFilter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * A command that allows a player to profess through classes
 */
public class CmdProfess implements IFunction
{
    private static final String CANNOT_USE     = "cannot-use";
    private static final String INVALID_CLASS  = "invalid-class";
    private static final String PROFESSED      = "professed";
    private static final String CANNOT_PROFESS = "cannot-profess";
    private static final String DISABLED       = "world-disabled";

    /**
     * Runs the command
     *
     * @param cmd    command that was executed
     * @param plugin plugin reference
     * @param sender sender of the command
     * @param args   argument list
     */
    @Override
    public void execute(ConfigurableCommand cmd, Plugin plugin, CommandSender sender, String[] args)
    {
        // Disabled world
        if (sender instanceof Player && !SkillAPI.getSettings().isWorldEnabled(((Player) sender).getWorld()))
        {
            cmd.sendMessage(sender, DISABLED, "&4You cannot use this command in this world");
        }

        // Only players have profession options
        else if (sender instanceof Player)
        {
            if (args.length == 0)
            {
                CommandManager.displayUsage(cmd, sender);
            }
            else
            {
                String name = args[0];
                for (int i = 1; i < args.length; i++) name += args[i];

                PlayerData data = SkillAPI.getPlayerData((Player) sender);
                RPGClass target = SkillAPI.getClass(name);

                // Invalid class
                if (target == null)
                {
                    cmd.sendMessage(sender, INVALID_CLASS, ChatColor.RED + "That is not a valid class");
                }

                // Can profess
                else if (data.canProfess(target))
                {
                    data.profess(target);
                    cmd.sendMessage(sender, PROFESSED, ChatColor.DARK_GREEN + "You are now a " + ChatColor.GOLD + "{class}", RPGFilter.CLASS.setReplacement(target.getName()));
                }

                // Cannot profess
                else
                {
                    cmd.sendMessage(sender, CANNOT_PROFESS, ChatColor.RED + "You cannot profess to this class currently");
                }
            }
        }

        // Console doesn't have profession options
        else
        {
            cmd.sendMessage(sender, CANNOT_USE, ChatColor.RED + "This cannot be used by the console");
        }
    }
}
