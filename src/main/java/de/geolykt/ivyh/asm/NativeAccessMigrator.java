package de.geolykt.ivyh.asm;

import java.util.List;
import java.util.Vector;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import de.geolykt.starloader.starplane.annotations.MethodDesc;
import de.geolykt.starloader.starplane.annotations.ReferenceSource;
import de.geolykt.starloader.starplane.annotations.RemapClassReference;
import de.geolykt.starloader.starplane.annotations.RemapMemberReference;
import de.geolykt.starloader.starplane.annotations.RemapMemberReference.ReferenceFormat;
import de.geolykt.starloader.transformers.ASMTransformer;

import snoddasmannen.galimulator.Empire;
import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.Space;

public class NativeAccessMigrator  extends ASMTransformer {

    @RemapMemberReference(ownerType = Space.class, name = "wars", descType = Vector.class, format = ReferenceFormat.NAME)
    private static final String WARS_FIELD_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "wars", descType = Vector.class, format = ReferenceFormat.DESCRIPTOR)
    private static final String WARS_FIELD_DESC = ReferenceSource.getStringValue();
    @RemapClassReference(type = Space.class)
    private static final String SPACE_CLASS_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "generateGalaxy", methodDesc =  @MethodDesc(args = {int.class, MapData.class}, ret = void.class), format = ReferenceFormat.NAME)
    private static final String GEN_GALAXY_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "generateGalaxy", methodDesc =  @MethodDesc(args = {int.class, MapData.class}, ret = void.class), format = ReferenceFormat.DESCRIPTOR)
    private static final String GEN_GALAXY_DESC = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "initialize", desc = "(F)V", format = ReferenceFormat.NAME)
    private static final String SPACE_INITIALIZE_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "loadState", methodDesc =  @MethodDesc(args = {String.class}, ret = boolean.class), format = ReferenceFormat.NAME)
    private static final String LOAD_STATE_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "saveSync", methodDesc =  @MethodDesc(args = {String.class, String.class}, ret = void.class), format = ReferenceFormat.NAME)
    private static final String SAVE_SYNC_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "tick", desc = "()I", format = ReferenceFormat.NAME)
    private static final String TICK_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Empire.class, name = "canBeVassalizedBy", methodDesc = @MethodDesc(args = Empire.class, ret = boolean.class), format = ReferenceFormat.NAME)
    private static final String CAN_BE_ASSALIZED_BY_NAME = ReferenceSource.getStringValue();
    @RemapMemberReference(ownerType = Space.class, name = "getParticipatingWars", methodDesc = @MethodDesc(args = Empire.class, ret = List.class), format = ReferenceFormat.NAME)
    private static final String GET_PARTICIPATING_WARS_NAME = ReferenceSource.getStringValue();

    @Override
    public boolean accept(@NotNull ClassNode node) {
        for (MethodNode method : node.methods) {
            boolean warAssignNull = false;
            boolean warRetrieveNull = false;
            boolean tickMethod = false;
            boolean vassalizedMethod = false;
            if (node.name.equals(SPACE_CLASS_NAME)
                    && ((method.name.equals(GEN_GALAXY_NAME) && method.desc.equals(GEN_GALAXY_DESC))
                            || (method.name.equals(SPACE_INITIALIZE_NAME) && method.desc.equals("(F)V"))
                            || (method.name.equals(LOAD_STATE_NAME) && method.desc.equals("(Ljava/lang/String;)Z")))) {
                warAssignNull = true;
            } else if (node.name.equals(SPACE_CLASS_NAME)
                    && ((method.name.equals(SAVE_SYNC_NAME) && method.desc.equals("(Ljava/lang/String;Ljava/lang/String;)V")))) {
                warRetrieveNull = true;
            } else if (node.name.equals(SPACE_CLASS_NAME) && method.name.equals(TICK_NAME) && method.desc.equals("()I")) {
                tickMethod = true;
            } else if (node.name.equals("snoddasmannen/galimulator/Empire") && method.name.equals(CAN_BE_ASSALIZED_BY_NAME) && method.desc.equals("(Lsnoddasmannen/galimulator/Empire;)Z")) {
                vassalizedMethod = true;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    if (fieldInsn.owner.equals(SPACE_CLASS_NAME)
                            && fieldInsn.name.equals(WARS_FIELD_NAME)
                            && fieldInsn.desc.equals(WARS_FIELD_DESC)) {
                        if (warAssignNull) {
                            AbstractInsnNode replaceInsn = new InsnNode(Opcodes.POP);
                            method.instructions.set(insn, replaceInsn);
                            insn = replaceInsn;
                        } else if (warRetrieveNull) {
                            AbstractInsnNode replaceInsn = new InsnNode(Opcodes.ACONST_NULL);
                            method.instructions.set(insn, replaceInsn);
                            insn = replaceInsn;
                        } else if (tickMethod) {
                            AbstractInsnNode nextInsn = insn.getNext();
                            method.instructions.remove(insn);
                            insn = nextInsn;
                            while (insn.getOpcode() != Opcodes.GETSTATIC) {
                                nextInsn = insn.getNext();
                                if (!(insn instanceof LineNumberNode || insn instanceof LabelNode)) {
                                    method.instructions.remove(insn);
                                }
                                insn = nextInsn;
                            }
                        }
                    }
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (vassalizedMethod) {
                        if (methodInsn.getOpcode() != Opcodes.INVOKESTATIC
                                || !methodInsn.owner.equals(SPACE_CLASS_NAME)
                                || !methodInsn.name.equals(GET_PARTICIPATING_WARS_NAME)
                                || !methodInsn.desc.equals("(Lsnoddasmannen/galimulator/Empire;)Ljava/util/List;")) {
                            continue;
                        }
                        AbstractInsnNode nextInsn = insn.getNext();
                        do {
                            method.instructions.remove(insn);
                            if (insn.getOpcode() == Opcodes.INVOKEINTERFACE
                                    && ((MethodInsnNode) insn).name.equals("noneMatch")) {
                                method.instructions.insertBefore(nextInsn, new MethodInsnNode(Opcodes.INVOKESTATIC, "de/geolykt/ivyh/asm/WarCallbacks", "isLoosingNoWars", "(Lde/geolykt/starloader/api/empire/ActiveEmpire;)Z"));
                                return true;
                            }
                            insn = nextInsn;
                            nextInsn = insn.getNext();
                        } while (true);
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
        return 2002;
    }
}
