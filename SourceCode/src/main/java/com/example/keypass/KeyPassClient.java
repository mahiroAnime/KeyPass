package com.example.keypass;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public final class KeyPassClient implements ClientModInitializer {
    public static final String MOD_ID = "keypass";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static int pendingTicks = -1;
    private static String pendingCommand;

    @Override
    public void onInitializeClient() {
        KeyPassConfig.load();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof TitleScreen)) return;

            for (ClickableWidget widget : Screens.getButtons(screen)) {
                if (!(widget instanceof ButtonWidget button)) continue;
                if (!button.getMessage().getString().equals(Text.translatable("menu.multiplayer").getString())) continue;

                int x = button.getX() + button.getWidth() + 4;
                int y = button.getY();

                Screens.getButtons(screen).add(ButtonWidget.builder(
                        Text.literal("K"),
                        b -> client.setScreen(new KeyPassScreen(screen))
                ).dimensions(x, y, 20, button.getHeight()).tooltip(
                        net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal("KeyPass"))
                ).build());
                break;
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerInfo server = client.getCurrentServerEntry();
            if (server == null || server.address == null) return;

            KeyPassConfig.Entry entry = KeyPassConfig.find(server.address);
            if (entry == null || entry.command().isBlank()) return;

            pendingCommand = entry.command().trim();
            pendingTicks = 20; // about 1 second after joining
            LOGGER.info("KeyPass: command scheduled for {}", server.address);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingTicks < 0) return;
            if (client.player == null || client.getNetworkHandler() == null) return;

            if (--pendingTicks <= 0) {
                String command = pendingCommand;
                pendingCommand = null;
                pendingTicks = -1;

                if (command != null && !command.isBlank()) {
                    command = command.trim();
                    if (command.startsWith("/")) command = command.substring(1);
                    if (!command.isBlank()) {
                        client.getNetworkHandler().sendChatCommand(command);
                        LOGGER.info("KeyPass: sent configured command");
                    }
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            pendingTicks = -1;
            pendingCommand = null;
        });
    }
}
