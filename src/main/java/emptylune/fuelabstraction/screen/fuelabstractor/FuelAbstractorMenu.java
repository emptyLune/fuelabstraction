package emptylune.fuelabstraction.screen.fuelabstractor;

import emptylune.fuelabstraction.block.FuelAbstractionBlocks;
import emptylune.fuelabstraction.blockentity.FuelAbstractorBlockEntity;
import emptylune.fuelabstraction.screen.FuelAbstractionMenuTypes;
import emptylune.fuelabstraction.screen.FuelAbstractorResultSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class FuelAbstractorMenu extends AbstractContainerMenu {

    private static final int FIRE_SIZE = 14;
    private static final int ARROW_SIZE = 24;

    private final FuelAbstractorBlockEntity blockEntity;
    private final Level level;

    private final ContainerData data;

    public FuelAbstractorMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(pContainerId, inventory, inventory.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(FuelAbstractorBlockEntity.FuelAbstractorContainerData.SIZE));
    }

    public FuelAbstractorMenu(int pContainerId, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(FuelAbstractionMenuTypes.FUEL_ABSTRACTOR_MENU.get(), pContainerId);
        checkContainerSize(inventory, FuelAbstractorBlockEntity.CONTAINER_SIZE);
        blockEntity = ((FuelAbstractorBlockEntity) entity);
        this.level = inventory.player.level;
        this.data = data;

        addPlayerInventory(inventory);
        addPlayerHotBar(inventory);

        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 35, 34));
            this.addSlot(new FuelAbstractorResultSlot(handler, 1, 35, 53));
            this.addSlot(new FuelAbstractorResultSlot(handler, 2, 143, 34));
        });

        addDataSlots(data);
    }

    public boolean isAbstraction() {
        return data.get(FuelAbstractorBlockEntity.FuelAbstractorContainerData.INDEX_ABSTRACTION_TOTAL_AMOUNT) > 0;
    }

    public boolean isSolidification() {
        return data.get(FuelAbstractorBlockEntity.FuelAbstractorContainerData.INDEX_SOLIDIFICATION_TOTAL_AMOUNT) > 0;
    }

    public boolean isSolidificationEnabled() {
        return blockEntity.isSolidificationEnabled();
    }

    public void setSolidificationEnabled(boolean active) {
        blockEntity.setActiveSolidification(active);
    }

    public int getSolidificationItemIndex() {
        return blockEntity.getSolidificationItemIndex();
    }

    public void setSolidificationItemFromIndex(byte index) {
        blockEntity.setSolidificationItemFromIndex(index);
    }

    public int getScaledAbstractionProgress() {
        int remaining = data.get(FuelAbstractorBlockEntity.FuelAbstractorContainerData.INDEX_ABSTRACTION_REMAINING);
        int totalAmount = data.get(FuelAbstractorBlockEntity.FuelAbstractorContainerData.INDEX_ABSTRACTION_TOTAL_AMOUNT);

        return totalAmount == 0 ? 0 : (totalAmount - remaining) * FIRE_SIZE / totalAmount;
    }

    public int getScaledSolidificationProgress() {
        int remaining = data.get(FuelAbstractorBlockEntity.FuelAbstractorContainerData.INDEX_SOLIDIFICATION_REMAINING);
        int totalAmount = data.get(FuelAbstractorBlockEntity.FuelAbstractorContainerData.INDEX_SOLIDIFICATION_TOTAL_AMOUNT);

        return totalAmount == 0 ? 0 : (totalAmount - remaining) * ARROW_SIZE / totalAmount;
    }

    public int getAbstractFuelAmount() {
        return data.get(FuelAbstractorBlockEntity.FuelAbstractorContainerData.INDEX_ABSTRACT_FUEL_AMOUNT);
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private static final int TE_INVENTORY_PLAYER_PLACEABLE_SLOT_COUNT = 1;
    private static final int TE_INVENTORY_SLOT_COUNT = FuelAbstractorBlockEntity.CONTAINER_SIZE;

    @NotNull
    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < VANILLA_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_PLAYER_PLACEABLE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, FuelAbstractionBlocks.FUEL_ABSTRACTOR.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot((new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18)));
            }
        }
    }

    private void addPlayerHotBar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
