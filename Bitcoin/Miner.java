package Bitcoin;
import java.io.*; 
import java.util.*;

public class Miner { 

    private static PriorityQueue<Transaction> readyTransactions = new PriorityQueue<>(new TransactionComparator());
    private static Queue<String> readyHashes = new LinkedList<>();

    private static HashMap<String, List<Transaction>> readFile() {
        String file = "../mempool.csv";
        BufferedReader reader = null;
        String line = "";
        HashMap<String, List<Transaction>> transactions = new HashMap();
        
        try {
            reader = new BufferedReader(new FileReader(file));
            while((line= reader.readLine())!=null) {
                String[] row = line.split(",");
                if (row.length==0 || row[0].equals("tx_id") )  continue;
                String id, fee, weight, par_id;
                id = row[0];
                fee = row[1];
                weight = row[2];
                par_id = "";
                if (row.length==3) {
                Transaction transaction = new Transaction(id, fee, weight, par_id);
                if (transactions.containsKey(par_id)) {
                    transactions.get(par_id).add(transaction);
                } else {
                    List<Transaction> b = new ArrayList<>();
                    b.add(transaction);
                    transactions.put(par_id, b);
                }
                    continue;
                }
                par_id = row[3];
                String[] parents = row[3].split(";");   
                if (parents.length < 2) {
                    Transaction transaction = new Transaction(id, fee, weight, par_id);
                    if (transactions.containsKey(par_id)) {
                        transactions.get(par_id).add(transaction);
                    } else {
                        List<Transaction> b = new ArrayList<>();
                        b.add(transaction);
                        transactions.put(par_id, b);
                    }
                }
                else if (parents.length>1)   {
                    // Multiple Transactions preceed
                    Transaction transaction = new Transaction(id, fee, weight, parents[0]);
                    for ( int i=0; i<parents.length; i++ ) {
                        if (transactions.containsKey(parents[i])) {
                            transactions.get(parents[i]).add(transaction);
                        } else {
                            List<Transaction> b = new ArrayList<>();
                            b.add(transaction);
                            transactions.put(parents[i], b);
                        }
                        transaction.addParentTransactions(parents[i]);
                    }   
                }
            }
        } catch( Exception e ) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
       return transactions;
    }

    private static void activateChildNode(HashMap<String, List<Transaction>> transactions) {
        
        while(!readyHashes.isEmpty()) {
            String hashVal = readyHashes.remove();
            if (transactions.containsKey(hashVal)) {
                for ( Transaction transaction : transactions.get(hashVal) ) {
                    if (transaction.dumpParentTransactions(hashVal)) {
                    readyTransactions.add(transaction);                        
                    }
                }
                transactions.remove(hashVal);
            }
        }
        return;
    }

    public static int performTransaction(Transaction transaction) {
            String s = transaction.getId();
            readyHashes.add(s);         
            return transaction.getFee();
    }

    // Driver code 
    public static void main(String args[]) {
        int netTransactionGain = 0;
        int threshold = 4000000;
       
        HashMap<String, List<Transaction>> transactions = new HashMap<>();
        transactions = readFile();
        readyHashes.add("");
        int transCount = 0;
            String blockHash = "";
            while (!transactions.isEmpty()||!readyTransactions.isEmpty()) {
            
            if (!transactions.isEmpty())
                activateChildNode(transactions);
            List<Transaction> carryOut = new ArrayList<>();   
            int totWt = 0;
            
            while(!readyTransactions.isEmpty()) {
                Transaction head = readyTransactions.peek();
                if (head.getWeight()+totWt<threshold) {
                    totWt += head.getWeight();
                    carryOut.add(readyTransactions.poll());
                } else break;
            }

            for (Transaction transaction : carryOut) {
                transCount++;
            }

            System.out.println(transCount);
        }
    } 
} 


class TransactionComparator implements Comparator<Transaction> {

    @Override
    public int compare(Transaction o1, Transaction o2) {
        return o1.getFee() - o2.getFee();
    }

} 
class Transaction {
    
    private int fee;
    private int weight;
    private String id;
    private String par_id;
    HashSet<String> par_ids = new HashSet<>();
    public Transaction(String id, String fee, String weight, String par)
    {
        this.id = id;
        this.fee = Integer.parseInt(fee);// (fee);
        this.weight = Integer.parseInt(weight);
        this.par_id = par;
        par_ids.add(par);
    }

    public void addParentTransactions(String par) {
        par_ids.add(par);
    }

    String getId() {
        return id;
    }    
    String getPar_id() {
        return par_id;
    }
    int getFee() {
        return fee;
    }
    int getWeight() {
        return weight;
    }
    boolean dumpParentTransactions(String par) {
        if (par_ids.contains(par)) {
            par_ids.remove(par);
        }
        return par_ids.isEmpty();
    }    
}
