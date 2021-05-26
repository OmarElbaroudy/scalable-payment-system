import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rocksdb.RocksDBException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import persistence.RocksHandler;
import persistence.models.UTXO;
import utilities.Sign;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class RocksTester {
        private static RocksHandler db;

        @BeforeAll
        static void init(){
            try{
                db = new RocksHandler(UUID.randomUUID().toString());
            }catch (RocksDBException e){
                System.err.println(e.getMessage());
            }
        }

        @AfterAll
        static void clean(){
            db.closeHandler();
        }

        @Test
        @DisplayName("check committeeId column")
        void whenGivenKeyAndValue_thenDataIsInserted(){
            try{
                ECKeyPair pair = Keys.createEcKeyPair();
                BigInteger pubKey = pair.getPublicKey();
                String committeeId = UUID.randomUUID().toString();
                db.assignKeyToCommittee(pubKey.toString(16), committeeId);
                String newId = db.getCommitteeId(pubKey.toString(16));
                assertEquals(committeeId, newId,
                        "committeeId obtained doesn't match inserted id");
            }catch (Exception e){
                System.err.println(e.getMessage());
            }
        }

        @Test
        @DisplayName("check UTXOSet column")
        void whenGivenKeyAndUTXOSet_thenUTXOSetIsRetrievedCorrectly(){
            try{
                String msg = "Message for signing";
                byte[] msgHash = Hash.sha3(msg.getBytes());
                ECKeyPair pair = Keys.createEcKeyPair();
                Sign.SignatureData sign= Sign.signMessage(msgHash, pair);

                UTXO fst = new UTXO(5, "5", sign);
                UTXO snd = new UTXO(5, "5", sign);
                UTXO thrd = new UTXO(5, "5", sign);

                HashSet<UTXO> set = new HashSet<>(List.of(fst, snd, thrd));
                db.addUTXOSet("5", set);
                HashSet<UTXO> newSet = db.getUTXOSet("5");

                boolean flag = newSet.contains(fst) &&
                        newSet.contains(snd) &&
                        newSet.contains(thrd);

                assertTrue(flag, "set is not retrieved successfully");
            }catch (Exception e){
                System.err.println(e.getMessage());
                fail();
            }
        }
}
