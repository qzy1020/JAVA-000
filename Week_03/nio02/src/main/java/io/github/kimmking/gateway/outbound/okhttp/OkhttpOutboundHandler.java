package io.github.kimmking.gateway.outbound.okhttp;

import com.sun.istack.internal.NotNull;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.*;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class OkhttpOutboundHandler {
    private static Logger logger = LoggerFactory.getLogger(OkhttpOutboundHandler.class);
    private String url;
    private ThreadPoolExecutor proxyService;
    private OkHttpClient client;
    public OkhttpOutboundHandler(final String url){
        this.url = url;
        int corePoolSize = 2;
        int maximumPoolSize = 4;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(2048);
        proxyService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                workQueue, new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r,"threadPool-"+r.hashCode());
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
        client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        String url = this.url + fullRequest.uri();
        proxyService.submit(()->fetchGetRquest(fullRequest, ctx, url));
    }

    public void fetchGetRquest(FullHttpRequest fullRequest, ChannelHandlerContext ctx, final String url) {
        handleRequest(true, fullRequest, ctx, url);
    }

    public void handleRequest(final boolean requestPattern, final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, String url){
        Request request = new Request.Builder().get().url(url).build();
        Call call = client.newCall(request);
        logger.info("HttpHeaders:" +request.headers());
        if (requestPattern){
            //同步请求
            synRequest(call, fullRequest, ctx);
        }else{
            //异步请求
            asynRequest(call, fullRequest, ctx);
        }
    }

    public void synRequest(Call call, FullHttpRequest fullRequest, ChannelHandlerContext ctx){
        FullHttpResponse response = null;
        try {
            HttpHeaders headers = fullRequest.headers();
            logger.info("HttpHeaders:" +headers);
            Response resp = call.execute();
            byte[] body = resp.body().bytes();
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));
            response.headers().set("Content-Type", "application/json");
            response.headers().setInt("Content-Length", Integer.parseInt(resp.header("Content-Length")));
            logger.info("response: "+ response);
        } catch (IOException e) {
            e.printStackTrace();
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            ctx.close();
        }finally {
            if (fullRequest != null) {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(response);
                }
            }
            ctx.flush();
//            ctx.close();
        }
    }

    public void asynRequest(Call call, FullHttpRequest fullRequest, ChannelHandlerContext ctx){
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                logger.info("response: "+resp);
            }
        });
    }
}
