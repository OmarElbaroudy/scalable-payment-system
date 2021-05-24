import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import persistence.MongoHandler;
import persistence.models.Block;
import persistence.models.MetaData;
import persistence.models.Transaction;
import persistence.models.UTXO;
import utilities.MerkelTree;
import utilities.Sign;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MongoTester {
    private static MongoHandler handler;
    private static Block block;
    private static Transaction transaction;

    @BeforeAll
    static void init() {
        handler = new MongoHandler(UUID.randomUUID().toString());
    }

    @AfterAll
    static void clean() {
        handler.closeConnection();
    }

    Block createBlock() {
        List<Transaction> transactions = List.of(createTransaction());
        MetaData data = new MetaData(1, "test", 1234, 10);
        block = new Block(data, new MerkelTree(transactions));
        return block;
    }

    Transaction createTransaction() {
        transaction = null;
        try {
            String msg = "Message for signing";
            byte[] msgHash = Hash.sha3(msg.getBytes());
            ECKeyPair pair = Keys.createEcKeyPair();
            Sign.SignatureData sign = Sign.signMessage(msgHash, pair);

            UTXO fst = new UTXO(5, "10", sign);
            UTXO snd = new UTXO(5, "10", sign);
            UTXO thrd = new UTXO(5, "10", sign);

            List<UTXO> input = List.of(fst, snd, thrd);
            transaction = new Transaction(input, thrd);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return transaction;
    }

    @Test
    @DisplayName("correct insertion of block Header")
    void whenGivenCorrectInput_thenMetaDataIsInserted() {
        Block block = createBlock();
        handler.saveBlock(block);
        Block b = handler.getBlock(block.getIdx());
        assertEquals(b.getMetaData(), block.getMetaData()
                , "block header is not correct");
    }


    @Test
    @DisplayName("simple payment verification protocol")
    void whenGivenValidTransaction_thenSPVPReturnsTrue() {
        Block b = handler.getBlock(block.getIdx());
        boolean exist = b.getTransactions().SPV(transaction);
        assertTrue(exist, "spv is not working correctly");
    }

    @Test
    @DisplayName("simple payment verification protocol")
    void whenGivenInvalidTransaction_thenSPVRReturnsFalse() {
        Block b = handler.getBlock(block.getIdx());
        boolean exist = b.getTransactions().SPV(new Transaction());
        assertFalse(exist, "spv is not working correctly");
    }

    @Test
    @DisplayName("deletion of block")
    void whenGivenValidBlockIndex_thenBlockIsDeleted() {
        handler.deleteBlock(block.getIdx());
        Block b = handler.getBlock(block.getIdx());
        assertNull(b, "block is not deleted");
    }
}
