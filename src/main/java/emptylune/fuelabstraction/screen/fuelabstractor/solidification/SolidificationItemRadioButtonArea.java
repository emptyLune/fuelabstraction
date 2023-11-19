package emptylune.fuelabstraction.screen.fuelabstractor.solidification;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import emptylune.fuelabstraction.block.FuelAbstractionBlocks;
import emptylune.fuelabstraction.item.FuelAbstractionItems;
import emptylune.fuelabstraction.screen.Point;
import emptylune.fuelabstraction.screen.fuelabstractor.FuelAbstractorScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SolidificationItemRadioButtonArea extends AbstractWidget {

    private static final Logger logger = LogUtils.getLogger();
    //    HACK: duplicate button size knowledge
    private static final int BUTTON_SIZE = 18;
    private static final int BUTTON_SPACE = 1;

    private final FuelAbstractorScreen screen;
    private int currentSelectedIndex;

    private final List<SolidificationItemRadioButton> buttons = new ArrayList<>();

    public SolidificationItemRadioButtonArea(FuelAbstractorScreen screen, Point point, int initialIndex) {
        this(screen, point.x(), point.y(), initialIndex);
    }

    public SolidificationItemRadioButtonArea(FuelAbstractorScreen screen, int x, int y, int initialIndex) {
        super(x, y, 0, 0, TextComponent.EMPTY);
        this.screen = screen;
        currentSelectedIndex = initialIndex;

        addRadioButton(new ItemStack(FuelAbstractionItems.TINY_ABSTRACT_FUEL_INSTANCE.get()));
        addRadioButton(new ItemStack(FuelAbstractionItems.ABSTRACT_FUEL_INSTANCE.get()));
        addRadioButton(new ItemStack(FuelAbstractionBlocks.BLOCK_ABSTRACT_FUEL_INSTANCE.get().asItem()));
        int safeIndex = initialIndex >= buttons.size() || initialIndex < 0 ? 1 : initialIndex;
        buttons.get(safeIndex).setSelected(true);
    }

    public void init(Point point) {
        this.init(point.x(), point.y());
    }

    public void init(int x, int y) {
        this.x = x;
        this.y = y;

        for (int index = 0, size = buttons.size(); index < size; index++) {
            int buttonX = x + (BUTTON_SIZE * index + BUTTON_SPACE * index);
            buttons.get(index).init(buttonX, y);
        }
    }

    private void addRadioButton(ItemStack itemStack) {
        int buttonCount = buttons.size();
        int buttonX = BUTTON_SIZE * buttonCount + BUTTON_SPACE * buttonCount;
        SolidificationItemRadioButton radioButton = new SolidificationItemRadioButton(this, buttonCount, itemStack, buttonX, y);
        buttons.add(radioButton);
    }

    public void select(int index) {
        if (index >= buttons.size()) {
            logger.warn("Index out of bounds. : {}", index);
            return;
        }
        for (int i = 0, size = buttons.size(); i < size; i++) {
            SolidificationItemRadioButton button = buttons.get(i);
            button.setSelected(i == index);
        }
        currentSelectedIndex = index;
        screen.fetchSolidificationItemChange(index);
    }

    @Override
    public boolean mouseClicked(double x, double y, int key) {
        for (SolidificationItemRadioButton button : buttons) {
            if (button.mouseClicked(x, y, key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void renderButton(PoseStack poseStack, int x, int y, float delta) {
        buttons.forEach(button -> button.render(poseStack, x, y, delta));
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }
}
