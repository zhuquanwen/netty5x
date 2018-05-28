package com.zqw.netty5x;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Server2 {
    public static void main(String[] args) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try{
            bootstrap.group(boss, worker);
            bootstrap.channel(NioServerSocketChannel.class); //注册factory
            bootstrap.childHandler(new ChannelInitializer<Channel>() { //注册piepline
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new StringEncoder());
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new ServerHandler());
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024); //服务端排队队列
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true); //TCP无掩饰
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); //清除死连接，维持活跃的
            ChannelFuture future = bootstrap.bind(7777);
            System.out.println("服务端启动！");
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
