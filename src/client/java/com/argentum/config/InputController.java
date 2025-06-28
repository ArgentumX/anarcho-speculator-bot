package com.argentum.config;

import com.argentum.logger.ModLogger;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class InputController {

    private static final InputController INSTANCE = new InputController();

    private final KeyBinding buyerModeKey;

    private InputController() {
        buyerModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.speculator.toggle_buyer_mode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "Speculator"
        ));
    }

    public static InputController getInstance() {
        return INSTANCE;
    }

    public void onTick(MinecraftClient client) {
        while (buyerModeKey.wasPressed()) {
            Config.buyerMode = !Config.buyerMode;
            ModLogger.info("Switched buyer mode: {}", Config.buyerMode);
        }
    }
}
