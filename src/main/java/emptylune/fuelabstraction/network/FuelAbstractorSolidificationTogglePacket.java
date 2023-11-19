package emptylune.fuelabstraction.network;

import emptylune.fuelabstraction.screen.fuelabstractor.FuelAbstractorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FuelAbstractorSolidificationTogglePacket implements FuelAbstractionPacket {

    private final boolean solidificationEnabled;

    public static FuelAbstractorSolidificationTogglePacket decoder(FriendlyByteBuf buffer) {
        boolean solidificationEnabled = buffer.readBoolean();
        return new FuelAbstractorSolidificationTogglePacket(solidificationEnabled);
    }

    public FuelAbstractorSolidificationTogglePacket(boolean solidificationEnabled) {
        this.solidificationEnabled = solidificationEnabled;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(solidificationEnabled);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof FuelAbstractorMenu menu) {
                menu.setSolidificationEnabled(this.solidificationEnabled);
            }
        });
        context.get().setPacketHandled(true);
    }


}
