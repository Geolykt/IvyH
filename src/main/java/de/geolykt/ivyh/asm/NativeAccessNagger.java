package de.geolykt.ivyh.asm;

import java.util.Vector;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.geolykt.starloader.starplane.annotations.ReferenceSource;
import de.geolykt.starloader.starplane.annotations.RemapClassReference;
import de.geolykt.starloader.starplane.annotations.RemapMemberReference;
import de.geolykt.starloader.starplane.annotations.RemapMemberReference.ReferenceFormat;
import de.geolykt.starloader.transformers.ASMTransformer;

import snoddasmannen.galimulator.Space;

public class NativeAccessNagger extends ASMTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeAccessNagger.class);

    @RemapMemberReference(ownerType = Space.class, name = "wars", descType = Vector.class, format = ReferenceFormat.NAME)
    private static final String WARS_FIELD_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "wars", descType = Vector.class, format = ReferenceFormat.DESCRIPTOR)
    private static final String WARS_FIELD_DESC = ReferenceSource.getStringValue();
    @RemapClassReference(type = Space.class)
    private static final String SPACE_CLASS_NAME = ReferenceSource.getStringValue();

    @Override
    public boolean accept(@NotNull ClassNode node) {
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    if (fieldInsn.owner.equals(SPACE_CLASS_NAME)
                            && fieldInsn.name.equals(WARS_FIELD_NAME)
                            && fieldInsn.desc.equals(WARS_FIELD_DESC)) {
                        LOGGER.warn("Referencing Space.wars from {}.{}{}", node.name, method.name, method.desc);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidTarget(@NotNull String internalName) {
        return true;
    }

    @Override
    public int getPriority() {
        return 2010;
    }
}
