package net.marum.villagebusiness.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.marum.villagebusiness.VillageBusiness;
import net.marum.villagebusiness.pricing.ItemPrice;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SalesStandScreen extends AbstractContainerScreen<SalesStandScreenHandler> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(VillageBusiness.MOD_ID, "textures/gui/sales_stand_gui.png");

    public SalesStandScreen(SalesStandScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    Button lowPriceButton;
    Button normalPriceButton;
    Button highPriceButton;

    @Override
    protected void init() {
        super.init();

        int x = (width-imageWidth)/2;
        int y = (height-imageHeight)/2;
        int BUTTON_WIDTH = 18;
        int BUTTON_HEIGHT = 18;

        lowPriceButton = Button.builder(
            Component.nullToEmpty("/2"),
            button -> onPriceSetLow()
        )
        .bounds(x+117-18, y+50, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build();
        this.addRenderableWidget(lowPriceButton);
        lowPriceButton.active = menu.blockEntity.getPriceSetting() != 0;

        normalPriceButton = Button.builder(
            Component.nullToEmpty("x1"),
            button -> onPriceSetNormal()
        )
        .bounds(x+117, y+50, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build();
        this.addRenderableWidget(normalPriceButton);
        normalPriceButton.active = menu.blockEntity.getPriceSetting() != 1;

        highPriceButton = Button.builder(
            Component.nullToEmpty("x2"),
            button -> onPriceSetHigh()
        )
        .bounds(x+117+18, y+50, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build();
        this.addRenderableWidget(highPriceButton);
        highPriceButton.active = menu.blockEntity.getPriceSetting() != 2;
    }

    private void onPriceSetLow() {
        lowPriceButton.active = false;
        normalPriceButton.active = true;
        highPriceButton.active = true;
        menu.blockEntity.sendPriceSettingToServer(0);
    }

    private void onPriceSetNormal() {
        lowPriceButton.active = true;
        normalPriceButton.active = false;
        highPriceButton.active = true;
        menu.blockEntity.sendPriceSettingToServer(1);
    }

    private void onPriceSetHigh() {
        lowPriceButton.active = true;
        normalPriceButton.active = true;
        highPriceButton.active = false;
        menu.blockEntity.sendPriceSettingToServer(2);
    }


    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width-imageWidth)/2;
        int y = (height-imageHeight)/2;

        context.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int x = (width-imageWidth)/2;
        int y = (height-imageHeight)/2;

        context.drawString(font, Component.translatable("village_business.price"), x+78, y+41, 0x444444, false);

        if (menu.blockEntity.hasProduct()) {
            if (menu.blockEntity.getItemPrice() != null) {
                int priceSetting = menu.blockEntity.getPriceSetting();
                ItemPrice itemPrice = menu.blockEntity.getItemPrice();
                boolean enoughProduct = menu.blockEntity.hasEnoughProduct();
                context.drawCenteredString(font, Component.literal("x"+itemPrice.getSellAmount(priceSetting)), x+48, y+25, enoughProduct ? 0xffffff : 0xff8888);
                boolean enoughSpace = menu.blockEntity.canInsertAmountIntoOutputSlot();
                context.drawCenteredString(font, Component.literal("x"+getConvertedPrice(itemPrice.getPrice(priceSetting))), x+93, y+25, enoughSpace ? 0xffffff : 0xff8888);
                context.drawCenteredString(font, getSaleChanceText(itemPrice.getSaleChance(priceSetting)), x+48, y+43, 0xffffff);
                context.drawCenteredString(font, getSaleFrequencyText(itemPrice.getCooldown(priceSetting)), x+48, y+57, 0xffffff);
            } else {
                context.drawCenteredString(font, Component.literal("-"), x+48, y+25, 0xff8888);
                context.drawCenteredString(font, Component.literal("-"), x+93, y+25, 0xff8888);
                context.drawCenteredString(font, Component.literal("0%"), x+48, y+43, 0xff8888);
                context.drawCenteredString(font, Component.literal("-"), x+48, y+57, 0xff8888);
            }
        } else {
            context.drawCenteredString(font, Component.literal("-"), x+48, y+25, 0xcccccc);
            context.drawCenteredString(font, Component.literal("-"), x+93, y+25, 0xcccccc);
            context.drawCenteredString(font, Component.literal("-"), x+48, y+43, 0xcccccc);
            context.drawCenteredString(font, Component.literal("-"), x+48, y+57, 0xcccccc);
        }

        tooltip("village_business.chance_tip", 15, 40, 50, 12, context, mouseX, mouseY, font);
        tooltip("village_business.return_tip", 15, 54, 50, 12, context, mouseX, mouseY, font);
    }

    private void tooltip(String translationKey, int x, int y, int w, int h, GuiGraphics context, int mouseX, int mouseY, Font textRenderer) {
        if (isHovering(x, y, w, h, mouseX, mouseY))
            context.renderTooltip(textRenderer, Component.translatable(translationKey), mouseX, mouseY);
    }

    private String getConvertedPrice(int price) {
        if (price%9 == 0) return ""+(price/9);
        return ""+Math.round((100*price)/9.0f)/100f;
    }

    private Component getSaleChanceText(int saleChance) {
        return Component.nullToEmpty(saleChance+"%");
    }

    private Component getSaleFrequencyText(int cooldown) {
        String timeString = cooldown+"s";
        if (cooldown >= 60 && cooldown%60 == 0) {
            timeString = cooldown/60+"m";
        }
        return Component.nullToEmpty(timeString);
    }
}
