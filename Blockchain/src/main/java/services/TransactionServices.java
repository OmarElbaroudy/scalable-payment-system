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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TransactionServices {

    //TODO call rocksHandler update after creating transaction successfully
    public static Transaction createTransaction
    (String[] privKeys, String recKey, double amount, RocksHandler handler) {
        ECKeyPair[] keyPairs = new ECKeyPair[privKeys.length];
        for (int i = 0; i < privKeys.length; i++) {
            BigInteger privKey = new BigInteger(privKeys[i], 16);
            keyPairs[i] = ECKeyPair.create(privKey);
        }

        double balance = 0;
        List<UTXO> input = new ArrayList<>();
        for (ECKeyPair pair : keyPairs) {
            String pubKey = pair.getPublicKey().toString(16);
            HashSet<UTXO> st = handler.getUTXOSet(pubKey);

            for (UTXO utxo : st) {
                balance += utxo.getAmount();
                byte[] msg = utxo.toString().getBytes();
                Sign.SignatureData signature = Sign.signMessage(msg, pair);

                UTXO signed = new UTXO(utxo, signature);
                input.add(signed);
            }
        }

        if (balance < amount) return null;

        if (balance == amount) {
            return new Transaction(input, new UTXO(amount, recKey));
        }

        double rem = balance - amount;
        UTXO returned = new UTXO(rem, keyPairs[0].getPublicKey().toString(16));
        return new Transaction(input, new UTXO(amount, recKey), returned);
    }

    public static boolean validateTransaction(Transaction t, RocksHandler handler)
            throws SignatureException {
        boolean flag = true;
        double sumIn = 0;

        if(t == null) return false;

        for (UTXO utxo : t.getInput()) {
            HashSet<UTXO> st = handler.getUTXOSet(utxo.getScriptPublicKey());
            flag &= st.contains(utxo);
            sumIn += utxo.getAmount();

            byte[] msg = utxo.toString().getBytes();
            BigInteger pubKeyRecovered = Sign.signedMessageToKey(msg, utxo.getScriptSig());
            boolean validSig = pubKeyRecovered.toString(16).equals(utxo.getScriptPublicKey());

            flag &= validSig;
        }

        double sumOut = t.getOutput().getAmount();
        sumOut += t.getReturned() == null ? 0 : t.getReturned().getAmount();
        flag &= Math.abs(sumIn - sumOut) < 0.001;

        return flag;
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

    public static double getBalance(String[] pubKeys, RocksHandler handler) {
        double balance = 0;
        for (String pubKey : pubKeys) {
            HashSet<UTXO> st = handler.getUTXOSet(pubKey);
            for (UTXO utxo : st) {
                balance += utxo.getAmount();
            }
        }

        return balance;
    }

    public static void orderByPubKey(List<Transaction> ts, HashMap<String, List<Transaction>> mp) {
        for(Transaction t : ts){

            if(t.getInput().isEmpty()){
                List<Transaction> cur = mp.getOrDefault("genesis",  new ArrayList<>());
                cur.add(t);

                mp.put("genesis", cur);
                continue;
            }

            UTXO input = t.getInput().get(0);
            String pubKey = input.getScriptPublicKey();
            List<Transaction> cur = mp.getOrDefault(pubKey, new ArrayList<>());

            cur.add(t);
            mp.put(pubKey, cur);
        }
    }
}
