package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Applies a damage immunity flag to each target
 */
public class ImmunityMechanic extends EffectComponent
{
    private static final String TYPE    = "type";
    private static final String SECONDS = "seconds";

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
        if (targets.size() == 0 || !settings.has(TYPE))
        {
            return false;
        }

        boolean isSelf = targets.size() == 1 && targets.get(0) == caster;
        String key = settings.getString(TYPE);
        double seconds = attr(caster, SECONDS, level, 3.0, isSelf);
        int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets)
        {
            FlagManager.addFlag(target, "immune:" + key.toUpperCase().replace(" ", "_"), ticks);
        }
        return targets.size() > 0;
    }
}
