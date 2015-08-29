package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deals damage based on a held item's lore to each target
 */
public class DamageLoreMechanic extends EffectComponent
{
    private static final String REGEX      = "regex";
    private static final String MULTIPLIER = "multiplier";

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets)
    {
        boolean isSelf = targets.size() == 1 && targets.get(0) == caster;
        String regex = settings.getString(REGEX, "Damage: {value}");
        regex = regex.replace("{value}", "([0-9]+)");
        Pattern pattern = Pattern.compile(regex);
        double m = attr(caster, MULTIPLIER, level, 1.0, isSelf);
        boolean worked = false;
        for (LivingEntity target : targets)
        {
            if (target.getEquipment() == null || target.getEquipment().getItemInHand() == null)
            {
                continue;
            }
            ItemStack hand = caster.getEquipment().getItemInHand();
            if (!hand.hasItemMeta() || !hand.getItemMeta().hasLore())
            {
                continue;
            }
            List<String> lore = hand.getItemMeta().getLore();
            for (String line : lore)
            {
                line = ChatColor.stripColor(line);
                Matcher matcher = pattern.matcher(line);
                if (matcher.find())
                {
                    String value = matcher.group(1);
                    try
                    {
                        double base = Double.parseDouble(value);
                        if (base * m > 0)
                        {
                            skill.damage(target, base * m, caster);
                            worked = true;
                            break;
                        }
                    }
                    catch (Exception ex)
                    {
                        // Not a valid value
                    }
                }
            }
        }
        return worked;
    }
}
