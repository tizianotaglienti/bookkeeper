package org.apache.bookkeeper.bookie.storage.ldb;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith(value = Parameterized.class)
public class ReadCacheTest {
    private boolean expectedResult;
    private long ledgerIdGet;
    private long entryIdGet;
    private long ledgerIdPut;
    private long entryIdPut;
    private ByteBuf entry = Unpooled.wrappedBuffer(new byte[1024]);
    private ReadCache cache = null;

    public ReadCacheTest(boolean expectedResult, long ledgerIdGet, long entryIdGet, long ledgerIdPut, long entryIdPut) {
        this.expectedResult = expectedResult;
        this.ledgerIdGet = ledgerIdGet;
        this.entryIdGet = entryIdGet;
        this.ledgerIdPut = ledgerIdPut;
        this.entryIdPut = entryIdPut;
    }

    @Parameterized.Parameters
    public static Collection<?> getTestParameters() {
        return Arrays.asList(new Object[][]{
                {false, -1, 0, -1, 0},  // false: fail della put di -1
                {false, 1, 0, 0, 1},    // false: put(1,0) e get(0,1) (basta uno tra ledgerID e entryID diverso)
                {true, 1, -1, 1, -1},
        });
    }

    @Before
    public void setup(){
        cache = new ReadCache(UnpooledByteBufAllocator.DEFAULT, 10 * 1024);
    }

    @Test
    public void getTest() {
        boolean result;
        try {
            cache.put(ledgerIdPut, entryIdPut, entry);
            result = cache.get(ledgerIdGet, entryIdGet).equals(entry);
        }catch (Exception e){    // inserita una entry del ledger = -1
            // se put non è valida (ledgerIdPut = -1)
            // se provo a fare una get di qualcosa di cui non ho fatto la put prima
            result = false;
        }
        Assert.assertEquals(result, expectedResult);
    }

    @After
    public void tearDown() {
        cache.close();
    }
}