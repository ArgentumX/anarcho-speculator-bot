package com.argentum.utils.items.filters;

import com.argentum.logger.ModLogger;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemFilter implements IFilter<ItemStack> {
    private final List<IFilter<ItemStack>> filters;

    public ItemFilter(List<IFilter<ItemStack>> filters) {
        this.filters = filters;
    }

    @Override
    public boolean isValid(ItemStack stack) {
        for (IFilter<ItemStack> filter : filters) {
            if (!filter.isValid(stack)) {
                return false;
            }
        }
        ModLogger.debug("Item passed all checks: {}", stack.getName().getString());
        return true;
    }
}