package com.argentum.bot;

import com.argentum.config.Config;
import com.argentum.logger.ModLogger;
import com.argentum.utils.items.filters.ItemFilter;
import com.argentum.utils.time.TickTimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot {
    private final Queue<Integer> clickQueue = new LinkedList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final TickTimer timer = new TickTimer();

    private boolean wasChestOpen = false;
    private boolean needsReanalysis = false;
    private boolean awaitingConfirmation = false;
    private boolean isProcessingQueue = false;

    private int tickCounter = 0;

    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        if (!timer.isTimePassed()) {
            timer.handleTick();
            return;
        }

        if (!Config.buyerMode) return;

        boolean chestScreenOpen = client.currentScreen instanceof GenericContainerScreen;

        if (chestScreenOpen) {
            handleChestInteraction(client);
        }

        wasChestOpen = chestScreenOpen;
    }

    private void handleChestInteraction(MinecraftClient client) {
        if (!wasChestOpen || needsReanalysis) {
            analyzeChestInventory(client);
            needsReanalysis = false;
        }

        if (awaitingConfirmation) {
            handleConfirmationClick(client);
        }

        if (clickQueue.isEmpty() && wasChestOpen && !awaitingConfirmation) {
            reloadMarket();
            timer.wait(10);
            needsReanalysis = true;
        }
    }
    private void handleConfirmationClick(MinecraftClient client) {
        boolean clicked = clickConfirm(client);

        if (clicked) {
            tickCounter = 0;
            return;
        }

        tickCounter++;
        if (tickCounter >= Config.CONFIRM_TICK_TIMEOUT) {
            awaitingConfirmation = false;
            tickCounter = 0;
            ModLogger.info("Failed to find confirm button in time.");
        }
    }


    private void processClickQueue() {
        isProcessingQueue = true;
        scheduleNextClick(Config.CLICK_DELAY_MS);
    }

    private void scheduleNextClick(long delay) {
        scheduler.schedule(() -> {
            if (!clickQueue.isEmpty()) {
                Integer slotIndex = clickQueue.poll();
                if (slotIndex != null) {
                    clickChestWithConfirm(slotIndex);
                    // Планируем следующий клик с задержкой
                    scheduleNextClick(delay);
                } else {
                    isProcessingQueue = false;
                }
            } else {
                isProcessingQueue = false;
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void reloadMarket(){
        clickChest(49);
    }

    private boolean clickConfirm(MinecraftClient client) {
        if (!(client.currentScreen instanceof GenericContainerScreen chestScreen)) {
            return false;
        }
        GenericContainerScreenHandler handler = chestScreen.getScreenHandler();
        Inventory chestInventory = handler.getInventory();

        for (int i = 0; i < chestInventory.size(); i++) {
            ItemStack stack = chestInventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Config.CONFIRM_ITEM) {
                clickQueue.clear();
                clickQueue.offer(i);
                processClickQueue();
                return true;
            }
        }
        return false;
    }

    private void analyzeChestInventory(MinecraftClient client) {
        if (!(client.currentScreen instanceof GenericContainerScreen chestScreen)) {
            return;
        }

        GenericContainerScreenHandler handler = chestScreen.getScreenHandler();
        Inventory chestInventory = handler.getInventory();

        clickQueue.clear();

        for (int slotIndex = 0; slotIndex < chestInventory.size(); slotIndex++) {
            ItemStack stack = chestInventory.getStack(slotIndex);

            if (stack.isEmpty()) continue;

            for (ItemFilter filter : Config.itemFilters) {
                if (filter.isValid(stack)) {
                    ModLogger.info("Found valid item: {}", stack.getName().getString());
                    clickQueue.offer(slotIndex);
                    break;
                }
            }

            if (!clickQueue.isEmpty()) {
                break;
            }
        }

        if (!clickQueue.isEmpty() && !isProcessingQueue) {
            processClickQueue();
        }
    }

    private void clickChestWithConfirm(int slotIndex){
        clickChest(slotIndex);
        awaitingConfirmation = true;
    }

    private void clickChest(int slotIndex) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof GenericContainerScreen chestScreen)) {
            ModLogger.warn("Unable to click: no open chest");
            return;
        }
        GenericContainerScreenHandler handler = chestScreen.getScreenHandler();
        int syncId = handler.syncId;
        client.interactionManager.clickSlot(syncId, slotIndex, 0, SlotActionType.PICKUP, client.player);
        ModLogger.info("Emulated chest click {}", slotIndex);
    }


}
