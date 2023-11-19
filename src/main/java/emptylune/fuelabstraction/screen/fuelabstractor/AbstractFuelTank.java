package emptylune.fuelabstraction.screen.fuelabstractor;

import com.mojang.blaze3d.vertex.PoseStack;
import emptylune.fuelabstraction.blockentity.FuelAbstractorBlockEntity;
import emptylune.fuelabstraction.screen.Point;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.List;

public class AbstractFuelTank extends AbstractWidget {
    
    public static final int WIDTH = 24;
    public static final int HEIGHT = 52;

    private static final int ABSTRACT_FUEL_COLOR = 0xffbc717a;
    public static final int OVERLAY_COLOR = 0x4FFFFFFF;
    private static final int ABSTRACT_FUEL_TANK_SIZE = 52;

    private static final String MAX_SIZE_TEXT = "tooltip.fuelabstraction.fuel_abstractor.tank.max";

    private static final float SCALED_MAX_SIZE_AMOUNT = calcScaledBurnsAmount(FuelAbstractorBlockEntity.MAX_ABSTRACT_FUEL);

    private static final String EMPTY_TEXT = "tooltip.fuelabstraction.fuel_abstractor.tank.empty";

    private final FuelAbstractorScreen screen;
    private int abstractFuelAmount;

    private static float calcScaledBurnsAmount(int amount) {
        return (float) amount / 200F;
    }

    public AbstractFuelTank(FuelAbstractorScreen screen, Point point, int abstractFuelAmount) {
        this(screen, point.x(), point.y(), abstractFuelAmount);
    }

    public AbstractFuelTank(FuelAbstractorScreen screen, int x, int y, int abstractFuelAmount) {
        super(x, y, WIDTH, HEIGHT, TextComponent.EMPTY);
        this.screen = screen;
        this.abstractFuelAmount = abstractFuelAmount;
    }

    public void init(Point point) {
        this.init(point.x(), point.y());
    }

    public void init(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setAbstractFuelAmount(int abstractFuelAmount) {
        this.abstractFuelAmount = abstractFuelAmount;
    }

    @Override
    public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
        return false;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int startY = this.y + HEIGHT - calcScaledAmount();
        fill(poseStack, x, startY, x + WIDTH, this.y + HEIGHT, ABSTRACT_FUEL_COLOR);

        if (isHovered) {
            fill(poseStack, x, y, x + WIDTH, y + HEIGHT, OVERLAY_COLOR);
            renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    private int calcScaledAmount() {
        int scaled = abstractFuelAmount * ABSTRACT_FUEL_TANK_SIZE / FuelAbstractorBlockEntity.MAX_ABSTRACT_FUEL;
        return abstractFuelAmount == 0 ? 0 : Math.max(1, scaled);
    }

    @Override
    public void renderToolTip(PoseStack poseStack, int mouseX, int mouseY) {
        screen.renderComponentTooltip(poseStack, generateComponents(), mouseX, mouseY);
    }

    private List<Component> generateComponents() {
        Component amount;
        if (this.abstractFuelAmount == 0) {
            amount = new TranslatableComponent(EMPTY_TEXT);
        } else {
            amount = new TranslatableComponent("tooltip.fuelabstraction.fuel_abstractor.tank", calcScaledBurnsAmount(abstractFuelAmount));
        }
        return Arrays.asList(amount, new TranslatableComponent(MAX_SIZE_TEXT, SCALED_MAX_SIZE_AMOUNT));
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }
}
