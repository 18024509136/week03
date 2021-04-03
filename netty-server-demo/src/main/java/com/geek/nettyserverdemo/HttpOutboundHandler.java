package com.geek.nettyserverdemo;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HttpOutboundHandler {

    private CloseableHttpAsyncClient httpAsyncClient;

    private ExecutorService executorService;

    private HttpRequestFilter httpRequestFilter = new HeaderHttpRequestFilter();

    private HttpResponseFilter httpResponseFilter = new HeaderHttpResponseFilter();

    private HttpEndPointRouter httpEndPointRouter = new RandomHttpEndpointRouter();

    private static List<String> backendUrls;

    private static final String ERROR_MSG = "系统异常";

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("application");
        String backendString = bundle.getString("backends");
        backendUrls = Arrays.stream(backendString.split(",")).collect(Collectors.toList());
    }

    public HttpOutboundHandler() {
        int cores = Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
        this.executorService = new ThreadPoolExecutor(cores, 4 * cores, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100), callerRunsPolicy);


        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(2000)
                .setSoTimeout(2000)
                .setIoThreadCount(4 * cores)
                .setRcvBufSize(4 * 1024)
                .build();
        httpAsyncClient = HttpAsyncClients.custom()
                .setDefaultIOReactorConfig(ioReactorConfig)
                .setMaxConnTotal(40)
                .setMaxConnPerRoute(8)
                .setKeepAliveStrategy((response, context) -> 60000)
                .build();
        httpAsyncClient.start();
    }

    public void handler(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
        // 获取目标host
        String backend = this.httpEndPointRouter.route(this.backendUrls);
        // 拼接URL
        String url = backend + "/" + fullHttpRequest.uri();
        // 添加请求头
        httpRequestFilter.filter(fullHttpRequest, ctx);
        // 请求后端
        executorService.submit(() -> requestBackend(fullHttpRequest, ctx, url));
    }

    private void requestBackend(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx, String url) {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);

        httpAsyncClient.execute(httpGet, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                handlerResponse(fullHttpRequest, ctx, response);
            }

            @Override
            public void failed(Exception e) {
                e.printStackTrace();
                httpGet.abort();
            }

            @Override
            public void cancelled() {
                httpGet.abort();
            }
        });
    }

    private void handlerResponse(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx, HttpResponse httpResponse) {
        FullHttpResponse fullHttpResponse = null;
        byte[] bytes;

        try {
            bytes = EntityUtils.toByteArray(httpResponse.getEntity());
            fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));
            fullHttpResponse.headers().add(HTTP.CONTENT_TYPE, "application/json");
            fullHttpResponse.headers().add(HTTP.CONTENT_LEN, Integer.parseInt(httpResponse.getFirstHeader(HTTP.CONTENT_LEN).getValue()));

            httpResponseFilter.filter(fullHttpResponse, ctx);
        } catch (Exception e) {
            fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            exceptionCaught(ctx, e);
        } finally {
            if (fullHttpRequest != null) {
                if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
                    ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(fullHttpResponse);
                }
            }
            ctx.flush();
        }


    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // ctx.close();
    }
}
