package persistence.models;

import org.apache.commons.codec.digest.DigestUtils;
import utilities.Sign;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class UTXO implements Serializable {
    private String txId;
    private double amount;
    private String scriptPublicKey; //HEX
    private Sign.SignatureData scriptSig;

    public UTXO(double amount, String scriptPublicKey, Sign.SignatureData scriptSig) {
        this.amount = amount;
        this.scriptPublicKey = scriptPublicKey;
        this.scriptSig = scriptSig;
        String input = amount + scriptPublicKey;
        txId = UUID.randomUUID() + hash(input);
    }

    public UTXO(double amount, String scriptPublicKey) {
        this.amount = amount;
        this.scriptPublicKey = scriptPublicKey;
        String input = amount + scriptPublicKey;
        txId = UUID.randomUUID() + hash(input);
        scriptSig = null;
    }

    public UTXO(UTXO utxo, Sign.SignatureData scriptSig){
        this.amount = utxo.amount;
        this.scriptPublicKey = utxo.scriptPublicKey;
        this.txId = utxo.txId;
        this.scriptSig = scriptSig;
    }

    public UTXO(UTXO utxo, double amount){
        this.txId = utxo.txId;
        this.amount = amount;
        this.scriptPublicKey = utxo.scriptPublicKey;
        this.scriptSig = utxo.scriptSig;
    }

    public UTXO() {
    }

    private String hash(String input) {
        return new DigestUtils("SHA3-256").digestAsHex(input);
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getScriptPublicKey() {
        return scriptPublicKey;
    }

    public void setScriptPublicKey(String scriptPublicKey) {
        this.scriptPublicKey = scriptPublicKey;
    }

    public Sign.SignatureData getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(Sign.SignatureData scriptSig) {
        this.scriptSig = scriptSig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UTXO utxo = (UTXO) o;
        return Objects.equals(txId, utxo.txId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(txId);
    }

    @Override
    public String toString() {
        return "UTXO{" +
                "txId='" + txId + '\'' +
                ", amount=" + amount + '\'' +
                ", scriptPublicKey='" + scriptPublicKey + '}';
    }
}
