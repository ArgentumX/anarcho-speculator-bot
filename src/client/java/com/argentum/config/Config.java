package com.argentum.config;

import com.argentum.utils.items.filters.ItemFilter;
import com.argentum.utils.items.filters.components.DurabilityFilter;
import com.argentum.utils.items.filters.components.EnchantmentFilter;
import com.argentum.utils.items.filters.components.PriceFilter;
import com.argentum.utils.items.filters.components.TypeFilter;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    public static boolean buyerMode = false;

    public static Item CONFIRM_ITEM = Items.LIME_STAINED_GLASS_PANE;
    public static int CONFIRM_TICK_TIMEOUT = 40;
    public static long CLICK_DELAY_MS = 200;


    public static Set<ItemFilter> itemFilters = new HashSet<>() {{
        add(new ItemFilter(List.of(
                new TypeFilter(Registries.ITEM.get(new Identifier("minecraft", "diamond_sword"))),
                new DurabilityFilter(70),
                new PriceFilter(1000000000)
        )));

        add(new ItemFilter(List.of(
                new TypeFilter(Registries.ITEM.get(new Identifier("minecraft", "diamond_pickaxe"))),
                new DurabilityFilter(50),
                new PriceFilter(1000000000),
                new EnchantmentFilter(Map.of(Enchantments.EFFICIENCY, 2))
        )));
    }};
}
