package com.argentum.utils.items.filters.components;

import com.argentum.logger.ModLogger;
import com.argentum.utils.items.filters.IFilter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceFilter implements IFilter<ItemStack> {
    private final int maxPrice;
    private final Pattern pricePattern = Pattern.compile("\\$ *Цен[аa] *\\$ *(\\d{1,3}(?:,\\d{3})*)");

    public PriceFilter(int maxPrice) {
        this.maxPrice = maxPrice;
    }

    @Override
    public boolean isValid(ItemStack stack) {
        int price = extractPrice(stack);
        if (price == -1) {
            return false;
        }

        ModLogger.debug("Detected price: {}", price);
        return price <= maxPrice;
    }

    private List<String> extractLore(ItemStack stack) {
        List<String> loreEntries = new ArrayList<>();
        if (!stack.hasNbt() || !stack.getNbt().contains("display", NbtElement.COMPOUND_TYPE)) {
            ModLogger.warn("No NBT or display tag found for item: {}", stack.getName().getString());
            return loreEntries;
        }

        NbtCompound displayNbt = stack.getNbt().getCompound("display");
        if (!displayNbt.contains("Lore", NbtElement.LIST_TYPE)) {
            ModLogger.warn("No lore found for item: {}", stack.getName().getString());
            return loreEntries;
        }

        NbtList loreList = displayNbt.getList("Lore", NbtElement.STRING_TYPE);
        for (int i = 0; i < loreList.size(); i++) {
            String loreEntry = loreList.getString(i);
            Text text = Text.Serializer.fromJson(loreEntry);
            if (text != null) {
                loreEntries.add(text.getString().trim());
            }
        }
        return loreEntries;
    }

    private int extractPrice(ItemStack stack) {
        List<String> lore = extractLore(stack);
        for (String plainText : lore) {
            Matcher matcher = pricePattern.matcher(plainText);
            if (matcher.find()) {
                String priceString = matcher.group(1).replace(",", "").trim();
                return Integer.parseInt(priceString);
            }
        }
        ModLogger.debug("No price found in lore for item: {}", stack.getName());
        return -1;
    }
}
