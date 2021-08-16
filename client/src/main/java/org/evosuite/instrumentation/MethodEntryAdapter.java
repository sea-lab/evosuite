/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.instrumentation;

import org.evosuite.PackageInfo;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Instrument classes to keep track of method entry and exit
 *
 * @author Gordon Fraser
 */
public class MethodEntryAdapter extends AdviceAdapter {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(MethodEntryAdapter.class);

    String className;
    String methodName;
    String fullMethodName;
    int access;

    String methodSignature;
    Type[] types;

    /**
     * <p>Constructor for MethodEntryAdapter.</p>
     *
     * @param mv         a {@link org.objectweb.asm.MethodVisitor} object.
     * @param access     a int.
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param desc       a {@link java.lang.String} object.
     */
    public MethodEntryAdapter(MethodVisitor mv, int access, String className,
                              String methodName, String desc) {
        super(Opcodes.ASM5, mv, access, methodName, desc);
        this.className = className;
        this.methodName = methodName;
        this.fullMethodName = methodName + desc;
        this.access = access;

//        this.methodSignature = methodName + "(";
        //constructor chi?
        //assert ham mikhad?
//        types = Type.getArgumentTypes(desc);
//        String typesString = "";
//        if (types.length == 0) {
//            typesString = "<NO_PARAM>";
//        }
//        for (int i = 0; i < types.length; i++) {
//            typesString = typesString + getClassName(types[i]) + "<VALUE>";
//            if (i != types.length - 1) {
//                typesString += "<NEXT_PARAM>";
//            }
//            System.out.println(types[i].getClassName());
//            System.out.println(getClassName(types[i]));
//            System.out.println("-----------------");
//        }
//
//        this.methodSignature += typesString + ")";
    }

    private String getClassName(Type type) {
        switch (type.getSort()) {
            case 0:
                return "<NO_ARG>";
            case 1:
                return "Boolean";
            case 2:
                return "Char";
            case 3:
                return "Byte";
            case 4:
                return "Short";
            case 5:
                return "Integer";
            case 6:
                return "Float";
            case 7:
                return "Long";
            case 8:
                return "Double";
            case 9:
                StringBuilder sb = new StringBuilder(type.getElementType().getClassName());

                for (int i = type.getDimensions(); i > 0; --i) {
                    sb.append("[]");
                }

                return sb.toString();
            case 10:
                return type.getClassName().substring(type.getClassName().lastIndexOf(".") + 1);
            default:
                return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodEnter() {

        if (methodName.equals("<clinit>"))
            return; // FIXXME: Should we call super.onMethodEnter() here?

        mv.visitLdcInsn(className);
        mv.visitLdcInsn(fullMethodName);
        if ((access & Opcodes.ACC_STATIC) > 0) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
        }
//        loadSign(types);
        mv.visitLdcInsn(methodName);
        loadArgArray();

        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                PackageInfo.getNameWithSlash(ExecutionTracer.class),
                "enteredMethod",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)V", false);

        super.onMethodEnter();
    }

    public void loadSign(Type[] types) {
        push(types.length);
        Type OBJECT_TYPE = Type
                .getObjectType("java/lang/Object");
        newArray(OBJECT_TYPE);
        for (int i = 0; i < types.length; i++) {
            dup();
            push(i);
            loadArg(i);
            box(types[i]);
            arrayStore(OBJECT_TYPE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMethodExit(int opcode) {
        // TODO: Check for <clinit>

        if (opcode != Opcodes.ATHROW) {

            mv.visitLdcInsn(className);
            mv.visitLdcInsn(fullMethodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    PackageInfo.getNameWithSlash(org.evosuite.testcase.execution.ExecutionTracer.class),
                    "leftMethod", "(Ljava/lang/String;Ljava/lang/String;)V", false);
        }
        super.onMethodExit(opcode);
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        int maxNum = 3;
        super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
    }
}
