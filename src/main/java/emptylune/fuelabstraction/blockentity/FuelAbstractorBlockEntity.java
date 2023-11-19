package emptylune.fuelabstraction.blockentity;

import com.mojang.logging.LogUtils;
import emptylune.fuelabstraction.block.FuelAbstractionBlocks;
import emptylune.fuelabstraction.item.FuelAbstractionItems;
import emptylune.fuelabstraction.screen.fuelabstractor.FuelAbstractorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FuelAbstractorBlockEntity extends BlockEntity implements MenuProvider {

    private static final Logger logger = LogUtils.getLogger();

    public static final int MAX_ABSTRACT_FUEL = 1000000;
    public static final int CONTAINER_SIZE = 3;
    private static final int BASE_ABSTRACTION_RATE = 20;
    private static final int BASE_SOLIDIFICATION_RATE = 80;

    protected final ContainerData data;
    private int abstractionRemaining;
    private int abstractionTotalAmount;
    private int abstractFuelAmount;
    private int solidificationRemaining;
    private int solidificationTotalAmount;
    private boolean solidificationEnabled;
    private SolidificationEntry currentSolidificationEntry;

    private final SingleSlotItemStackHandler fuelInventory;
    private final SingleSlotItemStackHandler solidInventory;
    private final SingleSlotItemStackHandler remainingInventory;
    private final CombinedInvWrapper combinedInvntory;

    private final LazyOptional<IItemHandler> fuelItemHandler;
    private final LazyOptional<IItemHandler> outputItemHandler;
    private final LazyOptional<IItemHandler> combinedItemHandler;

    public FuelAbstractorBlockEntity(BlockPos pos, BlockState state) {
        super(FuelAbstractionBlockEntities.FUEL_ABSTRACTOR.get(), pos, state);
        this.abstractionRemaining = 0;
        this.abstractionTotalAmount = 0;
        this.abstractFuelAmount = 0;
        this.solidificationRemaining = 0;
        this.solidificationTotalAmount = 0;

        solidificationEnabled = true;
        currentSolidificationEntry = SolidificationEntry.BASIC;

        data = new FuelAbstractorContainerData();

        fuelInventory = createItemStackHandler();
        remainingInventory = createItemStackHandler();
        solidInventory = createItemStackHandler();
        combinedInvntory = new CombinedInvWrapper(fuelInventory, remainingInventory, solidInventory);

        fuelItemHandler = LazyOptional.of(() -> fuelInventory);
        outputItemHandler = LazyOptional.of(() -> new CombinedInvWrapper(solidInventory, remainingInventory));
        combinedItemHandler = LazyOptional.of(() -> combinedInvntory);
    }

    private SingleSlotItemStackHandler createItemStackHandler() {
        return new SingleSlotItemStackHandler();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if (level.isClientSide) {
            return;
        }

        FuelAbstractorBlockEntity tile = (FuelAbstractorBlockEntity) be;

        boolean needsSetChanged = false;
        if (tile.isAbstraction()) {
            tile.proceedAbstraction();
            needsSetChanged = true;
        } else if (tile.canAbstraction()) {
            tile.startAbstraction();
            needsSetChanged = true;
        }

        if (tile.isSolidification()) {
            tile.proceedSolidification();
            needsSetChanged = true;
        } else if (tile.canSolidification()) {
            tile.startSolidification();
            needsSetChanged = true;
        }
        if (needsSetChanged) {
            setChanged(level, pos, state);
        }
    }

    private void startAbstraction() {
        ItemStack fuelSlotItemStack = fuelInventory.getStack();
        abstractionTotalAmount = ForgeHooks.getBurnTime(fuelSlotItemStack, RecipeType.SMELTING);
        abstractionRemaining = abstractionTotalAmount;

        ItemStack fuelRemainingItemStack = fuelSlotItemStack.getContainerItem();
        ItemStack remainingSlotItemStack = remainingInventory.getStack();
        if (remainingSlotItemStack.isEmpty()) {
            remainingInventory.setStack(fuelRemainingItemStack);
        } else {
            remainingSlotItemStack.grow(1);
        }
        fuelSlotItemStack.shrink(1);
    }

    private void proceedAbstraction() {
        if (abstractionRemaining >= BASE_ABSTRACTION_RATE) {
            abstractionRemaining -= BASE_ABSTRACTION_RATE;
            abstractFuelAmount += BASE_ABSTRACTION_RATE;
        } else {
            abstractFuelAmount += abstractionRemaining;
            abstractionRemaining = 0;
            abstractionTotalAmount = 0;
        }
    }

    private boolean canAbstraction() {
        ItemStack fuelSlotItemStack = fuelInventory.getStack();
        int burnTime = ForgeHooks.getBurnTime(fuelSlotItemStack, RecipeType.SMELTING);

        if (burnTime == 0) {
            return false;
        }

        int freeCapacity = MAX_ABSTRACT_FUEL - abstractFuelAmount;
        if (burnTime > freeCapacity) {
            return false;
        }

        if (!fuelSlotItemStack.hasContainerItem()) {
            return true;
        }

        ItemStack abstractionRemainingResult = remainingInventory.getStack();

        if (abstractionRemainingResult.isEmpty()) {
            return true;
        }

        ItemStack remainingItemStack = fuelSlotItemStack.getContainerItem();
        if (!abstractionRemainingResult.is(remainingItemStack.getItem())) {
            return false;
        }

        if (abstractionRemainingResult.getCount() == abstractionRemainingResult.getMaxStackSize()) {
            return false;
        }

        return true;
    }

    private boolean isAbstraction() {
        return abstractionTotalAmount > 0;
    }

    private void startSolidification() {
        solidificationTotalAmount = currentSolidificationEntry.getUseAmount();
        solidificationRemaining = solidificationTotalAmount;
    }

    private void proceedSolidification() {
        solidificationRemaining -= BASE_SOLIDIFICATION_RATE;
        if (solidificationRemaining > 0) {
            return;
        }
        solidificationRemaining = 0;
        solidificationTotalAmount = 0;

        abstractFuelAmount -= currentSolidificationEntry.getUseAmount();

        ItemStack resultSlotItemStack = solidInventory.getStack();
        if (resultSlotItemStack.isEmpty()) {
            ItemStack result = currentSolidificationEntry.getItemStack().copy();
            solidInventory.setStack(result);
        } else {
            resultSlotItemStack.grow(1);
        }
    }

    private boolean isSolidification() {
        return solidificationTotalAmount > 0;
    }

    private boolean canSolidification() {
        return solidificationEnabled && hasRequiredQuantityAbstractFuel() && canInsertItemOutputSlot();
    }

    private boolean hasRequiredQuantityAbstractFuel() {
        return abstractFuelAmount >= currentSolidificationEntry.getUseAmount();
    }

    private boolean canInsertItemOutputSlot() {
        ItemStack resultSlotItemStack = solidInventory.getStack();
        return resultSlotItemStack.isEmpty()
                || resultSlotItemStack.is(currentSolidificationEntry.getItemStack().getItem()) && resultSlotItemStack.getCount() < resultSlotItemStack.getMaxStackSize();
    }

    public boolean isSolidificationEnabled() {
        return solidificationEnabled;
    }

    public void setActiveSolidification(boolean active) {
        solidificationEnabled = active;
        if (!active) {
            solidificationRemaining = 0;
            solidificationTotalAmount = 0;
        }
        setChanged();
        sendBlockUpdatedToClient();
    }

    public byte getSolidificationItemIndex() {
        return switch (currentSolidificationEntry) {
            case TINY -> (byte) SolidificationEntry.SOLIDIFICATION_ITEM_TINY;
            case BASIC -> (byte) SolidificationEntry.SOLIDIFICATION_ITEM_BASIC;
            case BLOCK -> (byte) SolidificationEntry.SOLIDIFICATION_ITEM_BLOCK;
        };
    }

    public void setSolidificationItemFromIndex(byte index) {
        currentSolidificationEntry = SolidificationEntry.getSolidificationEntryFromIndex(index);

        solidificationRemaining = 0;
        solidificationTotalAmount = 0;
        setChanged();
        sendBlockUpdatedToClient();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return super.getCapability(cap, side);
        }

        if (side == null) {
            return combinedItemHandler.cast();
        } else if (side == Direction.DOWN) {
            return outputItemHandler.cast();
        } else {
            return fuelItemHandler.cast();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fuelItemHandler.invalidate();
        outputItemHandler.invalidate();
        combinedItemHandler.invalidate();
    }


    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("fuel_abstractor.solidification.enabled", solidificationEnabled);
        tag.putByte("fuel_abstractor.solidification.item", getSolidificationItemIndex());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        solidificationEnabled = tag.getBoolean("fuel_abstractor.solidification.enabled");
        currentSolidificationEntry = SolidificationEntry.getSolidificationEntryFromIndex(tag.getByte("fuel_abstractor.solidification.item"));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sendBlockUpdatedToClient() {
        if (!level.isClientSide) {
            return;
        }
        BlockState blockState = this.getBlockState();
        level.sendBlockUpdated(this.getBlockPos(), blockState, blockState, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory.fuel", fuelInventory.serializeNBT());
        tag.put("inventory.remaining", remainingInventory.serializeNBT());
        tag.put("inventory.solid", solidInventory.serializeNBT());
        tag.putInt("fuel_abstractor.abstraction.remaining", abstractionRemaining);
        tag.putInt("fuel_abstractor.abstraction.total_amount", abstractionTotalAmount);
        tag.putInt("fuel_abstractor.solidification.remaining", solidificationRemaining);
        tag.putInt("fuel_abstractor.solidification.total_amount", solidificationTotalAmount);
        tag.putBoolean("fuel_abstractor.solidification.enabled", solidificationEnabled);
        tag.putByte("fuel_abstractor.solidification.item", getSolidificationItemIndex());
        tag.putInt("fuel_abstractor.solidification.amount", abstractFuelAmount);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fuelInventory.deserializeNBT(tag.getCompound("inventory.fuel"));
        remainingInventory.deserializeNBT(tag.getCompound("inventory.remaining"));
        solidInventory.deserializeNBT(tag.getCompound("inventory.solid"));
        abstractionRemaining = tag.getInt("fuel_abstractor.abstraction.remaining");
        abstractionTotalAmount = tag.getInt("fuel_abstractor.abstraction.total_amount");
        solidificationRemaining = tag.getInt("fuel_abstractor.solidification.remaining");
        solidificationTotalAmount = tag.getInt("fuel_abstractor.solidification.total_amount");
        solidificationEnabled = tag.getBoolean("fuel_abstractor.solidification.enabled");
        currentSolidificationEntry = SolidificationEntry.getSolidificationEntryFromIndex(tag.getByte("fuel_abstractor.solidification.item"));
        abstractFuelAmount = tag.getInt("fuel_abstractor.solidification.amount");
    }

    public void drops() {
        int slotSize = combinedInvntory.getSlots();
        SimpleContainer inventory = new SimpleContainer(slotSize);
        for (int i = 0; i < slotSize; i++) {
            inventory.setItem(i, combinedInvntory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }


    @NotNull
    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("menu.fuelabstraction.fuel_abstractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new FuelAbstractorMenu(pContainerId, pInventory, this, this.data);
    }

    public class FuelAbstractorContainerData implements ContainerData {
        public static final int SIZE = 5;
        public static final int INDEX_ABSTRACTION_REMAINING = 0;
        public static final int INDEX_ABSTRACTION_TOTAL_AMOUNT = 1;
        public static final int INDEX_SOLIDIFICATION_REMAINING = 2;
        public static final int INDEX_SOLIDIFICATION_TOTAL_AMOUNT = 3;
        public static final int INDEX_ABSTRACT_FUEL_AMOUNT = 4;

        @Override
        public int get(int index) {
            return switch (index) {
                case INDEX_ABSTRACTION_REMAINING ->
                        FuelAbstractorBlockEntity.this.abstractionRemaining;
                case INDEX_ABSTRACTION_TOTAL_AMOUNT ->
                        FuelAbstractorBlockEntity.this.abstractionTotalAmount;
                case INDEX_SOLIDIFICATION_REMAINING ->
                        FuelAbstractorBlockEntity.this.solidificationRemaining;
                case INDEX_SOLIDIFICATION_TOTAL_AMOUNT ->
                        FuelAbstractorBlockEntity.this.solidificationTotalAmount;
                case INDEX_ABSTRACT_FUEL_AMOUNT ->
                        FuelAbstractorBlockEntity.this.abstractFuelAmount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            logger.warn("UnSupported operation : [{}: {}]", index, value);
        }

        @Override
        public int getCount() {
            return SIZE;
        }
    }

    private class SingleSlotItemStackHandler extends ItemStackHandler {
        public SingleSlotItemStackHandler() {
            super();
        }

        public void setStack(@NotNull ItemStack itemStack) {
            setStackInSlot(0, itemStack);
        }

        @NotNull
        public ItemStack getStack() {
            return getStackInSlot(0);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    }

    public enum SolidificationEntry {
        TINY(FuelAbstractionItems.TINY_ABSTRACT_FUEL_INSTANCE.get(), 200),
        BASIC(FuelAbstractionItems.ABSTRACT_FUEL_INSTANCE.get(), 1600),
        BLOCK(FuelAbstractionBlocks.BLOCK_ABSTRACT_FUEL_INSTANCE.get().asItem(), 14400);

        public static final int SOLIDIFICATION_ITEM_TINY = 0;
        public static final int SOLIDIFICATION_ITEM_BASIC = 1;
        public static final int SOLIDIFICATION_ITEM_BLOCK = 2;
        private final ItemStack itemStack;
        private final int useAmount;

        SolidificationEntry(Item item, int useAmount) {
            this.itemStack = new ItemStack(item);
            this.useAmount = useAmount;
        }

        @NotNull
        private static SolidificationEntry getSolidificationEntryFromIndex(byte index) {
            return switch (index) {
                case SOLIDIFICATION_ITEM_TINY -> TINY;
                case SOLIDIFICATION_ITEM_BASIC -> BASIC;
                case SOLIDIFICATION_ITEM_BLOCK -> BLOCK;
                default -> {
                    logger.warn("Unexpected value: {}", index);
                    yield BASIC;
                }
            };
        }

        @NotNull
        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getUseAmount() {
            return useAmount;
        }

    }
}
