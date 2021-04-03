package com.geek.nettyserverdemo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class HeaderHttpRequestFilter implements HttpRequestFilter {
    public void filter(FullHttpRequest request, ChannelHandlerContext ctx) {
        request.headers().add("geek-level", "10");
    }
}
