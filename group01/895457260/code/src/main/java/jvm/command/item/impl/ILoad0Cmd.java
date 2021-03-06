package jvm.command.item.impl;

import jvm.classfile.ClassFile;
import jvm.command.CommandIterator;
import jvm.command.item.NoOperandCmd;

/**
 * Created by Haochen on 2017/4/20.
 * TODO:
 */
public class ILoad0Cmd extends NoOperandCmd {
    public ILoad0Cmd(ClassFile clzFile, String opCode, CommandIterator iterator) {
        super(clzFile, opCode, iterator);
    }

    @Override
    public String toString() {
        return this.getOffset() + ": " + this.getReadableCodeText();
    }
}
