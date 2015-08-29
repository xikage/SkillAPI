package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Plays a particle effect
 */
public class SoundMechanic extends EffectComponent
{
    private static final String SOUND  = "sound";
    private static final String VOLUME = "volume";
    private static final String PITCH  = "pitch";

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
        if (targets.size() == 0)
        {
            return false;
        }

        try
        {
            Sound sound = Sound.valueOf(settings.getString(SOUND, "").toUpperCase().replace(" ", "_"));
            float volume = (float) settings.getDouble(VOLUME, 100.0) / 100;
            float pitch = (float) settings.getDouble(PITCH, 0.0);
            for (LivingEntity target : targets)
            {
                target.getWorld().playSound(target.getLocation(), sound, volume, pitch);
            }
            return targets.size() > 0;
        }
        catch (Exception ex)
        {
            return false;
        }
    }
}
