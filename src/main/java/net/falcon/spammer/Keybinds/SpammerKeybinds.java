package net.falcon.spammer.Keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.falcon.spammer.Screens.ConfigListScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SpammerKeybinds {
    public static KeyBinding openConfigGui;

    public static void registerKeybinds() {
        openConfigGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.spammer.openConfigGui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6, // F6 key
                "category.spammer.general"
        ));
    }

    public static void handleKeybinds(MinecraftClient client) {
        if (openConfigGui.wasPressed()) {
            client.execute(() -> {
                client.setScreen(new ConfigListScreen(client.currentScreen));
            });
        }
    }
}
