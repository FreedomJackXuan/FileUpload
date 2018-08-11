package org.Util.sendCDN.firstSend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.Bean.breakpoint.Bean_File;
import org.Bean.breakpoint.Breakpoint_Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

public class GetPostTest {
    /**
     * 向指定URL发送POST方法的请求
     *
     * @param url   发送请求的URL
     * @return URL所代表远程资源的响应
     */
    public static String sendPost(String url,LinkedList<String> file) {
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //想要上传的文件
            String total_file = null;
            for (String file_name:file){
                total_file += file_name+"#";
            }
            //文件名
            String body=total_file;
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("content-length",body.getBytes("utf-8").length+"");
            conn.setRequestProperty("content-type","multipart/form-data;ask");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            try (
                    // 获取URLConnection对象对应的输出流
                    PrintWriter out = new PrintWriter(conn.getOutputStream())) {
                // 发送请求参数
                out.print(body);
                // flush输出流的缓冲
                out.flush();
            }
            try (
                    // 定义BufferedReader输入流来读取URL的响应
                    BufferedReader in = new BufferedReader(new InputStreamReader
                            (conn.getInputStream(), "utf-8"))) {
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            }
        } catch (Exception e) {
            System.out.println("发送POST请求出现异常！" + e);
            e.printStackTrace();
        }
        return result;
    }
    //将数据存放在javaBean里

    public static Breakpoint_Message decoder(String result){
        // "file":[{name:”headimage”,range:”0-102400”}]}
        JSONObject object= JSON.parseObject(result);
        String support=object.getString("support");
        String protocol=object.getString("protocol");
        JSONArray jarr=object.getJSONArray("file");
        Breakpoint_Message message=new Breakpoint_Message();
        LinkedList<Bean_File> list=new LinkedList<>();
        message.setSupport(support);
        message.setProtocol(protocol);
        for (int i=0;i<jarr.size();i++){
            JSONObject jsonObject=jarr.getJSONObject(i);
            String filename=jsonObject.getString("name");
            String range=jsonObject.getString("range");
            Bean_File file=new Bean_File();
            file.setName(filename);
            file.setRange(range);
            list.add(file);
        }
        message.setFile(list);
        return message;
    }
}
