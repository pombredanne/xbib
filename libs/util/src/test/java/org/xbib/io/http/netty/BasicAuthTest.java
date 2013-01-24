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
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

public class BasicAuthTest {

    private static final String USERNAME = "user1";
    private static final String PASSWORD = "user1";
    private static final URI resource = URI.create("http://test.webdav.org:80/auth-basic/");

    
    public void testAuth() throws Exception {

        ClientBootstrap client = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        client.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("codec", new HttpClientCodec());
                pipeline.addLast("aggregator", new HttpChunkAggregator(5242880));
                pipeline.addLast("authHandler", new ClientMessageHandler());
                return pipeline;
            }
        });

        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, 
                HttpMethod.GET, resource.toASCIIString());
        request.addHeader(HttpHeaders.Names.HOST, resource.getHost());

        String authString = USERNAME + ":" + PASSWORD;
        ChannelBuffer authChannelBuffer = ChannelBuffers.copiedBuffer(authString, CharsetUtil.UTF_8);
        ChannelBuffer encodedAuthChannelBuffer = Base64.encode(authChannelBuffer);
        request.addHeader(HttpHeaders.Names.AUTHORIZATION, encodedAuthChannelBuffer.toString(CharsetUtil.UTF_8));
        client.connect(new InetSocketAddress(resource.getHost(), resource.getPort()))
                .awaitUninterruptibly()
                .getChannel().write(request).awaitUninterruptibly()
                .getChannel().getCloseFuture().awaitUninterruptibly();
        System.err.println("request done, server closed connection");
        client.releaseExternalResources();
    }

    class ClientMessageHandler extends SimpleChannelHandler {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            e.getCause().printStackTrace();
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            HttpResponse httpResponse = (HttpResponse) e.getMessage();
            HttpResponseStatus status = httpResponse.getStatus();
            String content = httpResponse.getContent().toString(CharsetUtil.UTF_8);
            System.err.println("status = " + status.getCode() + " content = " + content);
        }
    }
}
