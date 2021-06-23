package services;

import org.apache.commons.codec.digest.DigestUtils;
import persistence.MongoHandler;
import persistence.RocksHandler;
import persistence.models.Block;
import persistence.models.MetaData;
import persistence.models.Transaction;
import persistence.models.UTXO;
import utilities.MerkelTree;

import java.util.*;

public class BlockServices {

    public static Block mineBlock(List<Transaction> transactions, MongoHandler handler, RocksHandler rocksHandler) {
        Block lst = getLastBlock(handler);
        String prevHash = hash(Objects.requireNonNull(lst).toString());

        int nonce = 0;
        int idx = lst.getIdx() + 1;
        int difficulty = Integer.parseInt(
                Objects.requireNonNull(System.getenv("DIFFICULTY")));

        List<Transaction> ts = revalidateTransactions(transactions, rocksHandler);

        for (boolean flag; true; nonce++) {
            MetaData data = new MetaData(idx, prevHash, nonce, difficulty);
            MerkelTree tree = new MerkelTree(ts);
            Block b = new Block(data, tree);
            String hashedValue = hash(b.toString());
            flag = check(hashedValue, difficulty);
            if(flag) return b;
        }
    }

    private static List<Transaction> revalidateTransactions(List<Transaction> ts, RocksHandler handler) {
        HashMap<String, List<Transaction>> mp = new HashMap<>();
        List<Transaction> ret = new ArrayList<>();

        TransactionServices.orderByPubKey(ts, mp);

        for(var e : mp.entrySet()){
            double amount = 0, balance = 0;
            List<Transaction> cur = e.getValue();
            HashSet<UTXO>  st = handler.getUTXOSet(e.getKey());

            for(Transaction t : cur){
                amount += t.getOutput().getAmount();
            }

            for(UTXO utxo : st){
                balance += utxo.getAmount();
            }

            while(!cur.isEmpty() && amount > balance){
                int lstIdx = cur.size() - 1;
                amount -= cur.get(lstIdx).getOutput().getAmount();

                cur.remove(lstIdx);
            }
        }

        for(var e : mp.entrySet()){
            ret.addAll(e.getValue());
        }

        return ret;
    }



    private static boolean check(String s, int diff) {
        for (int i = 0; i < diff; i++) {
            if (s.charAt(i) != '0') return false;
        }
        return true;
    }

    private static String hash(String block) {
        return new DigestUtils("SHA3-256").digestAsHex(block);
    }

    private static Block getLastBlock(MongoHandler handler) {
        int idx = 1;
        Block lst = handler.getBlock(1), b = lst;

        while (b != null) {
            lst = b;
            b = handler.getBlock(++idx);
        }

        return lst;
    }

    /**
     * @param block      that is needed to be validated and added
     * @param handler    for inserting the block in the db
     * @return list of transactions if block is validated and added successfully
     * or null if block is invalid. if a block already exists in the database
     * with the same index it will be replaced by the validated block
     */
    public static List<Transaction> validateAndAddBlock(Block block, MongoHandler handler) {
        Block lst = getLastBlock(handler);
        if (lst == null) return null; //Genesis

        int difficulty = Integer.parseInt(
                Objects.requireNonNull(System.getenv("DIFFICULTY")));

        boolean flag = lst.getIdx() == block.getIdx() - 1;
        String hashLast = hash(lst.toString());
        flag &= hashLast.equals(block.getMetaData().getPreviousBlockHash());
        flag &= difficulty == block.getMetaData().getDifficulty();

        String hashVal = hash(block.toString());
        for (int i = 0; i < difficulty; i++) {
            flag &= hashVal.charAt(i) == '0';
        }

        if (!flag) return null;
        handler.saveBlock(block);
        return block.getTransactions().getTransactions();
    }

    public static boolean blockExists(Block block, MongoHandler handler) {
        Block comp = handler.getBlock(block.getIdx());
        return comp != null && comp.equals(block);
    }

    public static Block generateGenesis(MongoHandler handler, RocksHandler rocksHandler, boolean updateRocks) {
        String prevHash = System.getenv("GENESIS_PREVIOUS_HASH");
        int nonce = Integer.parseInt(Objects.requireNonNull(System.getenv("GENESIS_NONCE")));
        String pubKey = System.getenv("GENESIS_PUBLIC_KEY");

        MetaData data = new MetaData(1, prevHash, nonce, 0);

        UTXO output = new UTXO(1000, pubKey);
        Transaction transaction = new Transaction(new ArrayList<>(), output);

        Block b = new Block(data, new MerkelTree(List.of(transaction)));
        handler.saveBlock(b);
        if (updateRocks) rocksHandler.update(b);
        return b;
    }
}
