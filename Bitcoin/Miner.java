package Bitcoin;
import java.io.*; 
import java.util.*;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingDeque;

public class Miner { 

    private static PriorityQueue<Transaction> readyTransactions = new PriorityQueue<>(new TransactionComparator());
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

    private static void registerBlock (Queue<String> block) throws IOException{
        File file = new File("block.txt");
        //File file = new File("blocks_with_newLine_bw_them.txt");
        FileWriter fw = new FileWriter(file, true);
        PrintWriter pw = new PrintWriter(fw);
        
        while(!block.isEmpty()) {
            String x = block.remove();
            pw.println(x);
            if (block.isEmpty())    break;
        }
        //pw.println("\n");
        pw.close();
    }


    private static void activateChildTransactions(HashMap<String, List<Transaction>> transactions) throws IOException {

        int initialWt = 0;
        //int tot = 0;
        /*int netGain = 0;
        int curGain = 0;*/
        //BlockingQueue<String> block = new LinkedBlockingDeque<>();
        //BlockingQueue<String> copy = new LinkedBlockingDeque<>();
        Queue<String> block = new LinkedList<>();
        while(!readyTransactions.isEmpty()) {
            Transaction transc = readyTransactions.poll();
            String hashVal = transc.getId();
            int weight = transc.getWeight();
            if (initialWt+weight>threshold) {
                initialWt = 0;
                registerBlock(block);
                // These commented out ops should be used for large no. of entries
                // Small test case so we go on without threads as there are 3 write calls in this loop
                //copy = block;
                //block.clear();
                /*Thread t = new Thread(new AddBlockToLegder(copy));
                t.start();
                try {
                    t.join();
                } catch(Exception e) {
                    continue;
                }
                */
            }
            block.add(transc.getId());
            initialWt += transc.getWeight();
            performTransaction(transc);
            //curGain
            //tot++;
            //netGain += curGain;
            
            if (transactions.containsKey(hashVal)) {
                for ( Transaction transaction : transactions.get(hashVal) ) {
                    if (transaction.dumpParentTransactions(hashVal)) {
                    readyTransactions.add(transaction);                        
                    }
                }
                transactions.remove(hashVal);
            }
        }

        // Checking if block weight didn't reach threshold to add remaining blocks
        if (!block.isEmpty())
            registerBlock(block);
        //System.out.println(tot);
        return;
    }

    public static int performTransaction(Transaction transaction) {   
            return transaction.getFee();
    }

    // Driver code 
    public static void main(String args[]) throws IOException {

        HashMap<String, List<Transaction>> transactions = new HashMap<>();
        transactions = readFile();
        if (!transactions.isEmpty())
                activateChildTransactions(transactions);
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
/*
class AddBlockToLegder implements Runnable {

    private BlockingQueue<String> block = new LinkedBlockingDeque<>();

    public AddBlockToLegder ( BlockingQueue<String> block ) {
        this.block = block;       
    }

    @Override
    public void run() {
        try {
            File file = new File("block.txt");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            
            while(!block.isEmpty()) {
                String x = block.remove();
                pw.println(x);
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
*/