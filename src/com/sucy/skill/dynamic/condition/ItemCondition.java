package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * A condition for dynamic skills that requires the target to have a specified held item
 */
public class ItemCondition extends EffectComponent
{
    private static final String MATERIAL = "material";

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
        ArrayList<LivingEntity> list = new ArrayList<LivingEntity>();
        String item = settings.getString(MATERIAL, "").toUpperCase().replace(" ", "_");

        for (LivingEntity target : targets)
        {
            if (target.getEquipment() == null || target.getEquipment().getItemInHand() == null)
            {
                continue;
            }
            String hand = target.getEquipment().getItemInHand().getType().name();
            if (hand.equals(item))
            {
                list.add(target);
            }
        }

        return list.size() > 0 && executeChildren(caster, level, list);
    }
}
