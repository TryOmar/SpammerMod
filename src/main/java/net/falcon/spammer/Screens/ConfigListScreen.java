package net.falcon.spammer.Screens;

import net.falcon.spammer.Managers.SpamManager;
import net.falcon.spammer.Models.SpamConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Util;

import java.io.File;
import java.util.List;

public class ConfigListScreen extends Screen {
    private final Screen parent;
    private ConfigListWidget listWidget;
    private ButtonWidget runButton;
    private ButtonWidget stopButton;
    private ButtonWidget editButton;
    private ButtonWidget deleteButton;
    private ButtonWidget newButton;
    private ButtonWidget openFolderButton;
    private ButtonWidget refreshButton;
    private ButtonWidget commandsButton;
    private ButtonWidget closeButton;
    private String pendingDeleteId;

    public ConfigListScreen(Screen parent) {
        super(Text.literal("Spam Configurations"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int listTop = 32;
        int listBottom = this.height - 64;
        this.listWidget = new ConfigListWidget(this.client, this.width, this.height, listTop, listBottom, 20);
        this.listWidget.setOnSelectionChanged(() -> updateButtonsState());
        this.addSelectableChild(this.listWidget);

        refreshList();

        int row1Y = this.height - 52; // first row
        int row2Y = this.height - 28; // second row
        int x = 12;
        int w = 80;
        int gap = 6;

        // Row 1: Run / Stop / Edit / Delete
        this.runButton = ButtonWidget.builder(Text.literal("Run"), btn -> onRun()).dimensions(x, row1Y, w, 20).build();
        x += w + gap;
        this.stopButton = ButtonWidget.builder(Text.literal("Stop"), btn -> onStop()).dimensions(x, row1Y, w, 20).build();
        x += w + gap;
        this.editButton = ButtonWidget.builder(Text.literal("Edit"), btn -> onEdit()).dimensions(x, row1Y, w, 20).build();
        x += w + gap;
        this.deleteButton = ButtonWidget.builder(Text.literal("Delete"), btn -> onDelete()).dimensions(x, row1Y, w, 20).build();

        // Row 2: New Config / Open Folder / Refresh / Commands
        int x2 = 12;
        this.newButton = ButtonWidget.builder(Text.literal("New Config"), btn -> onNew()).dimensions(x2, row2Y, 100, 20).build();
        x2 += 100 + gap;
        this.openFolderButton = ButtonWidget.builder(Text.literal("Open Folder"), btn -> onOpenFolder()).dimensions(x2, row2Y, 100, 20).build();
        x2 += 100 + gap;
        this.refreshButton = ButtonWidget.builder(Text.literal("Refresh"), btn -> onRefresh()).dimensions(x2, row2Y, 80, 20).build();
        x2 += 80 + gap;
        this.commandsButton = ButtonWidget.builder(Text.literal("Commands"), btn -> onCommands()).dimensions(x2, row2Y, 80, 20).build();

        // Row 3: Close button (aligned to right)
        int row3Y = this.height - 28;
        this.closeButton = ButtonWidget.builder(Text.literal("Close"), btn -> this.close()).dimensions(this.width - 80 - 12, row3Y, 80, 20).build();

        this.addDrawableChild(this.runButton);
        this.addDrawableChild(this.stopButton);
        this.addDrawableChild(this.editButton);
        this.addDrawableChild(this.deleteButton);
        this.addDrawableChild(this.newButton);
        this.addDrawableChild(this.openFolderButton);
        this.addDrawableChild(this.refreshButton);
        this.addDrawableChild(this.commandsButton);
        this.addDrawableChild(this.closeButton);

        updateButtonsState();
    }

    private void refreshList() {
        this.listWidget.reset();
        List<SpamConfig> configs = SpamConfig.getAllIds();
        for (SpamConfig cfg : configs) {
            this.listWidget.addConfigEntry(new ConfigEntry(this.listWidget, cfg));
        }
        this.listWidget.setSelected(null);
        updateButtonsState();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.listWidget.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    private ConfigEntry getSelected() {
        return this.listWidget.getSelectedOrNull();
    }

    private void onRun() {
        ConfigEntry sel = getSelected();
        if (sel != null) SpamManager.run(sel.config.id);
        updateButtonsState();
    }

    private void onStop() {
        ConfigEntry sel = getSelected();
        if (sel != null) SpamManager.stop(sel.config.id);
        updateButtonsState();
    }

    private void onEdit() {
        ConfigEntry sel = getSelected();
        if (sel != null) {
            File file = SpamConfig.getConfigFile(sel.config.id);
            Util.getOperatingSystem().open(file);
        }
    }

    private void onDelete() {
        ConfigEntry sel = getSelected();
        if (sel != null) {
            this.pendingDeleteId = sel.config.id;
            Text title = Text.literal("Delete configuration?");
            Text message = Text.literal("Are you sure you want to delete '" + this.pendingDeleteId + "'?");
            this.client.setScreen(new ConfirmScreen(this::confirmDelete, title, message, ScreenTexts.YES, ScreenTexts.NO));
        }
    }

    private void confirmDelete(boolean confirmed) {
        if (confirmed && this.pendingDeleteId != null) {
            SpamManager.delete(this.pendingDeleteId);
            this.pendingDeleteId = null;
            refreshList();
        }
        if (this.client != null) this.client.setScreen(this);
    }

    private void onNew() {
        String newId = generateNewId();
        SpamManager.create(newId);
        File file = SpamConfig.getConfigFile(newId);
        Util.getOperatingSystem().open(file);
        refreshList(); // Refresh the list after creating new config
    }

    private String generateNewId() {
        int idx = 1;
        while (true) {
            String candidate = "config-" + idx;
            if (!SpamConfig.exists(candidate)) return candidate;
            idx++;
        }
    }

    private void onOpenFolder() {
        File userDir = new File("Spam", SpamConfig.getUsername());
        if (!userDir.exists()) userDir.mkdirs();
        String path = userDir.getAbsolutePath();
        Util.getOperatingSystem().open(userDir);
    }

    private void onRefresh() {
        refreshList();
    }

    private void onCommands() {
        this.client.setScreen(new CommandsScreen(this));
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }

    private void updateButtonsState() {
        boolean hasSelection = getSelected() != null;
        if (this.runButton != null) this.runButton.active = hasSelection;
        if (this.stopButton != null) this.stopButton.active = hasSelection;
        if (this.editButton != null) this.editButton.active = hasSelection;
        if (this.deleteButton != null) this.deleteButton.active = hasSelection;
    }

    static class ConfigListWidget extends AlwaysSelectedEntryListWidget<ConfigEntry> {
        private Runnable onSelectionChanged;
        public ConfigListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
            super(client, width, height, top, bottom, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return this.width - 24;
        }

        public void reset() {
            this.clearEntries();
        }

        public void addConfigEntry(ConfigEntry entry) {
            this.addEntry(entry);
        }

        public void setOnSelectionChanged(Runnable callback) {
            this.onSelectionChanged = callback;
        }

        @Override
        public void setSelected(ConfigEntry entry) {
            super.setSelected(entry);
            if (this.onSelectionChanged != null) this.onSelectionChanged.run();
        }
    }

    static class ConfigEntry extends AlwaysSelectedEntryListWidget.Entry<ConfigEntry> {
        final ConfigListWidget parent;
        final SpamConfig config;

        public ConfigEntry(ConfigListWidget parent, SpamConfig config) {
            this.parent = parent;
            this.config = config;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String state = SpamManager.spamStatus.getOrDefault(config.id, false) ? "Running" : "Stopped";
            int color = SpamManager.spamStatus.getOrDefault(config.id, false) ? 0x55FF55 : 0xFF5555;
            context.drawText(MinecraftClient.getInstance().textRenderer, config.id, x + 4, y + 6, 0xFFFFFF, false);
            context.drawText(MinecraftClient.getInstance().textRenderer, state, x + entryWidth - 80, y + 6, color, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.parent.setSelected(this);
            return true;
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.config.id);
        }
    }
}


