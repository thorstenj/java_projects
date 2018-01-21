import java.util.*;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    private UTXOPool pool;

    public TxHandler(UTXOPool utxoPool) {
        this.pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

        double txInputSum = 0;
        double txOutputSum = 0;

        UTXOPool uniqueUtxos = new UTXOPool();

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input txInput= tx.getInput(i);
            UTXO utxo = new UTXO(txInput.prevTxHash, txInput.outputIndex);
            Transaction.Output txOutput = pool.getTxOutput(utxo);

            // (1) all outputs claimed by {@code tx} are in the current UTXO pool
            if (!pool.contains(utxo)) {
                return false;
            }

            // (2) the signatures on each input of {@code tx} are valid
            if (!Crypto.verifySignature(txOutput.address, tx.getRawDataToSign(i), txInput.signature)) {
                return false;
            }

            // (3) no UTXO is claimed multiple times by {@code tx}
            if (uniqueUtxos.contains(utxo)) {
                return false;
            }

            uniqueUtxos.addUTXO(utxo, txOutput);
            txInputSum += txOutput.value;
        }

        // (4) all of {@code tx}s output values are non-negative
        for (Transaction.Output txOutput : tx.getOutputs()) {
            if (txOutput.value < 0) {
                return false;
            }
            txOutputSum += txOutput.value;
        }

        // 5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        return txInputSum >= txOutputSum;

    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {

        Set<Transaction> validTxs = new HashSet<>();

        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                validTxs.add(tx);
                for (Transaction.Input in : tx.getInputs()) {
                    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                    pool.removeUTXO(utxo);
                }
                for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output out = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    pool.addUTXO(utxo, out);
                }
            }
        }

        Transaction[] validTxArray = new Transaction[validTxs.size()];
        return validTxs.toArray(validTxArray);
    }

}
