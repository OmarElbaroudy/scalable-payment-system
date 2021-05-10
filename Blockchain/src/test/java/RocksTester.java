import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import persistence.RocksHandler;

import java.math.BigInteger;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class RocksTester {
        private static RocksHandler db;

        @BeforeAll
        static void init(){
            try{
                db = new RocksHandler();
            }catch (RocksDBException e){
                System.err.println(e.getMessage());
            }
        }

        @AfterAll
        static void clean(){
            db.closeHandler();
        }

        @Test
        void whenGivenKeyAndValue_thenDataIsInserted(){
            try{
                ECKeyPair pair = Keys.createEcKeyPair();
                BigInteger pubKey = pair.getPublicKey();
                UUID committeeId = UUID.randomUUID();
                db.put(pubKey.toString(16),committeeId.toString());
                String newId = db.get(pubKey.toString(16));
                assertEquals(committeeId.toString(), newId,
                        "committeeId obtained doesn't match inserted id");
            }catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
}
