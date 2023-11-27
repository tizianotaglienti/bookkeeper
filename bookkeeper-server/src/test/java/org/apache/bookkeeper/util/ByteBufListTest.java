package org.apache.bookkeeper.util;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.bookkeeper.util.ByteBufList.Encoder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(Parameterized.class)
public class ByteBufListTest {

    private static String dir = "tmpWrite";
    private static String fileName = "writeMockTest.log";
    private Object buf;
    private String stringPart1;
    private String stringPart2;
    private String prefix;
    private Integer numBytes;

    private Encoder encoder;
    private static File testFile;

    public ByteBufListTest(TestInput ti){
        this.buf = ti.getBuf();
        this.stringPart1 = ti.getStringPart1();
        this.stringPart2 = ti.getStringPart2();
        this.prefix = ti.getPrefix();
        this.numBytes = ti.getNumBytes();
        this.encoder = ti.getEncoder();
    }


    @Parameterized.Parameters
    public static Collection<TestInput> getTestParameters() {
        Collection<TestInput> inputs = new ArrayList<>();

/*
        // Caso 1: buf è una stringa di test (non null), stringhe con lunghezza > 0
        inputs.add(new TestInput("testString", "test", "part", "pre", 11, ByteBufList.ENCODER));
// Caso 1: buf è una stringa di test (non null), stringPart1 contiene "test" e stringPart2 è null
        inputs.add(new TestInput("testString", "test", null, "pre", 11, ByteBufList.ENCODER));

        // Caso 2: buf è un oggetto ByteBufList (non null), stringhe con lunghezza > 0
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())), "test", "part", "pre", 1, ByteBufList.ENCODER_WITH_SIZE));
// Caso 2: buf è un oggetto ByteBufList (non null), stringPart1 è null e stringPart2 contiene "part"
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())), null, "part", "pre", 1, ByteBufList.ENCODER_WITH_SIZE));

        // Caso 3: buf è un oggetto ByteBufList (non null), stringhe con lunghezza > 0, numBytes > 0
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testStringPart1".getBytes()), Unpooled.wrappedBuffer("testStringPart2".getBytes())), "test", "part", "pre", 1, ByteBufList.ENCODER));
// Caso 3: buf è un oggetto ByteBufList (non null), stringPart1 è null e stringPart2 è null
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testStringPart1".getBytes()), Unpooled.wrappedBuffer("testStringPart2".getBytes())), null, null, "pre", 1, ByteBufList.ENCODER));

        // Caso 4: buf è null, stringhe con lunghezza > 0, numBytes > 0
        inputs.add(new TestInput(null, "test", "part", "pre", 1, ByteBufList.ENCODER));
// Caso 4: buf è null, stringPart1 è null e stringPart2 è "test"
        inputs.add(new TestInput(null, null, "test", "pre", 1, ByteBufList.ENCODER));

        // Caso 5: buf è una stringa di test (non null), stringhe con lunghezza = 0, numBytes > 0
        inputs.add(new TestInput("emptyString", "", "", "", 5, ByteBufList.ENCODER));
// Caso 5: buf è una stringa di test (non null), stringPart1 è null e stringPart2 è "part"
        inputs.add(new TestInput("emptyString", null, "part", "pre", 5, ByteBufList.ENCODER));

        // Caso 6: buf è un oggetto ByteBufList (non null), stringhe con lunghezza > 0, numBytes = 0
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())), "test", "part", "pre", 0, ByteBufList.ENCODER_WITH_SIZE));
// Caso 6: buf è un oggetto ByteBufList (non null), stringPart1 contiene "test" e stringPart2 è null, numBytes è 0
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())), "test", null, "pre", 0, ByteBufList.ENCODER_WITH_SIZE));
*/


        inputs.add(new TestInput("testString","test","part","pre",11, ByteBufList.ENCODER));
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())),"","","",1, ByteBufList.ENCODER));
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())),"","","",0, ByteBufList.ENCODER));
        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testStringPart1".getBytes()),Unpooled.wrappedBuffer("testStringPart2".getBytes())),"","","",0, ByteBufList.ENCODER));

//        inputs.add(new TestInput("testString","test","part","pre",11));
//        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())),"","","",1));
//        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())),"","","",0));
//        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testStringPart1".getBytes()),Unpooled.wrappedBuffer("testStringPart2".getBytes())),"","","",0));


        return inputs;
    }

//    @Parameterized.Parameters
//    public static Collection<TestInput> getTestParameters() {
//        Collection<TestInput> inputs = new ArrayList<>();
//        inputs.add(new TestInput("testString","test","part","pre",11, ByteBufList.ENCODER));
//        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())),"","","",1, ByteBufList.ENCODER_WITH_SIZE));
//        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testString".getBytes())),"","","",0, ByteBufList.ENCODER));
//        inputs.add(new TestInput(ByteBufList.get(Unpooled.wrappedBuffer("testStringPart1".getBytes()),Unpooled.wrappedBuffer("testStringPart2".getBytes())),"","","",0, ByteBufList.ENCODER));
//        //inputs.add(new TestInput(ByteBufList.get(null),"","","",0, ByteBufList.ENCODER));
//        return inputs;
//
//    }

    @BeforeClass
    public static void setupEnvironment() throws IOException {
        // Create the directories if do not exist
        if (!Files.exists(Paths.get(dir))) {
            File tmpDir = new File(dir);
            tmpDir.mkdir();
        }

        // Delete test file if exists
        if (Files.exists(Paths.get(dir, fileName))) {
            File testFile = new File(dir, fileName);
            testFile.delete();
        }

    }

    @Before
    public void setUp() throws IOException {

        testFile = new File(dir,fileName);
        testFile.createNewFile();
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
    public void testGetBytesAndArrays() {
        ByteBufList bufList = ByteBufList.get(Unpooled.wrappedBuffer(this.stringPart1.getBytes()),
                Unpooled.wrappedBuffer(this.stringPart2.getBytes()));


        assertArrayEquals((this.stringPart1+this.stringPart2).getBytes(), bufList.toArray());

        bufList.prepend(Unpooled.wrappedBuffer(this.prefix.getBytes()));
        assertArrayEquals((this.prefix+this.stringPart1+this.stringPart2).getBytes(), bufList.toArray());

        byte[] buffBytes = new byte[this.numBytes];
        int res = bufList.getBytes(buffBytes);

        assertEquals((this.prefix+this.stringPart1+this.stringPart2).length(), res);

    }


    @Test
    public void testEncoderWithWrite() throws Exception {
        String value;

        int numPrevWrite = 0;

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.alloc()).thenAnswer(invocation -> PooledByteBufAllocator.DEFAULT);
        when(ctx.write(any(),any())).thenAnswer(invocation -> {
            try (FileOutputStream fos = new FileOutputStream(dir + "/" +fileName)) {
                byte[] byteStr = invocation.getArgument(0).toString().getBytes();
                fos.write(byteStr);
            } catch (IOException e) {

                e.printStackTrace();
            }
            return null;
        });

        if(this.buf instanceof ByteBufList) {
            ByteBufList bbl = (ByteBufList) this.buf;
            numPrevWrite = bbl.readableBytes();
        }
        this.encoder.write(ctx, this.buf, null);

        if(this.buf instanceof ByteBufList) {
            ByteBufList bblAfter = (ByteBufList) this.buf;
            int numWriteAfter = bblAfter.readableBytes();

            assertTrue(numPrevWrite > numWriteAfter);
        }


        try (FileInputStream fis = new FileInputStream(dir + "/" +fileName)) {
            byte[] byteStr = this.buf.toString().getBytes();
            int i;

            do {

                byte[] readStr = new byte[byteStr.length];
                i = fis.read(readStr);



                value = new String(readStr, StandardCharsets.UTF_8);

                if(i!= -1 && this.buf instanceof ByteBufList) {
                    assertNotNull(value);


                }else if (i != -1){
                    assertEquals(buf.toString(), value);
                }

            } while (i != -1);

        } catch (IOException e) {

            e.printStackTrace();
        }




    }

    private static class TestInput {

        private Object buf;
        private String stringPart1;
        private String stringPart2;
        private String prefix;
        private Integer numBytes;
        private Encoder encoder;

        public TestInput(Object buf, String stringPart1, String stringPart2, String prefix, Integer numBytes, Encoder encoder) {
            this.buf = buf;
            this.stringPart1 = stringPart1;
            this.stringPart2 = stringPart2;
            this.prefix = prefix;
            this.numBytes = numBytes;
            this.encoder = encoder;
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

        public Encoder getEncoder() {
            return encoder;
        }
    }

}