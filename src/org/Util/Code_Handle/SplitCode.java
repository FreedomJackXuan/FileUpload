package org.Util.Code_Handle;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class SplitCode {
    private static Logger log = Logger.getLogger(SplitCode.class);
    /**
     * @param code  用户代码
     * @param server_name 容器server名字
     * @return 通过hashmap存储用户使用的容器所对应的代码
     */
    public static HashMap<String,ArrayList<String>> splitCode(String code, String server_name){
        /**
         * a.create(mysql,user)#b.create(mysql,user)
         */
        log.info("begin splitCode");
        String[] codes=code.split("#");
        String[] server_names=server_name.split("#");
        int length=server_names.length;
        HashMap<String,ArrayList<String>> hashmap=calculate_server_name(length,server_names);
        log.info("success caclulate_server_name");
        for (String content:codes){
            String[] tmp_content=content.split("\\.");
            for (String tmp_server_name:server_names){
                if ((tmp_content[0].equals(tmp_server_name))){
                    hashmap.get(tmp_server_name).add(tmp_content[1]);
                }
            }
        }
        log.info("end splitCode");
        log.info(hashmap.toString());
        return hashmap;
    }

    /**
     *
     * @param length server_name的长度
     * @param server_name server_name的名字
     * @return 返回一个hashmap，每个server_name对应一个arraylist
     */
    public static HashMap<String,ArrayList<String>> calculate_server_name(int length,String[] server_name){
        HashMap<String,ArrayList<String>> hashMap=new HashMap<>();
        log.info("begin calculate_server_name");
        for (int i=0;i<length;i++){
            ArrayList arrayList=new ArrayList();
            hashMap.put(server_name[i],arrayList);
        }
        log.info("end calculate_server_name");
        log.info(hashMap.toString());
        return hashMap;
    }
}
