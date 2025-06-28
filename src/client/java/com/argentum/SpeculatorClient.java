package com.argentum;

import com.argentum.bot.Bot;
import com.argentum.config.InputController;
import com.argentum.logger.ModLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SpeculatorClient implements ClientModInitializer {
	private final Bot bot = new Bot();
	@Override
	public void onInitializeClient() {
		ModLogger.info("System. power. up.");
		ClientTickEvents.END_CLIENT_TICK.register(InputController.getInstance()::onTick);
		ClientTickEvents.END_CLIENT_TICK.register(bot::onTick);
	}
}



