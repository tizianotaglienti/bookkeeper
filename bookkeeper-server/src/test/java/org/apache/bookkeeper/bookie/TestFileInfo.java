package org.apache.bookkeeper.bookie;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestFileInfo {

    private static File lf;
    private byte[] masterKey; //valid, not valid,"",null
    private int fileInfoVersion;// V1,V0
    private ByteBuffer[] dataTest1;//null,valid,""
    private ByteBuffer[] dataTest2;
    private long position;//<0,=0,>0
    private Integer size;//<0,=0,>0
    private Boolean bestEffort;//true,false
    private File dst;//valid, not valid
    private long sizeContent;//<0,=0,>0
    private static String fileName = "writeFileInfo.log";
    private static int lengthByteBuff;

    public TestFileInfo(TestInput ti) {
        this.masterKey = ti.getMasterKey();
        this.fileInfoVersion = ti.getFileInfoVersion();
        this.dataTest1= ti.getDataTest1();
        this.dataTest2= ti.getDataTest2();
        this.position = ti.getPosition();
        this.size= ti.getSize();
        this.bestEffort = ti.getBestEffort();
        this.dst = ti.getDst();
        this.sizeContent = ti.getSizeContent();
    }

    @BeforeClass
    public static void setUp() throws IOException {
        // Create the directories if do not exist
        if (!Files.exists(Paths.get("tmp"))) {
            File tmpDir = new File("tmp");
            tmpDir.mkdir();
        }

        lf = new File("tmp"+"/"+ fileName);
        lf.createNewFile();

    }

    @Parameterized.Parameters
    public static Collection<TestInput[]> getTestParameters() throws IOException {
        Collection<TestInput> inputs = new ArrayList<>();

        Collection<TestInput[]> result = new ArrayList<>();


        //FileInfo fi = new FileInfo(lf, this.masterKey,1);
        byte[] test = "Hello Worldhsjoeondnskkekooekndnbhieijdnsnjooaebrcjsosoeknd".getBytes(StandardCharsets.UTF_8);
        ByteBuffer b1 = ByteBuffer.wrap(test);
        ByteBuffer[] bb = new ByteBuffer[5];
        bb[0] = b1;
        bb[1] = b1;
        bb[2] = b1;
        bb[3] = b1;
        bb[4] = b1;

        File temp = new File("tmp"+"/"+ "temp.log");
        temp.createNewFile();
        inputs.add(new TestInput("master".getBytes(StandardCharsets.UTF_8),FileInfo.CURRENT_HEADER_VERSION ,bb,bb,1L,20,false,temp,9223372036854775807L));
        inputs.add(new TestInput("".getBytes(StandardCharsets.UTF_8),FileInfo.CURRENT_HEADER_VERSION,bb,bb,1L,20,false,temp,9223372036854775807L));
        //inputs.add(new TestInput(null,FileInfo.CURRENT_HEADER_VERSION,bb,bb,1L,20,false,temp,Long.MAX_VALUE));
        //inputs.add(new TestInput("master".getBytes(StandardCharsets.UTF_8),3,bb,bb,0L,20,true,temp,Long.MAX_VALUE));
        //inputs.add(new TestInput(null,FileInfo.CURRENT_HEADER_VERSION,bb,bb,1L,20,false,temp,Long.MAX_VALUE));

        for (TestInput e : inputs) {
            result.add(new TestInput[] { e });
        }
        return result;

    }

    @AfterClass
    public static void tearDown() {

        // Delete test file if exists
        if (Files.exists(Paths.get("tmp"))) {
            File testFile = new File("tmp",fileName);
            testFile.delete();
            File testFile2 = new File("tmp","temp.log");
            testFile2.delete();
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

        public TestInput(byte[] masterKey, int fileInfoVersion, ByteBuffer[] dataTest1,ByteBuffer[] dataTest2, long position, Integer size, Boolean bestEffort, File dst, long sizeContent) {
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

        fi.close(true);
    }

    @Test
    public void testWriteAndRead() throws IOException {
        long bytes = 0;
        FileInfo fi = new FileInfo(lf,this.masterKey,this.fileInfoVersion);

        long numBytesWritten = fi.write(this.dataTest1,this.position);

        if (lf.exists()) {
            // size of a file (in bytes)
            bytes = lf.length();
        }

        if(numBytesWritten != 0L  && (bytes - 1025) > 0) {
            assertEquals(bytes - 1025L, numBytesWritten);
        }


        ByteBuffer bBuff = ByteBuffer.allocate(this.size);

        if(numBytesWritten !=0L) {
            fi.read(bBuff, this.position, this.bestEffort);

            ByteBuffer mergedBb = ByteBuffer.allocate(this.size);

            for (int i = 0; i < this.dataTest1.length; i++) {

                while (this.dataTest1[i].hasRemaining())
                    mergedBb.put(this.dataTest1[i].get());

            }
            assertEquals(20, mergedBb.compareTo(bBuff));

            fi.close(true);
        }

    }

    private String readFromFile(Integer numBytes,String pathFile) {

        String value = null;

        try (FileInputStream fis = new FileInputStream(pathFile)) { //"tmp"+"/"+fileName

            int i = 0;

            do {

                byte[] readStr = new byte[numBytes];
                i = fis.read(readStr);

                value = new String(readStr, StandardCharsets.UTF_8);


            } while (i != 0);

        } catch (IOException e) {

            e.printStackTrace();
        }

        return value;
    }

    @Test
    public void testMoveToNewLocation() throws IOException {

        FileInfo fi = new FileInfo(lf,this.masterKey,this.fileInfoVersion);

        long numBytesWritten = fi.write(this.dataTest2,this.position);

        String result = readFromFile(Math.toIntExact(numBytesWritten),"tmp"+"/"+fileName);

        fi.moveToNewLocation(this.dst,this.sizeContent);

        String resultNewFile = readFromFile(Math.toIntExact(numBytesWritten),"tmp"+"/"+ "temp.log");

        //check deleted file
        assertTrue(!Files.exists(Paths.get("tmp"+"/"+fileName)));
        //check if new file exists
        assertTrue(Files.exists(Paths.get("tmp"+"/"+this.dst.getName())));

        assertEquals(result,resultNewFile);

    }


}