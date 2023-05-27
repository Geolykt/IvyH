package de.geolykt.ivyh.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.geolykt.starloader.transformers.ASMTransformer;

public class WarASMTransformer extends ASMTransformer {

    @Override
    public boolean accept(@NotNull ClassNode node) {
        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") && method.desc.equals("()V")) {
                return false;
            }
        }
        MethodNode injectedCtor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        injectedCtor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        injectedCtor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.superName, "<init>", "()V"));
        for (FieldNode field : node.fields) {
            if ((field.access & Opcodes.ACC_STATIC) != 0) {
                continue;
            }
            injectedCtor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            switch (field.desc.codePointAt(0)) {
            case 'L':
            case '[':
                injectedCtor.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                break;
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
                injectedCtor.instructions.add(new InsnNode(Opcodes.ICONST_0));
                break;
            case 'J':
                injectedCtor.instructions.add(new InsnNode(Opcodes.LCONST_0));
                break;
            case 'F':
                injectedCtor.instructions.add(new InsnNode(Opcodes.FCONST_0));
                break;
            case 'D':
                injectedCtor.instructions.add(new InsnNode(Opcodes.DCONST_0));
                break;
            }
            injectedCtor.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, node.name, field.name, field.desc));
        }
        injectedCtor.instructions.add(new InsnNode(Opcodes.RETURN));
        injectedCtor.maxLocals = 3;
        injectedCtor.maxStack = 3;
        node.methods.add(injectedCtor);
        return true;
    }

    @Override
    public boolean isValidTarget(@NotNull String internalName) {
        return internalName.equals("snoddasmannen/galimulator/War");
    }

    @Override
    public int getPriority() {
        return 2000;
    }
}
