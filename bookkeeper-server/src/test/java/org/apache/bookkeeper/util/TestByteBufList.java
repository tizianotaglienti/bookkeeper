package org.apache.bookkeeper.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import org.apache.bookkeeper.util.ByteBufList.Encoder;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


@RunWith(Parameterized.class)
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
    private static File testFile;

    public TestByteBufList(TestInput ti){
        this.minCapacity = ti.getMinCapacity();
        this.maxCapacity = ti.getMaxCapacity();
        this.buf = ti.getBuf();
        this.stringPart1 = ti.getStringPart1();
        this.stringPart2 = ti.getStringPart2();
        this.prefix = ti.getPrefix();
        this.numBytes = ti.getNumBytes();
        this.suffix = ti.getSuffix();
        this.numAdding = ti.getNumAdding();
        this.encoder = ti.getEncoder();
    }

    @Parameterized.Parameters
    public static Collection<TestInput[]> getTestParameters() {
        Collection<TestInput> inputs = new ArrayList<>();

        Collection<TestInput[]> result = new ArrayList<>();
        testFile = new File(dir,fileName);

        inputs.add(new TestInput(0,12,new ArrayList<>(),"ciao","sono","pre",11,"suf",30, ByteBufList.ENCODER));
        for (TestInput e : inputs) {
            result.add(new TestInput[] { e });
        }
        return result;

    }

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
    public void testDoubleAndClone(){

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
    public void testGetBytesAndArrays() {
        ByteBufList bufList = ByteBufList.get(Unpooled.wrappedBuffer(this.stringPart1.getBytes()),
                Unpooled.wrappedBuffer(this.stringPart2.getBytes()));

        assertArrayEquals((this.stringPart1+this.stringPart2).getBytes(), bufList.toArray());

        bufList.prepend(Unpooled.wrappedBuffer(this.prefix.getBytes()));
        assertArrayEquals((this.prefix+this.stringPart1+this.stringPart2).getBytes(), bufList.toArray());

        for(int i = 0; i<this.numAdding; i++) {
            bufList.add(Unpooled.wrappedBuffer((this.suffix+i).getBytes()));
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

                if(i!= -1) {
                    assertEquals(buf.toString(), value);
                }

            } while (i != -1);

        } catch (IOException e) {

            e.printStackTrace();
        }




    }

    private static class TestInput {

        private Integer minCapacity;
        private Integer maxCapacity;
        private Object buf;
        private String stringPart1;
        private String stringPart2;
        private String prefix;
        private Integer numBytes;
        private String suffix;
        private Integer numAdding;
        private Encoder encoder;

        public TestInput(Integer minCapacity, Integer maxCapacity, Object buf, String stringPart1, String stringPart2, String prefix, Integer numBytes, String suffix, Integer numAdding, Encoder encoder) {
            this.minCapacity = minCapacity;
            this.maxCapacity = maxCapacity;
            this.buf = buf;
            this.stringPart1 = stringPart1;
            this.stringPart2 = stringPart2;
            this.prefix = prefix;
            this.numBytes = numBytes;
            this.suffix = suffix;
            this.numAdding = numAdding;
            this.encoder = encoder;
        }


        public Integer getMinCapacity() {
            return minCapacity;
        }

        public Integer getMaxCapacity() {
            return maxCapacity;
        }

        public Object getBuf() {
            return buf;
        }

        public String getStringPart1() {
            return stringPart1;
        }

        public String getStringPart2() {
            return stringPart2;
        }

        public String getPrefix() {
            return prefix;
        }

        public Integer getNumBytes() {
            return numBytes;
        }

        public String getSuffix() {
            return suffix;
        }

        public Integer getNumAdding() {
            return numAdding;
        }

        public Encoder getEncoder() {
            return encoder;
        }
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