package org.apache.bookkeeper.bookie.storage.ldb;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith(Parameterized.class)
public class WriteCachePutTest {
    private WriteCache cache;
    private ByteBuf entry;
    private long ledgerId;
    private long entryId;
    private boolean notAvailableSegment;
    private boolean expectedResult;

    public WriteCachePutTest(boolean expectedResult, long ledgerId, long entryId, ByteBuf entry, boolean notAvailableSegment) {
        this.expectedResult = expectedResult;
        this.ledgerId = ledgerId;
        this.entryId = entryId;
        this.entry = entry;
        this.notAvailableSegment = notAvailableSegment;
    }

    @Parameterized.Parameters
    public static Collection<?> getParameter() {
        return Arrays.asList(new Object[][] {

                {false, -1, 1, UnpooledByteBufAllocator.DEFAULT.buffer(1024), true},
                {true, 0, 0,  UnpooledByteBufAllocator.DEFAULT.buffer(1024), false},
                {false, 1, -1, UnpooledByteBufAllocator.DEFAULT.buffer(11*1024), false},

                {false, 0, -1, null, false}, // NullPointerException
                {false, 0, 0, UnpooledByteBufAllocator.DEFAULT.buffer(2 * 1024 + 2), true},
                {true, 1, 1, UnpooledByteBufAllocator.DEFAULT.buffer(10 * 1024 - 1), false},
                {false, -1, 0, UnpooledByteBufAllocator.DEFAULT.buffer(11 * 1024), false}


        });
    }

    @Before
    public void setup(){
        if (!notAvailableSegment) {
            cache = new WriteCache(UnpooledByteBufAllocator.DEFAULT, 10 * 1024);
        }
        else {
            cache = new WriteCache(UnpooledByteBufAllocator.DEFAULT, 10 * 1024, 2*1024);
        }
        if(entry != null) {
            entry.writerIndex(entry.capacity());
        }
    }


    @After
    public void tearDown(){
        if (entry != null) {
            entry.release();
        }
        cache.clear();
        cache.close();
    }


    @Test
    public void putTest() {
        boolean result;
        try {
            result = cache.put(ledgerId, entryId, entry);
        } catch (Exception e) {
            //e.printStackTrace();
            result = false;
        }
        Assert.assertEquals(expectedResult, result);
    }
}