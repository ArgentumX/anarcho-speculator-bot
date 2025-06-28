package com.argentum.utils.items.filters.components;

import com.argentum.utils.items.filters.IFilter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TypeFilter implements IFilter<ItemStack> {
    private final Item item;

    public TypeFilter(Item item) {
        this.item = item;
    }

    @Override
    public boolean isValid(ItemStack stack) {
        return stack.getItem() == item;
    }
}