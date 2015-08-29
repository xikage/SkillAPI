package com.sucy.skill.data.io;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.player.*;
import com.sucy.skill.api.skills.Skill;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for managers that handle saving and loading player data
 */
public abstract class IOManager
{
    private static final String
            LIMIT          = "limit",
            ACTIVE         = "active",
            ACCOUNTS       = "accounts",
            ACCOUNT_PREFIX = "acc",
            CLASSES        = "classes",
            SKILLS         = "skills",
            BINDS          = "binds",
            LEVEL          = "level",
            SCHEME         = "scheme",
            TOTAL_EXP      = "total-exp",
            POINTS         = "points",
            SKILL_BAR      = "bar",
            ENABLED        = "enabled",
            SLOTS          = "slots",
            UNASSIGNED     = "e",
            COMBOS         = "combos",
            ATTRIBS        = "attribs",
            ATTRIB_POINTS  = "attrib-points";

    /**
     * API reference
     */
    protected final SkillAPI api;

    /**
     * Initializes a new IO manager
     *
     * @param api SkillAPI reference
     */
    protected IOManager(SkillAPI api)
    {
        this.api = api;
    }

    /**
     * Loads data for the player
     *
     * @param player player to load for
     *
     * @return loaded player data
     */
    public abstract PlayerAccounts loadData(OfflinePlayer player);

    /**
     * Saves the player's data
     *
     * @param data data to save
     */
    public abstract void saveData(PlayerAccounts data);

    /**
     * Saves all player data
     */
    public void saveAll()
    {
        for (PlayerAccounts data : SkillAPI.getPlayerAccountData().values())
        {
            saveData(data);
        }
    }

    /**
     * Loads data from the DataSection for the given player
     *
     * @param player player to load for
     * @param file   DataSection containing the account info
     *
     * @return the loaded player account data
     */
    protected PlayerAccounts load(OfflinePlayer player, DataSection file)
    {
        PlayerAccounts data = new PlayerAccounts(player);
        DataSection accounts = file.getSection(ACCOUNTS);
        if (accounts == null) accounts = file.createSection(ACCOUNTS);
        for (String accountKey : accounts.keys())
        {
            DataSection account = accounts.getSection(accountKey);
            PlayerData acc = data.getData(Integer.parseInt(accountKey.replace(ACCOUNT_PREFIX, "")), player, true);

            // Load scheme
            acc.setScheme(account.getString(SCHEME, "default"));

            // Load classes
            DataSection classes = account.getSection(CLASSES);
            if (classes != null)
            {
                for (String classKey : classes.keys())
                {
                    RPGClass rpgClass = SkillAPI.getClass(classKey);
                    if (rpgClass != null)
                    {
                        PlayerClass c = acc.setClass(rpgClass);
                        DataSection classData = classes.getSection(classKey);
                        int levels = classData.getInt(LEVEL) - 1;
                        if (levels > 0)
                        {
                            c.giveLevels(levels);
                        }
                        c.setPoints(classData.getInt(POINTS));
                        c.setTotalExp(classData.getDouble(TOTAL_EXP));
                    }
                }
            }

            // Load skills
            DataSection skills = account.getSection(SKILLS);
            if (skills != null)
            {
                for (String skillKey : skills.keys())
                {
                    DataSection skill = skills.getSection(skillKey);
                    PlayerSkill skillData = acc.getSkill(skillKey);
                    if (skillData != null)
                    {
                        skillData.addLevels(skill.getInt(LEVEL));
                        skillData.addPoints(skill.getInt(POINTS));
                    }
                }
            }

            // Load binds
            DataSection binds = account.getSection(BINDS);
            if (binds != null)
            {
                for (String bindKey : binds.keys())
                {
                    acc.bind(Material.valueOf(bindKey), acc.getSkill(binds.getString(bindKey)));
                }
            }

            // Load skill bar
            DataSection skillBar = account.getSection(SKILL_BAR);
            PlayerSkillBar bar = acc.getSkillBar();
            if (skillBar != null && bar != null)
            {
                for (String key : skillBar.keys())
                {
                    if (key.equals(ENABLED))
                    {
                        if (bar.isEnabled() != skillBar.getBoolean(key))
                        {
                            bar.toggleEnabled();
                        }
                    }
                    else if (key.equals(SLOTS))
                    {
                        List<String> slots = skillBar.getList(SLOTS);
                        for (String i : slots)
                        {
                            bar.getData().put(Integer.parseInt(i), UNASSIGNED);
                        }
                    }
                    else if (SkillAPI.getSkill(key) != null)
                    {
                        bar.getData().put(skillBar.getInt(key), key);
                    }
                }
                bar.applySettings();
            }
            if (!SkillAPI.getSettings().isSkillBarEnabled() && bar != null && bar.isEnabled()) {
                bar.toggleEnabled();
            }

            // Load combos
            DataSection combos = account.getSection(COMBOS);
            PlayerCombos comboData = acc.getComboData();
            if (combos != null && comboData != null)
            {
                for (String key : combos.keys())
                {
                    Skill skill = SkillAPI.getSkill(key);
                    if (acc.hasSkill(key) && skill != null && skill.canCast())
                    {
                        comboData.setSkill(skill, combos.getInt(key));
                    }
                }
            }

            // Load attributes
            acc.setAttribPoints(account.getInt(ATTRIB_POINTS, 0));
            DataSection attribs = account.getSection(ATTRIBS);
            if (attribs != null)
            {
                for (String key : attribs.keys())
                {
                    acc.getAttributeData().put(key, attribs.getInt(key));
                }
            }
        }
        data.setAccount(file.getInt(ACTIVE, data.getActiveId()));

        return data;
    }

    protected DataSection save(PlayerAccounts data)
    {
        try
        {
            DataSection file = new DataSection();
            file.set(LIMIT, data.getAccountLimit());
            file.set(ACTIVE, data.getActiveId());
            DataSection accounts = file.createSection(ACCOUNTS);
            for (Map.Entry<Integer, PlayerData> entry : data.getAllData().entrySet())
            {
                DataSection account = accounts.createSection(ACCOUNT_PREFIX + entry.getKey());
                PlayerData acc = entry.getValue();

                // Save scheme
                account.set(SCHEME, acc.getScheme());

                // Save classes
                DataSection classes = account.createSection(CLASSES);
                for (PlayerClass c : acc.getClasses())
                {
                    DataSection classSection = classes.createSection(c.getData().getName());
                    classSection.set(LEVEL, c.getLevel());
                    classSection.set(POINTS, c.getPoints());
                    classSection.set(TOTAL_EXP, c.getTotalExp());
                }

                // Save skills
                DataSection skills = account.createSection(SKILLS);
                for (PlayerSkill skill : acc.getSkills())
                {
                    DataSection skillSection = skills.createSection(skill.getData().getName());
                    skillSection.set(LEVEL, skill.getLevel());
                    skillSection.set(POINTS, skill.getPoints());
                }

                // Save binds
                DataSection binds = account.createSection(BINDS);
                for (Map.Entry<Material, PlayerSkill> bind : acc.getBinds().entrySet())
                {
                    if (bind.getKey() == null || bind.getValue() == null) continue;
                    binds.set(bind.getKey().name(), bind.getValue().getData().getName());
                }

                // Save skill bar
                if (acc.getSkillBar() != null)
                {
                    DataSection skillBar = account.createSection(SKILL_BAR);
                    PlayerSkillBar bar = acc.getSkillBar();
                    skillBar.set(ENABLED, bar.isEnabled());
                    skillBar.set(SLOTS, new ArrayList<Integer>(bar.getData().keySet()));
                    for (Map.Entry<Integer, String> slotEntry : bar.getData().entrySet())
                    {
                        if (slotEntry.getValue().equals(UNASSIGNED))
                        {
                            continue;
                        }
                        skillBar.set(slotEntry.getValue(), slotEntry.getKey());
                    }
                }

                // Save combos
                DataSection combos = account.createSection(COMBOS);
                PlayerCombos comboData = acc.getComboData();
                if (combos != null && comboData != null)
                {
                    HashMap<String, Integer> comboMap = comboData.getComboData();
                    for (Map.Entry<String, Integer> combo : comboMap.entrySet())
                    {
                        combos.set(combo.getKey(), combo.getValue());
                    }
                }

                // Save attributes
                account.set(ATTRIB_POINTS, acc.getAttributePoints());
                DataSection attribs = account.createSection(ATTRIBS);
                for (String key : acc.getAttributeData().keySet())
                {
                    attribs.set(key, acc.getAttributeData().get(key));
                }
            }
            return file;
        }
        catch (Exception ex)
        {
            Bukkit.getLogger().info("Failed to save player data for " + data.getPlayer().getName());
            ex.printStackTrace();
            return null;
        }
    }
}
