package io.github.kimmking.gateway.outbound.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

public class NettyHttpClientOutboundHandler  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        
        
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        try{
            FullHttpResponse response = (FullHttpResponse) msg;
            ByteBuf content = response.content();
            HttpHeaders headers = response.headers();
        }finally {
            ctx.flush();
        }
    }
}