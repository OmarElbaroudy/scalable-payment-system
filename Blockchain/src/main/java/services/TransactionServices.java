package services;

import org.web3j.crypto.ECKeyPair;
import persistence.MongoHandler;
import persistence.RocksHandler;
import persistence.models.Block;
import persistence.models.Transaction;
import persistence.models.UTXO;
import utilities.Sign;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TransactionServices {

    private static Transaction createTransaction
            (String privKey, String recKey, int amount, RocksHandler handler) {
        BigInteger key = new BigInteger(privKey, 16);
        ECKeyPair pair = ECKeyPair.create(key);

        int balance = 0;
        List<UTXO> input = new ArrayList<>();
        String pubKey = pair.getPublicKey().toString(16);
        HashSet<UTXO> st = handler.getUTXOSet(pubKey);

        for (UTXO utxo : st) {
            balance += utxo.getAmount();
            byte[] msg = utxo.toString().getBytes();
            Sign.SignatureData signature = Sign.signMessage(msg, pair);

            UTXO signed = new UTXO(utxo, signature);
            input.add(signed);
        }

        if (balance < amount) return null;

        List<UTXO> output = new ArrayList<>(List.of(new UTXO(amount, recKey)));
        if (balance == amount) {
            return new Transaction(input, output);
        }

        int rem = balance - amount;
        UTXO returned = new UTXO(rem, pair.getPublicKey().toString(16));
        return new Transaction(input, output, returned);
    }

    private static Transaction updateTransactionCreation(Transaction t, String recKey, int amount, RocksHandler handler) {
        int sumIn = 0, sumOut = 0;

        for (UTXO utxo : t.getInput()) {
            sumIn += utxo.getAmount();
        }

        for (UTXO utxo : t.getOutput()) {
            sumOut += utxo.getAmount();
        }

        if (sumIn < sumOut + amount) {
            return null;
        }

        sumOut += amount;
        t.getOutput().add(new UTXO(amount, recKey));

        int ret = sumIn - sumOut;
        String senderKey = t.getInputPubKey();
        t.setReturned(sumOut == sumIn ? null : new UTXO(ret, senderKey));

        return t;
    }

    public static Transaction handleTransactionCreation(List<Transaction> ts, String privKey, String recKey, int amount, RocksHandler handler) {
        BigInteger key = new BigInteger(privKey, 16);
        String pubKey = Sign.publicKeyFromPrivate(key).toString(16);

        for (Transaction t : ts) {
            if (t.getInputPubKey().equals(pubKey)) {
                return updateTransactionCreation(t, recKey, amount, handler);
            }
        }

        Transaction t = createTransaction(privKey, recKey, amount, handler);

        if (t != null) {
            ts.add(t);
            return t;
        }

        return null;
    }

    private static boolean validateTransaction(Transaction t, RocksHandler handler)
            throws SignatureException {
        boolean flag = true;
        double sumIn = 0;

        if (t == null) return false;

        for (UTXO utxo : t.getInput()) {
            HashSet<UTXO> st = handler.getUTXOSet(utxo.getScriptPublicKey());
            flag &= st.contains(utxo);
            sumIn += utxo.getAmount();

            byte[] msg = utxo.toString().getBytes();
            BigInteger pubKeyRecovered = Sign.signedMessageToKey(msg, utxo.getScriptSig());
            boolean validSig = pubKeyRecovered.toString(16).equals(utxo.getScriptPublicKey());

            flag &= validSig;
        }

        int sumOut = 0;
        for (UTXO utxo : t.getOutput()) {
            sumOut += utxo.getAmount();
        }

        sumOut += t.getReturned() == null ? 0 : t.getReturned().getAmount();
        flag &= sumIn == sumOut;
        return flag;
    }


    public static boolean handleTransactionValidation(List<Transaction> ts, Transaction t, RocksHandler handler) throws SignatureException {
        if (validateTransaction(t, handler)) {
            for (int i = 0; i < ts.size(); i++) {
                if (ts.get(i).getInputPubKey().equals(t.getInputPubKey())) {
                    ts.set(i, t);
                    return true;
                }
            }

            ts.add(t);
            return true;
        }
        return false;
    }


    /**
     * @param t       transaction needed to be confirmed
     * @param handler database handler to access blocks
     * @return number of blocks mined following the block that contains
     * the transaction or -1 if it doesn't exist
     */
    public static int confirmTransaction(Transaction t, MongoHandler handler) {
        int idx = 1, ans = -1;
        Block b = handler.getBlock(idx);

        while (b != null) {
            if (b.getTransactions().SPV(t)) {
                ans = idx;
            }
            b = handler.getBlock(++idx);
        }

        return ans == -1 ? -1 : idx - ans - 1;
    }

    public static int getBalance(String[] pubKeys, RocksHandler handler) {
        int balance = 0;
        for (String pubKey : pubKeys) {
            HashSet<UTXO> st = handler.getUTXOSet(pubKey);
            for (UTXO utxo : st) {
                balance += utxo.getAmount();
            }
        }

        return balance;
    }
}

