/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.io.http.netty;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class MultiClientTest {

    private static final Logger logger = LoggerFactory.getLogger(MultiClientTest.class.getName());


    @Test
    public void testSimpleClient() throws Exception {
        URI[] uris = new URI[]{
            URI.create("http://www.google.com:80"),
            URI.create("http://www.google.de:80"),
        };

        ChannelFactory factory = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("simple", new SimpleChannelUpstreamHandler());
                return pipeline;
            }
            
        };
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setOption("reuseAddress", true);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("connectTimeoutMillis", "5000");
        bootstrap.setPipelineFactory(pipelineFactory);
        final CountDownLatch latch = new CountDownLatch(uris.length);
        final ChannelGroup channels = new DefaultChannelGroup();
        for (URI uri : uris) {
            InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
            ChannelFuture future = bootstrap.connect(address);
            future.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.err.println("connected to = " + future.getChannel().getRemoteAddress());
                        channels.add(future.getChannel());                      
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        logger.info("got channels = {}", channels);
        channels.close().awaitUninterruptibly();
        logger.info("channels closed");
        factory.releaseExternalResources();
    }
    
}
