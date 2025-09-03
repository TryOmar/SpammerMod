package net.falcon.spammer.Screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandsScreen extends Screen {
    private final Screen parent;

    public CommandsScreen(Screen parent) {
        super(Text.literal("Spam Commands"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Close button on third row
        int closeY = this.height - 28;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"), 
                btn -> this.close()
        ).dimensions(this.width - 80 - 12, closeY, 80, 20).build());
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        
        // Commands as text
        int y = 30;
        int x = 20;
        int lineHeight = 12;
        
        context.drawTextWithShadow(this.textRenderer, "!helpSpam - Show help message", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!showSpam <ID> - Show config details", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!createSpam <ID> - Create new config", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!deleteSpam <ID> - Delete config", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!renameSpam <ID> <newID> - Rename config", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!runSpam <ID> - Run spam", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!stopSpam <ID> - Stop spam", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!stopSpam - Stop all spam", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!folderSpam - Open spam folder", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!scanName <name> - Scan chat", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "!scanClear - Clear scan results", x, y, 0xFFFFFF);
        y += lineHeight;
        context.drawTextWithShadow(this.textRenderer, "F6 - Open Config GUI", x, y, 0xFFFFFF);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
