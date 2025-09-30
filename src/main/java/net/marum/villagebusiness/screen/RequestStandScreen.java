package net.marum.villagebusiness.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.marum.villagebusiness.VillageBusiness;
import net.marum.villagebusiness.init.VillagerBusinessItems;
import net.marum.villagebusiness.pricing.ItemPrice;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RequestStandScreen extends AbstractContainerScreen<RequestStandScreenHandler> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(VillageBusiness.MOD_ID, "textures/gui/request_stand_gui.png");
    boolean isREILoaded = false;

    public RequestStandScreen(RequestStandScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        context.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Render ghost slot item
        ItemStack filterItem = menu.getFilterItem();
        if (!filterItem.isEmpty()) {
            context.renderItem(filterItem, x + 145, y + 52);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        context.drawString(font, Component.translatable("village_business.request"), x + 93, y + 56, 0x444444, false);

        if (menu.blockEntity.hasFilter()) {
            if (menu.blockEntity.getItemPrice() != null) {
                ItemPrice itemPrice = menu.blockEntity.getItemPrice();
                boolean enoughEmeralds = menu.blockEntity.hasEnoughEmeralds();
                context.drawCenteredString(font, Component.literal("x" + getConvertedPrice(menu.blockEntity.getRequestPrice())), x + 48 + 36, y + 25, enoughEmeralds ? 0xffffff : 0xff8888);
                boolean enoughSpace = menu.blockEntity.hasEnoughSpace();
                context.drawCenteredString(font, Component.literal("x" + itemPrice.getSellAmount(1)), x + 93 + 36, y + 25, enoughSpace ? 0xffffff : 0xff8888);
                context.drawCenteredString(font, getSaleChanceText(menu.blockEntity.getRequestChance()), x + 48, y + 43, 0xffffff);
                context.drawCenteredString(font, getSaleFrequencyText(menu.blockEntity.getRequestCooldown()), x + 48, y + 57, 0xffffff);
            } else {
                context.drawCenteredString(font, Component.literal("-"), x + 48 + 36, y + 25, 0xff8888);
                context.drawCenteredString(font, Component.literal("-"), x + 93 + 36, y + 25, 0xff8888);
                context.drawCenteredString(font, Component.literal("0%"), x + 48, y + 43, 0xff8888);
                context.drawCenteredString(font, Component.literal("-"), x + 48, y + 57, 0xff8888);
            }
        } else {
            context.drawCenteredString(font, Component.literal("-"), x + 48 + 36, y + 25, 0xcccccc);
            context.drawCenteredString(font, Component.literal("-"), x + 93 + 36, y + 25, 0xcccccc);
            context.drawCenteredString(font, Component.literal("-"), x + 48, y + 43, 0xcccccc);
            context.drawCenteredString(font, Component.literal("-"), x + 48, y + 57, 0xcccccc);
        }

        tooltip("village_business.seller_chance_tip", 15, 40, 50, 12, context, mouseX, mouseY, font);
        tooltip("village_business.seller_return_tip", 15, 54, 50, 12, context, mouseX, mouseY, font);

        tooltip("village_business.request_slot", 145, 51, 18, 18, context, mouseX, mouseY, font);

        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if ghost slot is clicked
        if (isMouseOverGhostSlot(mouseX, mouseY)) { // Adjust position/size
            ItemStack cursorStack = this.minecraft.player.containerMenu.getCarried();
            if (cursorStack.getItem() == Items.EMERALD || cursorStack.getItem() == Items.EMERALD_BLOCK || cursorStack.getItem() == VillagerBusinessItems.EMERALD_NUGGET)
                return false;

            if (!cursorStack.isEmpty()) {
                menu.blockEntity.sendRequestToServer(cursorStack.copy());
            } else {
                menu.blockEntity.sendRequestToServer(ItemStack.EMPTY);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void tooltip(String translationKey, int x, int y, int w, int h, GuiGraphics context, int mouseX, int mouseY, Font textRenderer) {
        if (isHovering(x, y, w, h, mouseX, mouseY))
            context.renderTooltip(textRenderer, Component.translatable(translationKey), mouseX, mouseY);
    }

    private String getConvertedPrice(int price) {
        if (price % 9 == 0) return "" + (price / 9);
        return "" + Math.round((100 * price) / 9.0f) / 100f;
    }

    private Component getSaleChanceText(int saleChance) {
        return Component.nullToEmpty(saleChance + "%");
    }

    private Component getSaleFrequencyText(float cooldown) {
        return getSaleFrequencyText(Math.round(cooldown));
    }

    private Component getSaleFrequencyText(int cooldown) {
        String timeString = cooldown + "s";
        if (cooldown >= 60 && cooldown % 60 == 0) {
            timeString = cooldown / 60 + "m";
        }
        return Component.nullToEmpty(timeString);
    }

    private boolean isMouseOverGhostSlot(double mouseX, double mouseY) {
        return isHovering(145, 51, 18, 18, (int) mouseX, (int) mouseY);
    }
}
