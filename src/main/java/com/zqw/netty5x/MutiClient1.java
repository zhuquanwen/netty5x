package com.zqw.netty5x;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class MutiClient1 {
    private Bootstrap bootstrap = new Bootstrap();
    private final AtomicInteger index = new AtomicInteger();
    //会话对象组
    private List<Channel> channels = new ArrayList<>();
    public void init(int count){
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new StringEncoder());
                ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast(new ClientHandler1());
            }
        });
        for(int i=0; i<count;i++){
            Channel channel = bootstrap
                    .connect(new InetSocketAddress("192.168.1.4", 7777)).channel();
            channels.add(channel);
        }

    }
    public Channel next(){
        //获取可用的channel
        return getChannel(0);
    }
    private Channel getChannel(int count){
        Channel channel = channels.get(Math.abs(this.index.getAndIncrement()%channels.size()));
        if(count >= channels.size()){
            throw new RuntimeException("没有足够的channel");
        }
        if(!channel.isActive()){
            //重连
            reconnect(channel);
            //尝试获取下一个channel
            return getChannel(++count);
        }
        return channel;
    }

    private void reconnect(Channel channel) {
        synchronized (channel){
            int index = channels.indexOf(channel);
            channel =  bootstrap
                    .connect(new InetSocketAddress("192.168.1.4", 7777)).channel();
            channels.set(index, channel);
        }
    }

    public static void main(String[] args) {
        MutiClient1 mutiClient = new MutiClient1();
        mutiClient.init(10);
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("请输入:");
            String str = scanner.next();
            try {
                Channel channel = mutiClient.next();
                channel.writeAndFlush(str);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

}
