package com.argentum.utils.items.filters.components;

import com.argentum.utils.items.filters.IFilter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class EnchantmentFilter implements IFilter<ItemStack> {
    private final Map<Enchantment, Integer> requiredEnchantments;

    public EnchantmentFilter(Map<Enchantment, Integer> requiredEnchantments) {
        if (requiredEnchantments == null || requiredEnchantments.isEmpty()) {
            throw new IllegalArgumentException("Map of required enchantments cannot be null or empty");
        }
        this.requiredEnchantments = requiredEnchantments;
    }

    @Override
    public boolean isValid(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        for (Map.Entry<Enchantment, Integer> entry : requiredEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int requiredLevel = entry.getValue();
            if (EnchantmentHelper.getLevel(enchantment, stack) < requiredLevel) {
                return false;
            }
        }
        return true;
    }
}