package org.Server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.Bean.Message;
import org.Util.Code_Handle.HandleCode;
import org.Util.Code_Handle.SplitCode;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

//@ChannelHandler.Sharable
public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = Logger.getLogger(HttpServerInboundHandler.class);
    private Message message=new Message();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        try {
            FullHttpRequest fhr = (FullHttpRequest) msg;
            if ((fhr.method().toString().equals("POST"))){
                log.info("receive POST request");
                if (fhr.uri().equals("/ServerLess")){
                    log.info("interface correct");
                    ByteBuf buf = fhr.content();
                    String content = buf.toString(io.netty.util.CharsetUtil.UTF_8);
                    log.info(content);
                    JSONObject o=JSON.parseObject(content);
                    log.info(o.toJSONString());
                    String name=o.getString("name");
                    String code=o.getString("code");
                    String server_name=o.getString("server_name");
                    message.setServer_Name(server_name);
                    message.setName(name);
                    message.setCode(code);
                    log.info(message.toString());
                    buf.release();
                    HashMap<String,ArrayList<String>> hashMap=SplitCode.splitCode(message.getCode(),message.getServer_Name());
                    log.info("success splitCode");
                    HandleCode.judge_Code(hashMap);
                }else {
                    log.error("interface error");
                    String o="你输入的uri不正确";
                    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                            OK, Unpooled.wrappedBuffer(o.getBytes("utf-8")));
                    response.headers().set(CONTENT_TYPE, "Text/plain");
                    response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
                    if (HttpHeaderUtil.isKeepAlive(fhr)) {
                        response.headers().set(CONNECTION, KEEP_ALIVE);
                    }
                    ctx.writeAndFlush(response);
                }
            }else {
                log.error("not POST request");
                String o="你的请求方法不正确";
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                        OK, Unpooled.wrappedBuffer(o.getBytes("utf-8")));
                response.headers().set(CONTENT_TYPE, "Text/plain");
                response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
                if (HttpHeaderUtil.isKeepAlive(fhr)) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
                ctx.writeAndFlush(response);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        log.debug("--------------------------------------");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        ctx.close();
    }

}