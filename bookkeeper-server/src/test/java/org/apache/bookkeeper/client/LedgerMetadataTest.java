package org.apache.bookkeeper.client;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.bookkeeper.client.api.DigestType;
import org.apache.bookkeeper.client.api.LedgerMetadata;
import org.apache.bookkeeper.net.BookieSocketAddress;
import org.junit.Assert;
import org.junit.runners.Parameterized;
import org.apache.bookkeeper.net.BookieId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.experimental.runners.Enclosed;


@RunWith(Enclosed.class)
public class LedgerMetadataTest {

    @RunWith(Parameterized.class)
    public static class buildTest {
        public static final DigestType MAC = DigestType.MAC;
        public static final DigestType DUMMY = DigestType.DUMMY;
        public static final DigestType CRC_32_C = DigestType.CRC32C;
        public static final DigestType CRC_32 = DigestType.CRC32;
        private static List<BookieId> ensemble;

        private final DigestType digestType;
        private final byte[] passwd;
        private final Long ledgerId;
        private final Integer ensembleSize;
        private final Integer writequorumSize;
        private final Integer ackQuorumSize;
        private final Integer version;
        private final Long ctime;
        private final Long ctoken;
        private final Map<String, byte[]> customMetadata;
        private final Long lastEntryId;
        private final Long firstEntryId;
        private final Boolean storeCtime;
        private final Long lenght;
        private final Boolean closed;
        private final Object res;

        public buildTest(DigestType digestType, byte[] passwd, Long ledgerId, Integer ensembleSize, Integer writequorumSize, Integer ackQuorumSize, Integer version, Long ctime, Long ctoken, Map<String, byte[]> customMetadata, Long lastEntryId, Long firstEntryId, Boolean storeCtime, Long lenght, Boolean closed, Object res) {
            this.digestType = digestType;
            this.passwd = passwd;
            this.ledgerId = ledgerId;
            this.ensembleSize = ensembleSize;
            this.writequorumSize = writequorumSize;
            this.ackQuorumSize = ackQuorumSize;
            this.version = version;
            this.ctime = ctime;
            this.ctoken = ctoken;
            this.customMetadata = customMetadata;
            this.lastEntryId = lastEntryId;
            this.firstEntryId = firstEntryId;
            this.storeCtime = storeCtime;
            this.lenght = lenght;
            this.closed = closed;
            this.res = res;
        }
/*
 *  digestType: {CRC32},{MAC},{CRC32C},{DUMMY},{null}
 *  passwd: {byte[]}, {null}
    ledgerId:  {>0}, {=0}
    ensembleSize: {>0}, {=0}
    WritequorumSize: {=ensembleSize},{<ensembleSize}
    AckQuorumSize : {=WriteQuosumSize},{<WriteQuorumSize}
    version : {>=1} , {<=10}
    ctime :{=0},{>0}
    ctoken : {=0},{>0}
    customMetadata : {Map<String,byte[]>}, {Collection.emptyMap}
    lastEntryId : {=firstEntryId}, {>firstEntryId}
    firstEntryId : {<0},{=0},{>0}
    storeCtime : {true}, {false}
    lenght: {=0},{>0}
*/

        @Parameterized.Parameters(name = "TestLedgerMetadataTest: {0},{1},{2},{3},{4},{5},{6},{7},{8}")
        public static Collection data() {
            return java.util.Arrays.asList(new Object[][]{
                    {MAC, "psw".getBytes(), 0L, 3, 2, 1, 1, 1L, 1L, Collections.emptyMap(), 1L, 0L, true, 0L,true,null},
                    //adequacy
                    {null, null, 0L, 1, 1, 1, 1, 1L, 1L, Collections.emptyMap(), 1L, 0L, true, 0L,true,null},
                    {MAC, "psw".getBytes(), 0L, 3, 2, 1, 1, 1L, 1L, generateRandomMap(5), 0L, 0L, true, 0L,false,null},
                    {DUMMY, "".getBytes(), 125L, 1, -1, 1, 1, 1L, 1L, Collections.emptyMap(), 1L, 0L, false, 0L,true,IllegalArgumentException.class}, //Write quorum must be greater or equal to ack quorum
                    {CRC_32, "".getBytes(), 1L, 1, 2, 2, 1, 1L, 1L, Collections.emptyMap(), 1L, -1L, false, 0L,true,IllegalArgumentException.class}, //Write quorum must be less or equal to ensemble size
                    {CRC_32_C, "".getBytes(), null, 1, 1, 1, 1, 1L, 1L, Collections.emptyMap(), 0L, 0L, false, 0L,true,IllegalArgumentException.class} ,//ledger id must be set
                    {CRC_32_C, "".getBytes(), 0L, 1, 1, 1, 1, 1L, 1L, Collections.emptyMap(), null, null, false, 0L,false,IllegalArgumentException.class} ,//There must be at least one ensemble in the ledger
            });
        }
        @Test
        public void testBuildBasic() {
            try {
                LedgerMetadataBuilder builder = LedgerMetadataBuilder.create();
                if(closed) {
                    builder.withClosedState();
                    builder.withLength(lenght);
                    if(lastEntryId!=null)
                        builder.withLastEntryId(lastEntryId);
                }if(!closed) {
                    builder.withInRecoveryState();
                }
                if(ledgerId!=null) {
                    builder.withId(ledgerId);
                }if(passwd!=null) {
                    builder.withDigestType(digestType);
                    builder.withPassword(passwd);
                }if(ctime!=null) {
                    builder.withCreationTime(ctime);
                }if(ensembleSize!=null) {
                    builder.withEnsembleSize(ensembleSize);
                    ensemble = new ArrayList<>();
                    for (int i = 0; i < ensembleSize; i++) {
                        ensemble.add(new BookieSocketAddress("192.0.2.1", 1234).toBookieId());
                    }
                }if(writequorumSize!=null) {
                    builder.withWriteQuorumSize(writequorumSize);
                }if(ackQuorumSize!=null) {
                    builder.withAckQuorumSize(ackQuorumSize);
                }if(version!=null) {
                    builder.withMetadataFormatVersion(version);
                }if(customMetadata!=null) {
                    builder.withCustomMetadata(customMetadata);
                }if(ctoken!=null) {
                    builder.withCToken(ctoken);
                }if(storeCtime!=null) {
                    builder.storingCreationTime(storeCtime);
                }if(firstEntryId!=null) {
                    builder.newEnsembleEntry(firstEntryId,ensemble);
                }
                LedgerMetadata metadata = builder.build();
                assertEquals( (long)ledgerId, metadata.getLedgerId());
                assertEquals( (long)ensembleSize, metadata.getEnsembleSize());
                assertEquals( (int) writequorumSize, metadata.getWriteQuorumSize());
                assertEquals( (int) ackQuorumSize, metadata.getAckQuorumSize());
                if (passwd != null) {
                    assertEquals(digestType, metadata.getDigestType());
                }
                assertEquals( (long) (ctime), metadata.getCtime());
                if(closed) {
                    assertEquals((long) (lastEntryId), metadata.getLastEntryId());
                    assertEquals((long) (lenght), metadata.getLength());
                    assertTrue(metadata.isClosed()); }
                assertEquals(1, metadata.getAllEnsembles().size());
                assertEquals((long)(lenght), metadata.getLength());
                assertEquals((long)(ctoken), metadata.getCToken());
                assertEquals(ensemble, metadata.getAllEnsembles().get(firstEntryId));
                assertEquals(ensemble, metadata.getEnsembleAt(firstEntryId));
                assertEquals(customMetadata, metadata.getCustomMetadata());
                assertEquals((long)(version), metadata.getMetadataFormatVersion());
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(),res);
            }
        }
    }


    @RunWith(Parameterized.class)
    public static class fromOtherTest {

        private final LedgerMetadata other;
        public fromOtherTest(LedgerMetadata other) {
            this.other = other;
        }

        @Parameterized.Parameters(name = "TestLedgerMetadataTest: {0},{1},{2},{3},{4},{5},{6},{7},{8}")
        public static Collection data() {
            return java.util.Arrays.asList(new Object[][]{
                    {LedgerMetadataBuilder.create().withId(1L).withEnsembleSize(1).newEnsembleEntry(1L,Arrays.asList(new BookieSocketAddress("192.0.2.1", 1234).toBookieId())).withWriteQuorumSize(1).withAckQuorumSize(1).build()},
                    //adequacy
                    {LedgerMetadataBuilder.create().withId(1L).withLength(3).withEnsembleSize(1).newEnsembleEntry(1L,Arrays.asList(new BookieSocketAddress("192.0.2.1", 1234).toBookieId())).withWriteQuorumSize(1).withClosedState().withLastEntryId(1).withAckQuorumSize(1).build()},
                    {LedgerMetadataBuilder.create().withId(1L).withLength(3).withEnsembleSize(1).newEnsembleEntry(1L,Arrays.asList(new BookieSocketAddress("192.0.2.1", 1234).toBookieId())).withWriteQuorumSize(1).withClosedState().withLastEntryId(1).withAckQuorumSize(1).withDigestType(DigestType.CRC32).withPassword("".getBytes(StandardCharsets.UTF_8)).build()},
            });
        }

        @Test
        public void testFromLedgerMetadata() {
            LedgerMetadataBuilder metadata = LedgerMetadataBuilder.from(other);
            Assert.assertEquals(metadata.build().toString(),other.toString());
        }
    }


    @RunWith(Parameterized.class)
    public static class newEnsembleEntryTest {
        private static List<BookieId> ensemble;
        private static List<BookieId> ensemble_new;

        private final Long lastEntryId;
        private final Long firstEntryId;
        private final Long newEnsembleEntry;
        private final Integer ensemble_size;
        private final Integer new_ensemble_size;
        private final Object res;

        public newEnsembleEntryTest(Long lastEntryId, Long firstEntryId, Long newEnsembleEntry, Integer ensemble_size, Integer new_ensemble_size, Object res) {
            this.lastEntryId = lastEntryId;
            this.firstEntryId = firstEntryId;
            this.newEnsembleEntry = newEnsembleEntry;
            this.ensemble_size = ensemble_size;
            this.new_ensemble_size = new_ensemble_size;
            this.res = res;
        }

        @Parameterized.Parameters(name = "TestLedgerMetadataTest: {0},{1},{2},{3}")
        public static Collection data() {
            return java.util.Arrays.asList(new Object[][]{
                    {1L, 3L, 5L, 3, 3, null},
                    //adequacy
                    {1L, 3L, 3L, 3, 3, IllegalArgumentException.class},//New entry must have a first entry greater than any existing ensemble key
                    {1L, 3L, 4L, 2, 1, IllegalArgumentException.class}//Size of passed in ensemble must match the ensembleSize of the builder
            });
        }

        @Test
        public void test() {
            try {
                if (ensemble_size != null) {
                    ensemble = new ArrayList<>();
                    for (int i = 0; i < ensemble_size; i++) {
                        ensemble.add(new BookieSocketAddress("192.0.2.1", 1234).toBookieId());
                    }
                    ensemble_new = new ArrayList<>();
                    for (int i = 0; i < new_ensemble_size; i++) {
                        ensemble_new.add(new BookieSocketAddress("192.0.2.1", 1234).toBookieId());
                    }
                }
                LedgerMetadataBuilder builder = LedgerMetadataBuilder.create().withId(0L);
                if (ensemble_size != null) {
                    builder.withEnsembleSize(ensemble_size);
                }
                builder.withLastEntryId(lastEntryId).newEnsembleEntry(firstEntryId, ensemble).newEnsembleEntry(newEnsembleEntry, ensemble_new);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(), res);
            }
        }
    }
    @RunWith(Parameterized.class)
    public static class replaceEnsembleEntryTest {
        private static List<BookieId> ensemble;
        private static List<BookieId> ensemble_new;

        private final Long lastEntryId;
        private final Long firstEntryId;
        private final Long newEnsembleEntry;
        private final Integer ensemble_size;
        private final Integer new_ensemble_size;
        private final Object res;

        public replaceEnsembleEntryTest(Long lastEntryId, Long firstEntryId, Long newEnsembleEntry, Integer ensemble_size, Integer new_ensemble_size, Object res) {
            this.lastEntryId = lastEntryId;
            this.firstEntryId = firstEntryId;
            this.newEnsembleEntry = newEnsembleEntry;
            this.ensemble_size = ensemble_size;
            this.new_ensemble_size = new_ensemble_size;
            this.res = res;
        }

        @Parameterized.Parameters(name = "TestLedgerMetadataTest: {0},{1},{2},{3}")
        public static Collection data() {
            return java.util.Arrays.asList(new Object[][]{
                    {1L,3L,3L,3,3,null},
                    //adequacy
                    {1L,3L,2L,3,3,IllegalArgumentException.class},//Ensemble must replace an existing ensemble in the ensemble map
                    {1L, 3L, 4L, 2, 1, IllegalArgumentException.class}//Size of passed in ensemble must match the ensembleSize of the builder
            });
        }

        @Test
        public void test() {
            try {
                if (ensemble_size != null) {
                    ensemble = new ArrayList<>();
                    for (int i = 0; i < ensemble_size; i++) {
                        ensemble.add(new BookieSocketAddress("192.0.2.1", 1234).toBookieId());
                    }
                    ensemble_new = new ArrayList<>();
                    for (int i = 0; i < new_ensemble_size; i++) {
                        ensemble_new.add(new BookieSocketAddress("192.0.2.1", 1234).toBookieId());
                    }
                }
                LedgerMetadataBuilder builder = LedgerMetadataBuilder.create().withId(0L);
                if (ensemble_size != null) {
                    builder.withEnsembleSize(ensemble_size);
                }
                builder.withLastEntryId(lastEntryId).newEnsembleEntry(firstEntryId,ensemble).replaceEnsembleEntry(newEnsembleEntry, ensemble_new);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(), res);
            }
        }

    }

    @RunWith(Parameterized.class)
    public static class withEnsembleSizeTest {

        private final Integer size;

        public withEnsembleSizeTest(Integer size) {
            this.size = size;
        }

        @Parameterized.Parameters(name = "TestLedgerMetadataTest: {0},{1}")
        public static Collection data() {
            return java.util.Arrays.asList(new Object[][]{
                    {0},
                    {-1},
            });
        }

        @Test  //adequacy
        public void testException() {
            try {
                LedgerMetadataBuilder builder = LedgerMetadataBuilder.create().withEnsembleSize(0).newEnsembleEntry(0, new ArrayList<BookieId>()).withEnsembleSize(size);
            }catch(Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(),IllegalStateException.class);
            }
        }
        @Test
        public void testBasic() {
            try {
                LedgerMetadataBuilder builder = LedgerMetadataBuilder.create().withEnsembleSize(size);
            }catch(Exception e) {
                e.printStackTrace();
                Assert.assertEquals(e.getClass(),null);
            }
        }
    }
    private static Map<String,byte[]> generateRandomMap(int size) {
        Map<String,byte[]> map = new HashMap<String,byte[]>();
        for(int i=0;i<size;i++) {
            String key = "key"+i;
            map.put(key,key.getBytes());
        }
        return map;
    }
}