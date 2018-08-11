package org.Util.Code_Handle;
import org.Bean.breakpoint.Bean_File;
import org.Bean.breakpoint.Breakpoint_Message;
import org.Util.StaticLangue.Lang;
import org.Util.sendCDN.firstSend.GetPostTest;
import org.Util.sendCDN.sendFile.Client;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HandleCode {
    private static Logger log = Logger.getLogger(HandleCode.class);
    private static ExecutorService pool=Executors.newFixedThreadPool(10);
    /**
     *  判断用户所写的代码是应用还是在容器部署环境
     * @param hashMap
     */
    public static void judge_Code(HashMap<String,ArrayList<String>> hashMap) throws FileNotFoundException {
        //容器server部署环境的文件列表
        LinkedList<String> rongqi_list=new LinkedList<>();
        //应用server的文件列表
        LinkedList<String> yingyong_list=new LinkedList<>();
        for (String server_name:hashMap.keySet()) {
            ArrayList<String> code = hashMap.get(server_name);
            for (String every_code : code) {
                String[] tmp_code = every_code.split("\\(");
                if (tmp_code[0].equals("deploy")) {
                    log.info("deploy " + server_name + " server environment");
                    if (tmp_code[1].contains("database:mysql")) {
                        rongqi_list.add("/home/jac/桌面/123.zip");
                    }
                }
            }
            //向CDN发文件，并向容器部署环境
            if (rongqi_list.size()>0) {
                log.info("向CDN传容器所用到的文件");
                String data = GetPostTest.sendPost("127.0.0.1:8080", rongqi_list);
                log.info("第一次请求断点续传的响应体: " + data);
                //将第一次服务器返回的数据存入javabean里
                Breakpoint_Message message = GetPostTest.decoder(data);
                log.info(message.toString());
                if (message.getProtocol().equals("true")) {
                    log.info("可以进行断点续传,开始上传");
                    for(Bean_File file : message.getFile()) {
                        sendFile(file);
                    }
                } else {
                    log.info("CDN出现问题,不可以进行断点续传");
                    return;
                }
            }
            //向CDN发文件,并向应用发送文件
            if (yingyong_list.size()>0){
                log.info("向CDN传应用所用到的文件");
            }
        }
        pool.shutdown();
        log.info("aaaaaaaaaaaaaa");
    }

    /**
     * //向CDN上传 封装好的包
     * @param file 需要上传的包信息
     */
    public static void sendFile(Bean_File file){
        try {
            RandomAccessFile randomAccessFile=new RandomAccessFile(file.getName(),"rw");
            long length=randomAccessFile.length();
            String range=file.getRange();
            int head_read=Integer.parseInt(range.split("-")[0]);
            int tail_read=Integer.parseInt(range.split("-")[1]);
            //此文件CDN没有接收
            if ((head_read==0)&&(tail_read==0)){
                log.info("从头开始发送文件");
                long time= (length/ Lang.byte_length);
                long other_time=length%Lang.byte_length;
                if (time==0){
                    //此文件小于1048576byte
                    log.info("此文件小于1048576byte");
                    byte[] bytes=new byte[(int) length];
                    randomAccessFile.read(bytes,0, (int) length);
                    String send_range="bytes=0-"+length+"/"+length;
                    Thread thread=new Thread(new Client(8080,"127.0.0.1",bytes,send_range, file.getName()));
                    pool.execute(thread);
                }else {
                    //此文件大于1048576byte,分块发送
                    log.info("此文件大于1048576byte,分块发送");
                    for (int i=0;i<time;i++){
                        byte[] bytes=new byte[(int) Lang.byte_length];
                        int begin= (int) (Lang.byte_length*i);
                        int end= (int) (begin+Lang.byte_length);
                        randomAccessFile.read(bytes,begin, (int) Lang.byte_length);
                        String send_range="bytes="+begin+"-"+end+"/"+length;
                        Thread thread=new Thread(new Client(8080,"127.0.0.1",bytes,send_range, file.getName()));
                        pool.execute(thread);
                    }
                    if (other_time!=0){
                        log.info("此文件按1048576byte分块后还有剩余");
                        byte[] bytes=new byte[(int) other_time];
                        String send_range="bytes="+(time*Lang.byte_length)+"-"+length+"/"+length;
                        int begin= (int) (time*Lang.byte_length);
                        randomAccessFile.read(bytes,begin, (int) other_time);
                        Thread thread=new Thread(new Client(8080,"127.0.0.0.1",bytes,send_range,file.getName()));
                        pool.execute(thread);
                    }
                }
                //此文件已被CDN接收过,并且是从头接收并未接收完
            }else if ((head_read==0)&&(tail_read!=0)){
                if (tail_read==length){
                    log.info("此文件已经接收完全");
                }else if (tail_read<length){
                    log.info("此文件未接收完全");
                    long should_send=length-tail_read; //应该发送的文件大小
                    long time=should_send/Lang.byte_length; // 按102400字节分割大小后的次数
                    long other_time=should_send%Lang.byte_length;//余下的字节
                    if (time==0){
                        //剩余的文件比1048576小
                        log.info("剩余的文件比1048576小");
                        byte[] bytes=new byte[(int) should_send];
                        int begin=tail_read; //开始发送的位置
                        int end= (int) length;//结束发送的位置
                        randomAccessFile.read(bytes,begin,(int) should_send);
                        String send_range="bytes="+begin+"-"+end+"/"+length;
                        log.info("发送的信息----文件名为："+file.getName()+"应该发送文件的大小："+should_send+"开始发送的位置:"+begin+"结束发送的位置:"+end+"range:"+send_range);
                        Thread thread=new Thread(new Client(8080,"127.0.0.1",bytes,send_range,file.getName()));
                        pool.execute(thread);
                    }else {
                        //剩余的文件比1048576大,分块发送
                        log.info("剩余的文件比1048576大,分块发送");
                        for (int i=0;i<time;i++){
                            byte[] bytes=new byte[(int) Lang.byte_length];
                            long begin=(tail_read+(i*Lang.byte_length));
                            long end=begin+Lang.byte_length;
                            randomAccessFile.read(bytes,(int)begin,(int)Lang.byte_length);
                            String send_range="bytes="+begin+"-"+end+"/"+length;
                            log.info("发送的信息----文件名为："+file.getName()+"应该发送文件的大小："+should_send+"开始发送的位置:"+begin+"结束发送的位置:"+end+"range:"+send_range);
                            Thread thread=new Thread(new Client(8080,"127.0.0.1",bytes,send_range,file.getName()));
                            pool.execute(thread);
                        }
                        if (other_time!=0){
                            log.info("此文件按1048576byte分块后还有剩余");
                            byte[] bytes=new byte[(int) other_time];
                            long begin=tail_read+(Lang.byte_length*time);
                            long end=begin+other_time;
                            randomAccessFile.read(bytes,(int)begin, (int) end);
                            String send_range="bytes="+begin+"-"+end+"/"+length;
                            log.info("发送的信息----文件名为："+file.getName()+"应该发送文件的大小："+should_send+"开始发送的位置:"+begin+"结束发送的位置:"+end+"range:"+send_range);
                            Thread thread=new Thread(new Client(8080,"127.0.0.1",bytes,send_range,file.getName()));
                            pool.execute(thread);
                        }
                    }
                }
            }
            pool.shutdown();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}