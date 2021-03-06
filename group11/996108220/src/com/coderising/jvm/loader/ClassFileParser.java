package com.coderising.jvm.loader;
import com.coderising.jvm.attr.CodeAttr;
import com.coderising.jvm.attr.LineNumberTable;
import com.coderising.jvm.attr.LocalVariableTable;
import com.coderising.jvm.clz.AccessFlag;
import com.coderising.jvm.clz.ClassFile;
import com.coderising.jvm.clz.ClassIndex;
import com.coderising.jvm.constant.ClassInfo;
import com.coderising.jvm.constant.ConstantPool;
import com.coderising.jvm.constant.FieldRefInfo;
import com.coderising.jvm.constant.MethodRefInfo;
import com.coderising.jvm.constant.NameAndTypeInfo;
import com.coderising.jvm.constant.NullConstantInfo;
import com.coderising.jvm.constant.StringInfo;
import com.coderising.jvm.constant.UTF8Info;
import com.coderising.jvm.field.Field;
import com.coderising.jvm.method.Method;
import com.coderising.jvm.util.Util;
import com.sun.org.apache.bcel.internal.classfile.LineNumber;

public class ClassFileParser {

	public ClassFile parse(byte[] codes) {
		ByteCodeIterator iter=new ByteCodeIterator(codes);
		String magicNumber=iter.nextU4ToHexString();
		if (!magicNumber.equals("cafebabe")) {
			return null;
		}
		ClassFile classFile=new ClassFile();
		classFile.setMinorVersion(iter.nextU2ToInt());
		classFile.setMajorVersion(iter.nextU2ToInt());
		ConstantPool pool=parseConstantPool(iter);
		classFile.setConstPool(pool);
		classFile.setAccessFlag(parseAccessFlag(iter));
		classFile.setClassIndex(parseClassInfex(iter));
		parseInterfaces(iter);
		int fieldCount=iter.nextU2ToInt();
		for (int i = 0; i <fieldCount ; i++) {
			classFile.addField(parseField(iter,pool));
		}
		int methodCount=iter.nextU2ToInt();
		for (int i = 0; i < methodCount; i++) {
			classFile.addMethod(parseMethod(iter, classFile));
		}
		return classFile;
	}

	private Method parseMethod(ByteCodeIterator iter,ClassFile clzFile) {
		
		int accessFlag=iter.nextU2ToInt();	
		int nameIndex=iter.nextU2ToInt();
		int descriptorIndex=iter.nextU2ToInt();
		Method method= new Method(clzFile, accessFlag, nameIndex, descriptorIndex);
		int attrCount=iter.nextU2ToInt();
		int attrNameIndex=iter.nextU2ToInt();
		int attrLen=iter.nextU4ToInt();
		int maxStack=iter.nextU2ToInt();
		int maxLocals=iter.nextU2ToInt();
		int codeLen=iter.nextU4ToInt();
		String code=iter.getByte(codeLen).toString();
		CodeAttr codeAttr=new CodeAttr(attrNameIndex, attrLen, maxStack, maxLocals, codeLen, code);
		int exceptionLength=iter.nextU2ToInt();
		int count=iter.nextU2ToInt();
		for (int i = 0; i < count; i++) {
			int attrNameIndex1=iter.nextU2ToInt();
			UTF8Info utf8Info=(UTF8Info) clzFile.getConstantPool().getConstantInfo(attrNameIndex1);
			String attrName=utf8Info.getValue();
			if (attrName.equals("LineNumberTable")) {
				int attrLen1=iter.nextU4ToInt();
				LineNumberTable lnTable=new LineNumberTable(attrNameIndex1, attrLen1);
				lnTable.parse(iter);	
			}
			else if (attrName.equals("LocalVariableTable")) {
				int attrLen1=iter.nextU4ToInt();
				LocalVariableTable lvTable=new LocalVariableTable(attrNameIndex1, attrLen1);
				lvTable.parse(iter);	
			}
			else {
				throw new RuntimeException();
			}
		}
		method.setCodeAttr(codeAttr);
		return method;
	}

	private Field parseField(ByteCodeIterator iter,ConstantPool pool) {
		int accessFlag=iter.nextU2ToInt();
	    int	nameIndex=iter.nextU2ToInt();

	    int	descriptorIndex=iter.nextU2ToInt();
		Field field=new Field(accessFlag, nameIndex, descriptorIndex, pool);
		iter.nextU2ToInt();
		return field;
	}

	private void parseInterfaces(ByteCodeIterator iter) {
		// TODO Auto-generated method stub
		int interfaceCount = iter.nextU2ToInt();

		System.out.println("interfaceCount:" + interfaceCount);

	}

	private AccessFlag parseAccessFlag(ByteCodeIterator iter) {

		return new AccessFlag(iter.nextU2ToInt());
	}

	private ClassIndex parseClassInfex(ByteCodeIterator iter) {
		ClassIndex classIndex=new ClassIndex();
		classIndex.setThisClassIndex(iter.nextU2ToInt());
		classIndex.setSuperClassIndex(iter.nextU2ToInt());
		return classIndex;

	}

	private ConstantPool parseConstantPool(ByteCodeIterator iter) {
		int constPoolCount=iter.nextU2ToInt();
		
		ConstantPool pool=new ConstantPool();
		pool.addConstantInfo(new NullConstantInfo());
		for (int i = 1; i <= constPoolCount-1; i++) {
			int tag=iter.next();
			if (tag==1) {
				//UTF8Info
				UTF8Info utf8Info=new UTF8Info(pool);
				int length=iter.nextU2ToInt();
				utf8Info.setLength(length);
				String value=Util.byteToString(iter.getByte(length));
				utf8Info.setValue(value);
				pool.addConstantInfo(utf8Info);
			}
			else if (tag==7) {
				//ClassInfo
				ClassInfo classInfo=new ClassInfo(pool);
				int utf8Index=iter.nextU2ToInt();
				classInfo.setUtf8Index(utf8Index);
				pool.addConstantInfo(classInfo);
			}
			else if (tag==8) {
				//StringInfo
				StringInfo stringInfo=new StringInfo(pool);
				int index=iter.nextU2ToInt();
				stringInfo.setIndex(index);
				pool.addConstantInfo(stringInfo);
			}
			else if (tag==9) {
				//FieldRefInfo
				FieldRefInfo fieldRefInfo=new FieldRefInfo(pool);
				int classInfoIndex=iter.nextU2ToInt();
				int nameAndTypeIndex=iter.nextU2ToInt();
				fieldRefInfo.setClassInfoIndex(classInfoIndex);
				fieldRefInfo.setNameAndTypeIndex(nameAndTypeIndex);
				pool.addConstantInfo(fieldRefInfo);
			}
			else if (tag==10) {
				//MethodRefInfo
				MethodRefInfo methodRefInfo=new MethodRefInfo(pool);
				int classInfoIndex=iter.nextU2ToInt();
				int nameAndTypeIndex=iter.nextU2ToInt();
				methodRefInfo.setClassInfoIndex(classInfoIndex);
				methodRefInfo.setNameAndTypeIndex(nameAndTypeIndex);
				pool.addConstantInfo(methodRefInfo);
			}
			else if (tag==12) {
				//NameAndTypeInfo
				NameAndTypeInfo nameAndTypeInfo=new NameAndTypeInfo(pool);
				int index1=iter.nextU2ToInt();
				int index2=iter.nextU2ToInt();
				nameAndTypeInfo.setIndex1(index1);
				nameAndTypeInfo.setIndex2(index2);
				pool.addConstantInfo(nameAndTypeInfo);
			}
			else {
				new RuntimeException("缺少tag为"+tag+"的常量");
			}
		}

		return pool;
	}

	
}
