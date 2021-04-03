package com.geek.nettyserverdemo;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;

public class HttpHandler extends ChannelInboundHandlerAdapter {

    private HttpOutboundHandler httpOutboundHandler = new HttpOutboundHandler();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
            String uri = fullHttpRequest.uri();
            if (uri.contains("/test")) {
                handlerTest(fullHttpRequest, ctx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handlerTest(FullHttpRequest httpRequest, ChannelHandlerContext ctx) {
        /*FullHttpResponse response = null;

        try {
            String responseMsg = "hello world";

            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseMsg.getBytes(Charset.forName("utf-8"))));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", response.content().readableBytes());
        } catch (Exception e) {
            System.out.println("处理出错" + e.getMessage());
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
        } finally {
            if (httpRequest != null) {
                if (!HttpUtil.isKeepAlive(httpRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    response.headers().set("Connection", "Keep-Alive");
                    ctx.write(response);
                }
            }
        }*/

        httpOutboundHandler.handler(httpRequest, ctx);

    }
}
