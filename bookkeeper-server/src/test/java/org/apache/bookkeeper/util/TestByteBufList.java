package org.apache.bookkeeper.util;

import static org.junit.Assert.*;

import org.apache.bookkeeper.util.ByteBufList.Encoder;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestByteBufList {

    private Integer minCapacity;
    private Integer maxCapacity;
    private static String dir = "tmpWrite";
    private static String fileName = "writeMockTest.log";
    private Object buf;
    private String stringPart1;
    private String stringPart2;
    private String prefix;
    private Integer numBytes;
    private String suffix;
    private Integer numAdding;
    private Encoder encoder;

    @BeforeClass
    public static void setupEnvironment() {
        // Create the directories if do not exist
        if (!Files.exists(Paths.get(dir))) {
            File tmpDir = new File(dir);
            tmpDir.mkdir();
        }

    }

    @After
    public void tearDown() {
        // Delete test file if exists
        if (Files.exists(Paths.get(dir, fileName))) {
            File testFile = new File(dir,fileName);
            testFile.delete();
        }
    }

    @Test
    public void testDoubleAndClone() throws Exception {

        ByteBuf b1 = PooledByteBufAllocator.DEFAULT.heapBuffer(this.minCapacity, this.maxCapacity);
        b1.writerIndex(b1.capacity());
        ByteBuf b2 = PooledByteBufAllocator.DEFAULT.heapBuffer(this.minCapacity, this.maxCapacity);
        b2.writerIndex(b2.capacity());
        ByteBufList buf = ByteBufList.get(b1, b2);

        assertEquals(b1, buf.getBuffer(0));
        assertEquals(b2, buf.getBuffer(1));

        ByteBufList clone = ByteBufList.clone(buf);
        assertEquals(2, clone.size());
        assertEquals(b1.capacity()+b2.capacity(), clone.readableBytes());
        assertEquals(b1, clone.getBuffer(0));
        assertEquals(b2, clone.getBuffer(1));

        clone.release();
        buf.release();
    }


    @Test
    public void testGetBytesAndArrays() throws Exception {
        ByteBufList bufList = ByteBufList.get(Unpooled.wrappedBuffer(this.stringPart1.getBytes()),
                Unpooled.wrappedBuffer(this.stringPart2.getBytes()));

        assertArrayEquals((this.stringPart1+this.stringPart2).getBytes(), bufList.toArray());

        bufList.prepend(Unpooled.wrappedBuffer(this.prefix.getBytes()));
        assertArrayEquals((this.prefix+this.stringPart1+this.stringPart2).getBytes(), bufList.toArray());

        for(Integer i = 0; i<this.numAdding;i++) {
            bufList.add(Unpooled.wrappedBuffer((this.suffix+i.toString()).getBytes()));
        }
        assertEquals(Unpooled.wrappedBuffer(bufList.toArray()), ByteBufList.coalesce(bufList));

        byte[] buffBytes = new byte[this.numBytes];
        int res = bufList.getBytes(buffBytes);

        assertEquals((this.prefix+this.stringPart1+this.stringPart2).length(), res);

    }


    @Test
    public void testEncoder() throws Exception {

        String value = null;

        ChannelHandlerContext ctx = new ChannelHandlerContextMock();
        ChannelPromise cp = mock(ChannelPromise.class);


        this.encoder.write(ctx, this.buf, cp);

        try (FileInputStream fis = new FileInputStream(dir + "/" +fileName)) {
            byte[] byteStr = this.buf.toString().getBytes();
            int i = 0;

            do {

                byte[] readStr = new byte[byteStr.length];
                i = fis.read(readStr);

                value = new String(readStr, StandardCharsets.UTF_8);


            } while (i != -1);

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        assertEquals((String) buf,value);

    }

    class ChannelHandlerContextMock implements ChannelHandlerContext {
        @Override
        public ChannelFuture bind(SocketAddress localAddress) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
            return null;
        }

        @Override
        public ChannelFuture disconnect() {
            return null;
        }

        @Override
        public ChannelFuture close() {
            return null;
        }

        @Override
        public ChannelFuture deregister() {
            return null;
        }

        @Override
        public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture disconnect(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture close(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture deregister(ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture write(Object msg) {
            try (FileOutputStream fos = new FileOutputStream(dir + "/" +fileName)) {
                byte[] byteStr = msg.toString().getBytes();
                fos.write(byteStr);
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        public ChannelFuture write(Object msg, ChannelPromise promise) {

            try (FileOutputStream fos = new FileOutputStream(dir + "/" +fileName)) {
                byte[] byteStr = msg.toString().getBytes();
                fos.write(byteStr);
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
            return null;
        }

        @Override
        public ChannelFuture writeAndFlush(Object msg) {
            return null;
        }

        @Override
        public ChannelPromise newPromise() {
            return null;
        }

        @Override
        public ChannelProgressivePromise newProgressivePromise() {
            return null;
        }

        @Override
        public ChannelFuture newSucceededFuture() {
            return null;
        }

        @Override
        public ChannelFuture newFailedFuture(Throwable cause) {
            return null;
        }

        @Override
        public ChannelPromise voidPromise() {
            return null;
        }

        @Override
        public Channel channel() {
            return null;
        }

        @Override
        public EventExecutor executor() {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public ChannelHandler handler() {
            return null;
        }

        @Override
        public boolean isRemoved() {
            return false;
        }

        @Override
        public ChannelHandlerContext fireChannelRegistered() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelUnregistered() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelActive() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelInactive() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
            return null;
        }

        @Override
        public ChannelHandlerContext fireUserEventTriggered(Object evt) {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelRead(Object msg) {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelReadComplete() {
            return null;
        }

        @Override
        public ChannelHandlerContext fireChannelWritabilityChanged() {
            return null;
        }

        @Override
        public ChannelHandlerContext read() {
            //TODO
            return null;
        }

        @Override
        public ChannelHandlerContext flush() {
            return null;
        }

        @Override
        public ChannelPipeline pipeline() {
            return null;
        }

        @Override
        public ByteBufAllocator alloc() {
            return null;
        }

        @Override
        @Deprecated
        public <T> Attribute<T> attr(AttributeKey<T> key) {
            return null;
        }

        @Override
        @Deprecated
        public <T> boolean hasAttr(AttributeKey<T> key) {
            return false;
        }

    }

}