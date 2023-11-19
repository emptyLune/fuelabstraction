package emptylune.fuelabstraction.screen.fuelabstractor.solidification;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import emptylune.fuelabstraction.screen.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SolidificationItemRadioButton extends AbstractButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation("fuelabstraction:textures/gui/button_back.png");

    private static final int TEXTURE_WIDTH = 36;

    private static final int TEXTURE_HEIGHT = 36;

    private static final int WIDTH = 18;
    private static final int HEIGHT = 18;

    private final SolidificationItemRadioButtonArea group;

    private final int idInGroup;

    private final ItemRenderer itemRenderer;

    private boolean selected = false;
    private final ItemStack itemStack;

    public SolidificationItemRadioButton(SolidificationItemRadioButtonArea group, int idInGroup, ItemStack itemStack, int x, int y) {
        super(x, y, WIDTH, HEIGHT, TextComponent.EMPTY);
        this.group = group;
        this.idInGroup = idInGroup;
        this.itemStack = itemStack;
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    public void init(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    @Override
    public boolean mouseClicked(double x, double y, int key) {
        if (selected || !isInRange(x, y)) {
            return false;
        }
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        onPress();
        return true;
    }

    private boolean isInRange(double x, double y) {
        boolean inX = x >= this.x && x < (this.x + width);
        boolean inY = y >= this.y && y < (this.y + height);
        return inX && inY;
    }


    @Override
    public void onPress() {
        group.select(idInGroup);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int startX = selected ? width : 0;
        int startY = isHoveredOrFocused() ? height : 0;

        blit(poseStack, x, y, startX, startY, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.itemRenderer.renderAndDecorateFakeItem(itemStack, x + 1, y + 1);

        if (isHovered) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }
}
