package emptylune.fuelabstraction.screen.fuelabstractor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import emptylune.fuelabstraction.FuelAbstractionMain;
import emptylune.fuelabstraction.network.FuelAbstractionSolidificationItemChangePacket;
import emptylune.fuelabstraction.network.FuelAbstractorChannels;
import emptylune.fuelabstraction.network.FuelAbstractorSolidificationTogglePacket;
import emptylune.fuelabstraction.screen.Point;
import emptylune.fuelabstraction.screen.fuelabstractor.solidification.SolidificationItemRadioButtonArea;
import emptylune.fuelabstraction.screen.fuelabstractor.solidification.SolidificationToggleButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.slf4j.Logger;

public class FuelAbstractorScreen extends AbstractContainerScreen<FuelAbstractorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(FuelAbstractionMain.MOD_ID, "textures/gui/fuel_abstractor.png");

    private static final Point TOGGLE_OFFSET = new Point(144, 18);

    private static final Point RADIO_OFFSET = new Point(104, 52);
    private static final Point TANK_OFFSET = new Point(75, 18);

    private final SolidificationToggleButton solidificationToggleButton;
    private final SolidificationItemRadioButtonArea radioButtonArea;
    private final AbstractFuelTank tank;

    public FuelAbstractorScreen(FuelAbstractorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        solidificationToggleButton = new SolidificationToggleButton(this, calcToggleStartPoint(), pMenu.isSolidificationEnabled());
        radioButtonArea = new SolidificationItemRadioButtonArea(this, calcRadioButtonStartPoint(), pMenu.getSolidificationItemIndex());
        tank = new AbstractFuelTank(this, calcTankStartPoint(), pMenu.getAbstractFuelAmount());
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(solidificationToggleButton);
        solidificationToggleButton.init(calcToggleStartPoint());

        addRenderableWidget(radioButtonArea);
        radioButtonArea.init(calcRadioButtonStartPoint());

        addRenderableWidget(tank);
        tank.init(calcTankStartPoint());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        tank.setAbstractFuelAmount(menu.getAbstractFuelAmount());
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        Point startPoint = calcMenuStartPoint();
        int x = startPoint.x();
        int y = startPoint.y();

        this.blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.isAbstraction()) {
            int scaledProgress = menu.getScaledAbstractionProgress();
            blit(poseStack, x + 36, y + 18 + scaledProgress, 176, scaledProgress, 14, 14 - scaledProgress);
        }

        if (menu.isSolidification()) {
            blit(poseStack, x + 111, y + 34, 176, 14, menu.getScaledSolidificationProgress(), 16);
        }
    }

    private Point calcMenuStartPoint() {
        return new Point((width - imageWidth) / 2, (height - imageHeight) / 2);
    }

    private Point calcToggleStartPoint() {
        return calcMenuStartPoint().offset(TOGGLE_OFFSET);
    }

    private Point calcRadioButtonStartPoint() {
        return calcMenuStartPoint().offset(RADIO_OFFSET);
    }

    private Point calcTankStartPoint() {
        return calcMenuStartPoint().offset(TANK_OFFSET);
    }

    public void fetchSolidificationToggle(boolean enabled) {
        menu.setSolidificationEnabled(enabled);
        FuelAbstractorChannels.CHANNEL.sendToServer(new FuelAbstractorSolidificationTogglePacket(enabled));
    }

    public void fetchSolidificationItemChange(int index) {
        menu.setSolidificationItemFromIndex((byte) index);
        FuelAbstractorChannels.CHANNEL.sendToServer(new FuelAbstractionSolidificationItemChangePacket((byte) index));
    }


}
