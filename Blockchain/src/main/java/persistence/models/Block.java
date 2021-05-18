package persistence.models;

import utilities.MerkelTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Block {
    private MerkelTree transactions;
    private MetaData metaData;

    public Block(MetaData metaData, MerkelTree transactions) {
        this.metaData = metaData;
        this.transactions = transactions;
        metaData.setMerkleRootHash(transactions.getRoot());
    }

    public Block() {
    }


    public int getIdx() {
        return this.metaData.getBlockIndex();
    }

    public MerkelTree getTransactions() {
        return transactions;
    }

    public void setTransactions(MerkelTree transactions) {
        this.transactions = transactions;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    /**
     * @return all utxos from all transactions in this block in
     * a Hashmap where key is the public key and value is all utxos going
     * to that key
     */
    public HashMap<String, HashSet<UTXO>> getUTXOS() {
        HashMap<String, HashSet<UTXO>> ret = new HashMap<>();
        List<Transaction> transactions = this.transactions.getTransactions();

        for (Transaction t : transactions) {
            addUTXOToMap(t.getOutput(), ret);
            if (t.getReturned() != null) {
                addUTXOToMap(t.getReturned(), ret);
            }
        }
        return ret;
    }

    /**
     * @return spent transaction outputs from all transactions in this block
     * in a hashmap where key is the public key and value is all stxos that
     * went to that public key
     */
    public HashMap<String, HashSet<UTXO>> getSTXOS() {
        HashMap<String, HashSet<UTXO>> ret = new HashMap<>();
        List<Transaction> transactions = this.transactions.getTransactions();

        for (Transaction t : transactions) {
            for (UTXO utxo : t.getInput()) {
                addUTXOToMap(utxo, ret);
            }
        }
        return ret;
    }

    private void addUTXOToMap(UTXO utxo, HashMap<String, HashSet<UTXO>> mp) {
        String pubKey = utxo.getScriptPublicKey();
        HashSet<UTXO> utxos = mp.getOrDefault(pubKey, new HashSet<>());
        utxos.add(utxo);
        mp.put(pubKey, utxos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return Objects.equals(metaData, block.metaData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactions, metaData);
    }

    @Override
    public String toString() {
        return "Block{" +
                "transactions=" + transactions.toString() +
                ", metaData=" + metaData.toString() +
                '}';
    }
}
