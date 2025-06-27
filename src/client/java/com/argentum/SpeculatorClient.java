package com.argentum;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpeculatorClient implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Speculator");
	private final Set<ItemFilter> itemFilters;
	private boolean wasChestOpen = false;
	private boolean needsReanalysis = false;
	private boolean awaitingConfirmation = false;
	private int tickCounter = 0;
	private final TickTimer timer = new TickTimer();
	private final Item CONFIRM_ITEM = Items.LIME_STAINED_GLASS_PANE;
	private final int CONFIRM_TICK_TIMEOUT = 100;

	public SpeculatorClient() {
		itemFilters = new HashSet<>();
		itemFilters.add(new ItemFilter(
				Registries.ITEM.get(new Identifier("minecraft", "diamond_sword")),
				70,
				150000000
		));
		itemFilters.add(new ItemFilter(
				Registries.ITEM.get(new Identifier("minecraft", "diamond_pickaxe")),
				50,
				150000000
		));
	}
	private final Queue<Integer> clickQueue = new LinkedList<>();
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private boolean isProcessingQueue = false;
	private static final long CLICK_DELAY_MS = 200;

	@Override
	public void onInitializeClient() {
		LOGGER.info("System. power. up.");
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;
			if (!timer.isTimePassed()) {
				timer.handleTick();
				return;
			}

			boolean chestScreenOpen = client.currentScreen instanceof GenericContainerScreen;

			if (chestScreenOpen) {
				handleChestInteraction(client);
			}

			wasChestOpen = chestScreenOpen;
		});
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
		if (tickCounter >= CONFIRM_TICK_TIMEOUT) {
			awaitingConfirmation = false;
			tickCounter = 0;
			LOGGER.info("Failed to find confirm button in time.");
		}
	}


	private void processClickQueue() {
		isProcessingQueue = true;
		scheduleNextClick(CLICK_DELAY_MS);
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
			if (!stack.isEmpty() && stack.getItem() == CONFIRM_ITEM) {
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

			for (ItemFilter filter : itemFilters) {
				if (filter.isValid(stack)) {
					LOGGER.info("Found valid item: {}", stack.getName().getString());
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
			LOGGER.warn("Unable to click: no open chest");
			return;
		}
		GenericContainerScreenHandler handler = chestScreen.getScreenHandler();
		int syncId = handler.syncId;
		client.interactionManager.clickSlot(syncId, slotIndex, 0, SlotActionType.PICKUP, client.player);
		LOGGER.info("Emulated chest click {}", slotIndex);
	}
}



