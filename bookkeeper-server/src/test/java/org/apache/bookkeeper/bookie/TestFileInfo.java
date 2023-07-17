package org.apache.bookkeeper.bookie;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.netty.buffer.ByteBuf;

public class TestFileInfo {

    private File lf;
    private byte[] masterKey;
    private int fileInfoVersion;
    private ByteBuf lac;
    private ByteBuffer[] data;
    private Integer position;
    private Integer size;
    private Boolean bestEffort;
    private File dst;
    private Long sizeContent;
    private static String pathFile = "tmp/writeFileInfo.log";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

        // Delete test file if exists
        if (Files.exists(Paths.get(this.dst.getName()))) {
            File testFile = new File(this.dst.getName());
            testFile.delete();
        }
    }

    @Test
    public void testBasic() throws IOException {

        FileInfo fi = new FileInfo(this.lf,this.masterKey,this.fileInfoVersion);

        assertEquals(this.masterKey,fi.getMasterKey());
        assertEquals(this.lf,fi.getLf());

        doAnswer(invocation ->{
            return null; //return type void
        }).when(fi).checkOpen(any());

        doAnswer(invocation ->{
            return null;
        }).when(fi).readHeader();

        fi.setExplicitLac(this.lac);

        ByteBuf bb = fi.getExplicitLac();

        assertEquals(0,bb.compareTo(this.lac));

        fi.close(true);
    }

    @Test
    public void testWriteAndRead() throws IOException {

        FileInfo fi = new FileInfo(this.lf,this.masterKey,this.fileInfoVersion);

        Long numBytesWritten = fi.write(this.data,this.position);

        Long numBytesInData = 0L;

        for(int i = 0; i<this.data.length;i++) {

            numBytesInData += this.data[i].remaining();

        }
        assertEquals(numBytesWritten,numBytesInData);

        ByteBuffer bb = ByteBuffer.allocate(this.size);

        fi.read(bb,this.position,this.bestEffort);

        ByteBuffer mergedBb = ByteBuffer.allocate(this.size);

        for(int i = 0; i<this.data.length;i++) {

            while (data[i].hasRemaining())
                mergedBb.put(data[i].get());

        }


        assertEquals(0,mergedBb.compareTo(bb));

    }

    private String readFromFile(String path, Integer numBytes) {

        String value = null;

        try (FileInputStream fis = new FileInputStream(pathFile)) {

            int i = 0;

            do {

                byte[] readStr = new byte[numBytes];
                i = fis.read(readStr);

                value = new String(readStr, StandardCharsets.UTF_8);


            } while (i != -1);

        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return value;
    }

    @Test
    public void testMoveToNewLocation() throws IOException {

        FileInfo fi = new FileInfo(this.lf,this.masterKey,this.fileInfoVersion);

        Long numBytesWritten = fi.write(this.data,this.position); //controllare header byte

        String result = readFromFile(pathFile,numBytesWritten.intValue());

        fi.moveToNewLocation(this.dst,this.sizeContent);

        String resultNewFile = readFromFile(this.dst.getName(),numBytesWritten.intValue());

        //check deleted file
        assertTrue(!Files.exists(Paths.get(pathFile)));
        //check if new file exists
        assertTrue(Files.exists(Paths.get(this.dst.getName())));

        assertEquals(result,resultNewFile);




    }


}