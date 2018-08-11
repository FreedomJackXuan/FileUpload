package org.Util.sendCDN.sendFile;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

public class FullHttpRequestTest {
//    private String filename;
//    private String boundry;
//    public FullHttpRequestTest(String filename,String boundry){
//        this.filename=filename;
//        this.boundry=boundry;
//    }
    private static FullHttpRequest getHttpRequest(String filename,String boundry){
        FullHttpRequest request=new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,filename+"/Uploader");
        request.headers().set(CONTENT_TYPE,"multipart/form-data;"+boundry+"\r\n");
        return null;
    }
}
