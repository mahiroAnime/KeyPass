package com.example.keypass;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class KeyPassScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget ipField;
    private TextFieldWidget commandField;
    private int selected = -1;

    public KeyPassScreen(Screen parent) {
        super(Text.literal("KeyPass"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        int left = center - 130;
        int top = 55;

        ipField = new TextFieldWidget(textRenderer, left, top + 28, 260, 20, Text.literal("IP"));
        ipField.setMaxLength(255);
        addDrawableChild(ipField);

        commandField = new TextFieldWidget(textRenderer, left, top + 78, 260, 20, Text.literal("Command"));
        commandField.setMaxLength(256);
        addDrawableChild(commandField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> saveSelected())
                .dimensions(left, top + 112, 84, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("New"), b -> newEntry())
                .dimensions(left + 88, top + 112, 84, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), b -> deleteSelected())
                .dimensions(left + 176, top + 112, 84, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
                .dimensions(left, top + 140, 260, 20).build());

        refreshEditor();
    }

    private void saveSelected() {
        String ip = ipField.getText().trim();
        String command = commandField.getText().trim();
        if (ip.isBlank() || command.isBlank()) return;

        if (selected >= 0 && selected < KeyPassConfig.entries().size()) {
            KeyPassConfig.set(selected, ip, command);
        } else {
            KeyPassConfig.add(ip, command);
            selected = KeyPassConfig.entries().size() - 1;
        }
        refreshEditor();
    }

    private void newEntry() {
        selected = -1;
        ipField.setText("");
        commandField.setText("");
        ipField.setFocused(true);
    }

    private void deleteSelected() {
        if (selected < 0 || selected >= KeyPassConfig.entries().size()) return;
        KeyPassConfig.remove(selected);
        if (selected >= KeyPassConfig.entries().size()) selected = KeyPassConfig.entries().size() - 1;
        refreshEditor();
    }

    private void select(int index) {
        selected = index;
        refreshEditor();
    }

    private void refreshEditor() {
        if (selected >= 0 && selected < KeyPassConfig.entries().size()) {
            KeyPassConfig.Entry entry = KeyPassConfig.entries().get(selected);
            ipField.setText(entry.ip());
            commandField.setText(entry.command());
        } else {
            ipField.setText("");
            commandField.setText("");
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Do not call Screen.renderBackground here: in Minecraft 1.21.7 it may apply the
        // background blur, and FancyMenu can already blur the same frame. Drawing our own
        // dark background avoids the "Can only blur once per frame" crash.
        context.fill(0, 0, width, height, 0xFF101010);

        // Let Minecraft/FancyMenu render the screen background and widgets first.
        // Drawing our labels before super.render() makes them get covered by the
        // screen background on Minecraft 1.21.7.
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, Text.literal("KeyPass"), width / 2, 18, 0xFFFFFF);

        int center = width / 2;
        int left = center - 130;
        int top = 55;
        context.drawTextWithShadow(textRenderer, Text.literal("IP / server address"), left, top + 14, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("Command (with arguments)"), left, top + 64, 0xFFFFFF);

        int listX = Math.max(10, left - 220);
        int listY = 55;
        context.drawTextWithShadow(textRenderer, Text.literal("Servers"), listX, listY - 18, 0xFFFFFF);

        for (int i = 0; i < KeyPassConfig.entries().size(); i++) {
            KeyPassConfig.Entry entry = KeyPassConfig.entries().get(i);
            String label = entry.ip();
            if (label.length() > 26) label = label.substring(0, 26) + "…";
            context.drawTextWithShadow(textRenderer, Text.literal((i == selected ? "> " : "  ") + label), listX, listY + i * 20 + 4, i == selected ? 0x55FFFF : 0xFFFFFF);
        }

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int center = width / 2;
        int left = center - 130;
        int listX = Math.max(10, left - 220);
        int listY = 55;

        if (mouseX >= listX && mouseX < listX + 200) {
            for (int i = 0; i < KeyPassConfig.entries().size(); i++) {
                int y = listY + i * 20;
                if (mouseY >= y && mouseY < y + 20) {
                    select(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
