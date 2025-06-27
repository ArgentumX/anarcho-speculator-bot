package com.argentum;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ItemFilter {
    private static final Logger LOGGER = LogManager.getLogger("ItemFilter");
    private final Item item;
    private final int minDurabilityPercent;
    private final int maxPrice;
    private final Pattern pricePattern =  Pattern.compile("\\$ *Цен[аa] *\\$ *(\\d{1,3}(?:,\\d{3})*)");

    public ItemFilter(Item item, int minDurabilityPercent, int maxPrice) {
        this.item = item;
        this.minDurabilityPercent = minDurabilityPercent;
        this.maxPrice = maxPrice;
    }

    public boolean isValid(ItemStack stack) {
        if (!isValidType(stack)) {
            return false;
        }
        if (!isValidDurability(stack)) {
            LOGGER.info("Durability too low: {}", stack.getName().getString());
            return false;
        }

        if (!isValidPrice(stack)){
            LOGGER.info("Item too expensive: {}", stack.getName().getString());
            return false;
        }

        LOGGER.info("Item passed all checks: {}", stack.getName().getString());
        return true;
    }

    public boolean isValidType(ItemStack stack){
        return stack.getItem() == item;
    }

    public boolean isValidDurability(ItemStack stack){
        if (stack.isDamageable()) {
            int maxDamage = stack.getMaxDamage();
            int currentDamage = stack.getDamage();
            int durabilityPercent = ((maxDamage - currentDamage) * 100) / maxDamage;
            return durabilityPercent >= minDurabilityPercent;
        }
        return true;
    }

    public boolean isValidPrice(ItemStack stack){
        int price = extractPrice(stack);
        if (price == -1) {
            return false;
        }

        LOGGER.info("Detected price: {}", price);
        return price <= maxPrice;
    }

    private List<String> extractLore(ItemStack stack) {
        List<String> loreEntries = new ArrayList<>();
        if (!stack.hasNbt() || !stack.getNbt().contains("display", NbtElement.COMPOUND_TYPE)) {
            LOGGER.warn("No NBT or display tag found for item: {}", stack.getName().getString());
            return loreEntries;
        }

        NbtCompound displayNbt = stack.getNbt().getCompound("display");
        if (!displayNbt.contains("Lore", NbtElement.LIST_TYPE)) {
            LOGGER.warn("No lore found for item: {}", stack.getName().getString());
            return loreEntries;
        }

        NbtList loreList = displayNbt.getList("Lore", NbtElement.STRING_TYPE);
        for (int i = 0; i < loreList.size(); i++) {
            String loreEntry = loreList.getString(i);
            Text text = Text.Serializer.fromJson(loreEntry);
            assert text != null;
            loreEntries.add(text.getString().trim());
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
        LOGGER.info("No price found in lore for item: {}", stack.getName());
        return -1;
    }
}