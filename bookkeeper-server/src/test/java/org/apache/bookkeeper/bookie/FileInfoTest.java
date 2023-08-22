package org.apache.bookkeeper.bookie;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class FileInfoTest {

    private static File lf;
    private byte[] masterKey;
    private int fileInfoVersion;
    private ByteBuffer[] dataTest1;
    private ByteBuffer[] dataTest2;
    private long position;
    private Integer size;
    private Boolean bestEffort;
    private File dst;
    private long sizeContent;
    private static String fileName = "writeFileInfo.log";
    private ByteBuf lac;
    private static ByteBuffer b1;
    private File temp;


    public FileInfoTest(TestInput ti) {
        this.masterKey = ti.getMasterKey();
        this.fileInfoVersion = ti.getFileInfoVersion();
        this.dataTest1= ti.getDataTest1();
        this.dataTest2= ti.getDataTest2();
        this.position = ti.getPosition();
        this.size= ti.getSize();
        this.bestEffort = ti.getBestEffort();
        this.dst = ti.getDst();
        this.sizeContent = ti.getSizeContent();
        this.lac = ti.getLac();
    }

    @BeforeClass
    public static void setUpEnvironment() {
        // Create the directories if do not exist
        if (!Files.exists(Paths.get("tmp"))) {
            File tmpDir = new File("tmp");
            tmpDir.mkdir();
        }

        // Delete test file if exists
        if (Files.exists(Paths.get("tmp", fileName))) {
            File testFile = new File("tmp", fileName);
            testFile.delete();
        }

        if (Files.exists(Paths.get("tmp","temp.log"))) {
            File testFile = new File("tmp", "temp.log");
            testFile.delete();
        }

    }

    @Before
    public void setup() throws IOException {

        lf = new File("tmp"+"/"+ fileName);
        lf.createNewFile();

        this.temp = new File("tmp"+"/"+ "temp.log");
        this.temp.createNewFile();

    }

    @Parameterized.Parameters
    public static Collection<TestInput> getTestParameters() {
        Collection<TestInput> inputs = new ArrayList<>();


        byte[] test = "Test String".getBytes(StandardCharsets.UTF_8);
        b1 = ByteBuffer.wrap(test);
        ByteBuffer[] bb = new ByteBuffer[5];
        bb[0] = b1;
        bb[1] = b1;
        bb[2] = b1;
        bb[3] = b1;
        bb[4] = b1;


        ByteBuf byB = Unpooled.buffer(0);
        byB.writeLong(0L);
        byB.writeLong(0L);
        byB.writeLong(0L);
        byB.writeLong(0L);
        byB.writeLong(0L);
        byB.writeLong(0L);
        byB.writeLong(0L);
        byB.writeLong(0L);

        inputs.add(new TestInput(byB,"master".getBytes(StandardCharsets.UTF_8),FileInfo.V1 ,bb,bb,0L,55,true,null,9223372036854775807L));
        inputs.add(new TestInput(Unpooled.buffer(0),"".getBytes(StandardCharsets.UTF_8),FileInfo.V1,new ByteBuffer[]{ByteBuffer.allocate(2)},bb,0L,2,false,null,9223372036854775807L));
        inputs.add(new TestInput(byB,"master".getBytes(StandardCharsets.UTF_8),FileInfo.V1 ,bb,bb,0L,400,false,null,0));
        inputs.add(new TestInput(byB,"master".getBytes(StandardCharsets.UTF_8),FileInfo.V1 ,bb,bb,0L,400,true,null,0));
        inputs.add(new TestInput(byB,"master".getBytes(StandardCharsets.UTF_8),FileInfo.V0 ,bb,bb,0L,55,false,new File("tmp"+"/"+ fileName),9223372036854775807L));
        inputs.add(new TestInput(byB,"master".getBytes(StandardCharsets.UTF_8),-1,bb,bb,0L,55,true,null,10));


        return inputs;

    }

    @After
    public void tearDown() throws IOException {

        // Delete test file if exists
        if (Files.exists(Paths.get("tmp"))) {
            File testFile = new File("tmp"+"/"+"temp.log");
            if(Files.exists(Paths.get("tmp"+"/"+"temp.log")) && !testFile.delete()){
                throw new IOException("tear down file cannot deleted");
            }
            File testFile2 = new File("tmp"+"/"+fileName);
            if(Files.exists(Paths.get("tmp"+"/"+fileName)) && !testFile2.delete()){
                throw new IOException("tear down file2 cannot deleted");
            }
        }
    }

    private static class TestInput {
        private byte[] masterKey;
        private int fileInfoVersion;
        private ByteBuffer[] dataTest1;
        private ByteBuffer[] dataTest2;
        private long position;
        private Integer size;
        private Boolean bestEffort;
        private File dst;
        private long sizeContent;
        private ByteBuf lac;

        public TestInput(ByteBuf lac,byte[] masterKey, int fileInfoVersion, ByteBuffer[] dataTest1,ByteBuffer[] dataTest2, long position, Integer size, Boolean bestEffort, File dst, long sizeContent) {
            this.lac = lac;
            this.masterKey = masterKey;
            this.fileInfoVersion = fileInfoVersion;
            this.dataTest1 = dataTest1;
            this.dataTest2 = dataTest2;
            this.position = position;
            this.size = size;
            this.bestEffort = bestEffort;
            this.dst = dst;
            this.sizeContent = sizeContent;
        }


        public ByteBuf getLac() {
            return lac;
        }


        public byte[] getMasterKey() {
            return masterKey;
        }

        public int getFileInfoVersion() {
            return fileInfoVersion;
        }

        public ByteBuffer[] getDataTest1() {
            return dataTest1;
        }

        public ByteBuffer[] getDataTest2() {
            return dataTest2;
        }

        public long getPosition() {
            return position;
        }

        public Integer getSize() {
            return size;
        }

        public Boolean getBestEffort() {
            return bestEffort;
        }

        public File getDst() {
            return dst;
        }

        public long getSizeContent() {
            return sizeContent;
        }
    }

    @Test
    public void testBasic() throws IOException {

        FileInfo fi = new FileInfo(lf,this.masterKey,this.fileInfoVersion);

        String masterK = new String(this.masterKey, StandardCharsets.UTF_8);
        String fiMasterK =  new String(fi.getMasterKey(), StandardCharsets.UTF_8);

        assertEquals(masterK, fiMasterK);
        assertEquals(lf,fi.getLf());

        if(!fi.isDeleted()){
            Logger.getRootLogger().setLevel(Level.DEBUG);
            if(this.lac.capacity() != 0) {
                fi.setExplicitLac(this.lac);
                assertEquals(this.lac.resetReaderIndex(), fi.getExplicitLac());
            }

        }

        ByteBuf byB = Unpooled.buffer(0);
        byB.writeLong(1L);
        byB.writeLong(1L);
        byB.writeLong(1L);
        byB.writeLong(1L);
        byB.writeLong(1L);
        byB.writeLong(1L);
        byB.writeLong(1L);
        byB.writeLong(1L);


        if(byB.capacity() != 0) {
            Logger.getRootLogger().setLevel(Level.INFO);
            fi.setExplicitLac(byB);
            assertEquals(byB.resetReaderIndex(), fi.getExplicitLac());
        }


        fi.close(true);
    }

    @Test
    public void testWriteAndRead() throws IOException {

        FileInfo fi = new FileInfo(lf,this.masterKey,this.fileInfoVersion);

        long numBytesWritten = fi.write(this.dataTest1,this.position);
        b1.position(0);


        assertTrue(numBytesWritten > 0L);
        assertTrue(fi.size() > 0 );
        assertEquals(fi.size() , numBytesWritten); //bytes - 1025L


        ByteBuffer bBuff = ByteBuffer.allocate(this.size);

        if(this.size > 55 && this.bestEffort.equals(false)){
            try {
                fi.read(bBuff, this.position, this.bestEffort);
                Assert.fail("Expected an ShortReadException to be thrown");
            }catch(ShortReadException e) {}
        }else {

            int numBytesRead = fi.read(bBuff, this.position, this.bestEffort);

            assertTrue(numBytesRead > 0);
            assertEquals(numBytesWritten, bBuff.capacity() - bBuff.remaining());

        }

        fi.close(true);

    }

    private String readFromFile(Integer numBytes,String pathFile) {

        String value = null;

        try (FileInputStream fis = new FileInputStream(pathFile)) {

            int i;

            do {

                byte[] readStr = new byte[numBytes];
                i = fis.read(readStr);

                value = new String(readStr, StandardCharsets.UTF_8);
                if(value.equals(new String(new byte[numBytes],StandardCharsets.UTF_8)))
                    i = 0;

            } while (i != 0);

        } catch (IOException e) {

            e.printStackTrace();
        }

        return value;
    }

    @Test
    public void testMoveToNewLocation() throws IOException {

        String resultNewFile;

        FileInfo fi = new FileInfo(lf,this.masterKey,this.fileInfoVersion);

        long numBytesWritten = fi.write(this.dataTest2,this.position);
        b1.position(0);

        assertTrue(numBytesWritten > 0);

        String result = readFromFile(Math.toIntExact(numBytesWritten),"tmp"+"/"+fileName);

        if(this.dst == null) {
            fi.moveToNewLocation(this.temp, this.sizeContent);
        }else{
            fi.moveToNewLocation(this.dst, this.sizeContent);
        }

        if(this.dst == null) {
            resultNewFile = readFromFile(Math.toIntExact(numBytesWritten), "tmp" + "/" + this.temp.getName());
        }else{
            resultNewFile = readFromFile(Math.toIntExact(numBytesWritten), "tmp" + "/" + this.dst.getName());
        }

        if(this.dst != null && !this.dst.getName().equals(lf.getName())) {
            //check deleted file
            assertFalse(Files.exists(Paths.get("tmp" + "/" + fileName)));
            //check if new file exists
            if(this.dst == null)
                assertTrue(Files.exists(Paths.get("tmp" + "/" + this.temp.getName())));
            else
                assertTrue(Files.exists(Paths.get("tmp" + "/" + this.dst.getName())));

            assertEquals(result, resultNewFile);
        }

    }


}