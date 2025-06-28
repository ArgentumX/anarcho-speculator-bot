package com.argentum.utils.items.filters.components;

import com.argentum.utils.items.filters.IFilter;
import net.minecraft.item.ItemStack;

public class DurabilityFilter implements IFilter<ItemStack> {
    private final int minDurabilityPercent;

    public DurabilityFilter(int minDurabilityPercent) {
        this.minDurabilityPercent = minDurabilityPercent;
    }

    @Override
    public boolean isValid(ItemStack stack) {
        if (stack.isDamageable()) {
            int maxDamage = stack.getMaxDamage();
            int currentDamage = stack.getDamage();
            int durabilityPercent = ((maxDamage - currentDamage) * 100) / maxDamage;
            return durabilityPercent >= minDurabilityPercent;
        }
        return true;
    }
}