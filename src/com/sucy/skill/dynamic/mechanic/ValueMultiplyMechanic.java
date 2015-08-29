package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;

/**
 * Adds to a cast data value
 */
public class ValueMultiplyMechanic extends EffectComponent
{
    private static final String KEY        = "key";
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
        if (targets.size() == 0 || !settings.has(KEY))
        {
            return false;
        }

        boolean isSelf = targets.size() == 1 && targets.get(0) == caster;
        String key = settings.getString(KEY);
        double multiplier = attr(caster, MULTIPLIER, level, 1, isSelf);
        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        if (data.containsKey(key) && NUMBER.matcher(data.get(key).toString()).matches())
        {
            data.put(key, multiplier * Double.parseDouble(data.get(key).toString()));
        }
        return true;
    }
}
