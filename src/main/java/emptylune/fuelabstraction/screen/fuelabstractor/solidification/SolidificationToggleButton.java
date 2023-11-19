package emptylune.fuelabstraction.screen.fuelabstractor.solidification;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import emptylune.fuelabstraction.screen.Point;
import emptylune.fuelabstraction.screen.fuelabstractor.FuelAbstractorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public class SolidificationToggleButton extends AbstractButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation("fuelabstraction:textures/gui/action_toggle_button.png");

    private static final int TEXTURE_WIDTH = 28;
    private static final int TEXTURE_HEIGHT = 28;

    private static final int BUTTON_WIDTH = 14;
    private static final int BUTTON_HEIGHT = 14;

    private static final String ENABLED_TEXT = "tooltip.fuelabstraction.fuel_abstractor.solidification.enabled";

    private static final String DISABLED_TEXT = "tooltip.fuelabstraction.fuel_abstractor.solidification.disabled";

    private final FuelAbstractorScreen screen;

    private boolean pushed;

    public SolidificationToggleButton(FuelAbstractorScreen screen, Point point, boolean pushed) {
        this(screen, point.x(), point.y(), pushed);
    }

    public SolidificationToggleButton(FuelAbstractorScreen screen, int x, int y, boolean pushed) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, TextComponent.EMPTY);
        this.screen = screen;
        this.pushed = pushed;
    }

    public void init(Point point) {
        setPosition(point);
    }

    @Override
    public void onPress() {
        pushed = !pushed;
        screen.fetchSolidificationToggle(pushed);
    }

    @Override
    public boolean mouseClicked(double x, double y, int key) {
        if (!isInRange(x, y)) {
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
    public void updateNarration(NarrationElementOutput narration) {
        this.defaultButtonNarrationText(narration);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int startX = pushed ? width : 0;
        int startY = isHoveredOrFocused() ? height : 0;

        blit(poseStack, x, y, startX, startY, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        if (isHovered) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        Component state;
        if (pushed) {
            state = new TranslatableComponent(ENABLED_TEXT);
        } else {
            state = new TranslatableComponent(DISABLED_TEXT);
        }

        screen.renderComponentTooltip(poseStack, Arrays.asList(state), mouseX, mouseY);
    }

    public void setPosition(Point point) {
        setPosition(point.x(), point.y());
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
