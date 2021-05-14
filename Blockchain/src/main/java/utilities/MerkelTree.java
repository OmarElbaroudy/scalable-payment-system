package utilities;

import org.apache.commons.codec.digest.DigestUtils;
import persistence.models.Transaction;

import java.util.Arrays;
import java.util.List;

public class MerkelTree {
    private int N;
    private List<Transaction> transactions;
    private List<String> tree;

    public MerkelTree(List<Transaction> transactions) {
        this.transactions = transactions;
        N = transactions.size();
        tree = Arrays.asList(new String[N << 2]);
        build(1, 1, N);
    }

    public MerkelTree() {
    }

    private String hash(String transaction) {
        return new DigestUtils("SHA3-256").digestAsHex(transaction);
    }

    private void build(int node, int b, int e) {
        if (b == e) {
            String val = hash(transactions.get(b - 1).toString());
            tree.set(node, val);
            return;
        }
        int mid = (b + e) >> 1;
        build(node << 1, b, mid);
        build(node << 1 | 1, mid + 1, e);
        tree.set(node, tree.get(node << 1) + tree.get(node << 1 | 1));
    }

    public String getRoot() {
        return tree.get(1);
    }

    public boolean isEmpty() {
        return N == 0;
    }

    private String query(int node, int b, int e, String transaction) {
        if (b == e && tree.get(node).equals(transaction))
            return "%";

        if (!tree.get(node).contains(transaction))
            return tree.get(node);

        int mid = (b + e) >> 1;
        String le = query(node << 1, b, mid, transaction);
        String ri = query(node << 1 | 1, mid + 1, e, transaction);
        return le + ri;
    }

    public boolean SPV(Transaction transaction) {
        String trans = hash(transaction.toString());
        String ret = query(1, 1, N, trans);
        if (!ret.contains("%")) return false;
        ret = ret.replaceAll("%", trans);
        return ret.equals(getRoot());
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<String> getTree() {
        return tree;
    }

    public void setTree(List<String> tree) {
        this.tree = tree;
    }
}
