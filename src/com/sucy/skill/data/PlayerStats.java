package com.sucy.skill.data;

import com.rit.sucy.config.FilterType;
import com.rit.sucy.scoreboard.StatHolder;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stat provider for the MCCore stat scoreboard
 */
public class PlayerStats implements StatHolder
{

    private PlayerClass player;

    /**
     * Constructor
     *
     * @param player player to show stats for
     */
    public PlayerStats(PlayerClass player)
    {
        this.player = player;
    }

    /**
     * @return map of stats for the scoreboard
     */
    @Override
    public ArrayList<OfflinePlayer> getStats()
    {
        ArrayList<OfflinePlayer> stats = new ArrayList<OfflinePlayer>();
        stats.add(statPlayers.get(HEALTH));
        if (SkillAPI.getSettings().isManaEnabled())
        {
            stats.add(player.getData().getManaPlayer());
        }
        stats.add(statPlayers.get(POINTS));
        stats.add(statPlayers.get(LEVEL));
        stats.add(statPlayers.get(EXP));
        if (SkillAPI.getSettings().isAttributesEnabled())
        {
            stats.add(statPlayers.get(ATTRIB));
        }

        return stats;
    }

    /**
     * @return populated list of values
     */
    @Override
    public ArrayList<Integer> getValues()
    {
        double health = player.getPlayerData().getPlayer().getHealth();
        ArrayList<Integer> values = new ArrayList<Integer>();
        values.add((int) health);
        if (SkillAPI.getSettings().isManaEnabled())
        {
            values.add((int) player.getPlayerData().getMana());
        }
        values.add(player.getPoints());
        values.add(player.getLevel());
        values.add((int) player.getExp());
        if (SkillAPI.getSettings().isAttributesEnabled())
        {
            values.add(player.getPlayerData().getAttributePoints());
        }

        return values;
    }

    private static final String BASE   = "Stats.";
    private static final String EXP    = "exp";
    private static final String HEALTH = "health";
    private static final String LEVEL  = "level";
    private static final String POINTS = "points";
    private static final String ATTRIB = "attrib";

    private static final HashMap<String, OfflinePlayer> statPlayers = new HashMap<String, OfflinePlayer>();

    /**
     * Initializes the offline players used by the scoreboard. This is done
     * by the API on startup so do not call this method.
     */
    public static void init()
    {
        if (statPlayers.size() == 0)
        {
            load(EXP, BASE + EXP);
            load(HEALTH, BASE + HEALTH);
            load(LEVEL, BASE + LEVEL);
            load(POINTS, BASE + POINTS);
            load(ATTRIB, BASE + ATTRIB);
        }
    }

    /**
     * Clears the initialized offline players. This is done by the API
     * upon disable so do not call this method.
     */
    public static void clear()
    {
        statPlayers.clear();
    }

    private static void load(String key, String node)
    {
        statPlayers.put(key, Bukkit.getServer().getOfflinePlayer(SkillAPI.getLanguage().getMessage(node, true, FilterType.COLOR).get(0)));
    }
}
