package de.geolykt.ivyh;

import org.jetbrains.annotations.NotNull;

public final class IvyUtil {
    public static final void appendColorString(@NotNull StringBuilder out, @NotNull String string, int abgr) {
        int rgb = (abgr & 0xFF) << 16 | (abgr & 0xFF00) | (abgr & 0xFF0000) >> 16;
        out.append("[#");
        String hex = Integer.toHexString(rgb);
        for (int i = hex.length(); i < 6; i++) {
            out.appendCodePoint('0');
        }
        out.append(hex).appendCodePoint(']').append(string).append("[]");
    }
}
