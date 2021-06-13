package BitcoinTest;
import java.io.*;
import java.math.BigInteger; 
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 
import java.util.*;

public class Miner { 

    private static PriorityQueue<Transaction> readyTransactions = new PriorityQueue<>(new TransactionComparator());
    private static Queue<String> readyHashes = new LinkedList<>();
    private static HashSet<String> multiCheckers = new HashSet<>();

    private static List<HashMap<String, List<Transaction>>> readFile() {
        System.out.println("Hola");
        String file = "../mempool1.csv";
        BufferedReader reader = null;
        String line = "";
        List<HashMap<String, List<Transaction>>> allTransactions = new ArrayList<>();
        HashMap<String, List<Transaction>> transactions = new HashMap<>();
        HashMap<String, List<Transaction>> multiTransactions = new HashMap<>();
        
        try {
            //int lca = 0;
            reader = new BufferedReader(new FileReader(file));
            while((line= reader.readLine())!=null) {
                String[] row = line.split(",");
                if (row.length==0 || row[0].equals("tx_id") )  continue;
                String id, fee, weight, par_id;
                id = row[0];
                fee = row[1];
                weight = row[2];
                par_id = "";
                if (row.length>3)   par_id = row[3];   
                //else    lca++;
                Transaction transaction = new Transaction(id, fee, weight, par_id);
                if (row.length<5) {
                    if (transactions.containsKey(par_id)) {
                        transactions.get(par_id).add(transaction);
                    } else {
                        List<Transaction> b = new ArrayList<>();
                        b.add(transaction);
                        transactions.put(par_id, b);
                    }
                }
                else if (row.length>4)   {
                    // Multiple Transactions preceed
                    for ( int i=3; i<row.length; i++ ) {
                        if (transactions.containsKey(row[i])) {
                            transactions.get(row[i]).add(transaction);
                        } else {
                            List<Transaction> b = new ArrayList<>();
                            b.add(transaction);
                            multiTransactions.put(row[i], b);
                        }
                        transaction.addParentTransactions(row[i]);
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
        allTransactions.add(transactions);
        allTransactions.add(multiTransactions);
        return allTransactions;
    }

    private static void activateChildNode(HashMap<String, List<Transaction>> transactions) {
        
        while(!readyHashes.isEmpty()) {
            String hashVal = readyHashes.remove();
            if (transactions.containsKey(hashVal)) {
                for ( Transaction transaction : transactions.get(hashVal) ) {
                    readyTransactions.add(transaction);
                }
                transactions.remove(hashVal);
            }
        }
        return;
    }

    public static int performTransaction(Transaction transaction) {
            String s = transaction.getId();
            readyHashes.add(s);
            multiCheckers.add(s);            
            return transaction.getFee();
    }

    // Driver code 
    public static void main(String args[]) {
        int netTransactionGain = 0;
        int threshold = 4000000;
        //List<Block> blocks = new ArrayList<Block>();
        List<HashMap<String, List<Transaction>>> allTransactions = new ArrayList<>();
        allTransactions = readFile();
        HashMap<String, List<Transaction>> transactions = allTransactions.get(0);
        HashMap<String, List<Transaction>> multiTransactions = allTransactions.get(1);
        System.out.println(multiTransactions.size());
        readyHashes.add("");
        int c = 0;
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
                netTransactionGain += performTransaction(transaction);
                c++;
                //System.out.println(c);
                
            }
        }
         
        System.out.println(netTransactionGain);
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
    HashSet<String> par_ids;
    public Transaction(String id, String fee, String weight, String par)
    {
        this.id = id;
        this.fee = Integer.parseInt(fee);// (fee);
        this.weight = Integer.parseInt(weight);
        this.par_id = par;
        HashSet<String> par_ids = new HashSet<>();
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
    boolean parentTransactionsLeft() {
        return !par_ids.isEmpty();
    }
}
