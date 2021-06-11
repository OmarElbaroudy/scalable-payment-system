package persistence.models;

import java.util.List;
import java.util.Objects;

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
                "input=" + (input == null ? "" : input.toString()) +
                ", output=" + (output == null ? "" : output.toString()) +
                ", returned=" + (returned == null ? "" : returned.toString()) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return input != null && that.input != null &&
                Objects.equals(input.get(0), that.input.get(0)) &&
                Objects.equals(output, that.output) &&
                Objects.equals(returned, that.returned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output, returned);
    }
}
