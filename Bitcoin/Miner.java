package Bitcoin;
import java.io.*; 
import java.util.*;

public class Miner { 

    private static PriorityQueue<Transaction> readyTransactions = new PriorityQueue<>(new TransactionComparator());
    private static Queue<String> readyHashes = new LinkedList<>();
    private static int threshold = 4000000;

    private static HashMap<String, List<Transaction>> readFile() {
        String file = "../mempool.csv";
        BufferedReader reader = null;
        String line = "";
        HashMap<String, List<Transaction>> transactions = new HashMap();
        
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
                if (row.length==3) {
                Transaction transaction = new Transaction(id, fee, weight, par_id);
                if (transactions.containsKey(par_id)) {
                    transactions.get(par_id).add(transaction);
                } else {
                    List<Transaction> b = new ArrayList<>();
                    b.add(transaction);
                    transactions.put(par_id, b);
                }
                    readyTransactions.add(transaction);
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
        int tot = 0;
        int initialWt = 0;
        int blocks = 0;
        int netGain = 0;
        int curGain = 0;
        while(!readyTransactions.isEmpty()) {
            Transaction transc = readyTransactions.poll();
            String hashVal = transc.getId();
            int weight = transc.getWeight();
            if (initialWt+weight>threshold) {
                blocks++;
                // Use File Ops Here
    /*FileWriter fw = null;
    try{
        fw = new FileWriter("block.txt", true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw)
        {
            out.println("the text");
            //more code
            out.println("more text");
            //more code
        } catch (IOException e) {
        e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
                
                
      */          
                initialWt = weight;
                curGain = 0;
            }
            if (tot==0)
                System.out.println(transc.getId());
            curGain = performTransaction(transc);
            tot++;
            netGain += curGain;
            
            if (transactions.containsKey(hashVal)) {
                for ( Transaction transaction : transactions.get(hashVal) ) {
                    if (transaction.dumpParentTransactions(hashVal)) {
                    readyTransactions.add(transaction);                        
                    }
                }
                transactions.remove(hashVal);
            }
        }
        System.out.println(tot);
        return;
    }

    public static int performTransaction(Transaction transaction) {
            String s = transaction.getId();
            readyHashes.add(s);         
            return transaction.getFee();
    }

    // Driver code 
    public static void main(String args[]) {
        
        //List<Block> blocks = new ArrayList<Block>();
        HashMap<String, List<Transaction>> transactions = new HashMap<>();
        transactions = readFile();
        //readyHashes.add("");
        if (!transactions.isEmpty())
                activateChildNode(transactions);
    } 
} 


class TransactionComparator implements Comparator<Transaction> {

    @Override
    public int compare(Transaction lhs, Transaction rhs) {
        if (lhs.resolved.contains(rhs.getId()))
            return 1;
        if (rhs.resolved.contains(lhs.getId()))
            return -1;
        if (lhs.getFee()>rhs.getFee())    return -1;
        else {
            if (lhs.getFee()==rhs.getFee()) {
                if (lhs.getWeight()<rhs.getWeight())    return -1;
            }
        }
        return 1;
    }

} 
class Transaction {
    
    private int fee;
    private int weight;
    private String id;
    private String par_id;
    HashSet<String> par_ids = new HashSet<>();
    HashSet<String> resolved = new HashSet<>();
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
            resolved.add(par);
            par_ids.remove(par);
        }
        return par_ids.isEmpty();
    }    
}
