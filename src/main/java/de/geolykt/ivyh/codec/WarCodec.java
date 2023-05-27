package de.geolykt.ivyh.codec;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import de.geolykt.ivyh.codec.IvyWarContainerState.WarState;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.serial.Codec;

public class WarCodec extends Codec<@NotNull IvyWarContainerState> {

    public WarCodec(@NotNull NamespacedKey encoderKey) {
        super(encoderKey);
    }

    @Override
    public boolean canEncode(@NotNull Object object) {
        return object instanceof IvyWarContainerState;
    }

    @Override
    public byte @NotNull [] encode(@NotNull IvyWarContainerState input) {
        int size = 24 * input.wars.size() + 2;
        for (WarState war : input.wars) {
            size += war.aggressors.length * 4;
            size += war.defenders.length * 4;
        }
        byte[] data = new byte[size];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.putShort((short) input.wars.size());
        for (WarState war : input.wars) {
            buffer.putInt(war.warStart);
            buffer.putInt(war.lastActionYear);
            buffer.putInt(war.aggressorScore);
            buffer.putInt(war.initiator);
            buffer.putInt(war.target);
            buffer.putShort((short) war.aggressors.length);
            for (int agg : war.aggressors) {
                buffer.putInt(agg);
            }
            buffer.putShort((short) war.defenders.length);
            for (int def : war.defenders) {
                buffer.putInt(def);
            }
        }
        return data;
    }

    private final int @NotNull[] decodeIntArray(@NotNull DataInputStream dis) throws IOException {
        short len = dis.readShort();
        int[] arr = new int[len];
        for (short i = 0; i < len; i++) {
            arr[i] = dis.readInt();
        }
        return arr;
    }

    private final int @NotNull[] decodeIntArray(ByteBuffer buffer) {
        short len = buffer.getShort();
        int[] arr = new int[len];
        for (short i = 0; i < len; i++) {
            arr[i] = buffer.getInt();
        }
        return arr;
    }

    @Override
    @NotNull
    public IvyWarContainerState decode(byte @NotNull [] input) {
        ByteBuffer buffer = ByteBuffer.wrap(input);
        List<@NotNull WarState> wars = new ArrayList<>();
        short count = buffer.getShort();
        for (int i = 0; i < count; i++) {
            wars.add(new WarState(buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt(),
                    decodeIntArray(buffer), decodeIntArray(buffer)));
        }
        return new IvyWarContainerState(wars);
    }

    @Override
    @NotNull
    public IvyWarContainerState decode(@NotNull DataInputStream input) throws IOException {
        List<@NotNull WarState> wars = new ArrayList<>();
        short count = input.readShort();
        for (int i = 0; i < count; i++) {
            wars.add(new WarState(input.readInt(), input.readInt(), input.readInt(), input.readInt(), input.readInt(),
                    decodeIntArray(input), decodeIntArray(input)));
        }
        return new IvyWarContainerState(wars);
    }
}
