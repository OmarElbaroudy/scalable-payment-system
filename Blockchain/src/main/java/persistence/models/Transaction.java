package persistence.models;

import java.util.List;

public class Transaction {
    private List<UTXO> input;
    private UTXO output, returned;

    public Transaction(List<UTXO> input, UTXO output, UTXO returned) {
        this.input = input;
        this.output = output;
        this.returned = returned;
    }

    public Transaction(List<UTXO> input, UTXO output) {
        this.input = input;
        this.output = output;
        this.returned = null;
    }

    public Transaction() {
    }

    public List<UTXO> getInput() {
        return input;
    }

    public void setInput(List<UTXO> input) {
        this.input = input;
    }

    public UTXO getOutput() {
        return output;
    }

    public void setOutput(UTXO output) {
        this.output = output;
    }

    public UTXO getReturned() {
        return returned;
    }

    public void setReturned(UTXO returned) {
        this.returned = returned;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "input=" + input.toString() +
                ", output=" + output.toString() +
                ", returned=" + (returned == null ? "" : returned.toString()) +
                '}';
    }
}
