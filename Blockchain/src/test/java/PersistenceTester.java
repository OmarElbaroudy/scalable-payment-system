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

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersistenceTester {
    private static MongoHandler handler;

    @BeforeAll
    static void init() {
        try {
            handler = new MongoHandler();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @AfterAll
    static void clean() {
        handler.closeConnection();
    }

    Block createBlock() {
        Block block = null;
        try {
            String msg = "Message for signing";
            byte[] msgHash = Hash.sha3(msg.getBytes());
            ECKeyPair pair = Keys.createEcKeyPair();

            UTXO fst = new UTXO(5, "5", Sign.signMessage(msgHash, pair));
            UTXO snd = new UTXO(5, "5");
            UTXO thrd = new UTXO(5, "5");

            List<UTXO> input = List.of(fst, snd, thrd);
            Transaction transaction = new Transaction(input, thrd);
            List<Transaction> transactions = List.of(transaction);
            MetaData data = new MetaData(1, "dhadfh", 231, 6);
            block = new Block(data, new MerkelTree(transactions));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return block;
    }

    @Test
    @DisplayName("correct insertion of blockIndex")
    void whenGivenCorrectInput_thenDataIsInserted() {
        Block block = createBlock();
        handler.saveBlock(block);
        Block b = handler.getBlock(block.getMetaData().getBlockIndex());
        assertEquals(b.getMetaData().getBlockIndex(), block.getMetaData().getBlockIndex()
                , "block index is not correct");
    }
}
