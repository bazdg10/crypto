# Summer Of Bitcoin
# C++

Given a list of transactions validate and initiate the transactions and print the order of the transactions performed<br />

Data Structure Used: Heap <br />
Time Complexity: O(n logn)  *n = number of transactions*<br />

Transaction Class<br /><br />
    vars<br />
    int fee | weight | id | par_id<br />
    HashSet<String> par_ids<br /><br />
    funcs<br />
      addParentTransactions(String par)<br />
      String getId()<br />
      String getPar_id()<br /> 
      int getFee()<br /> 
      int getWeight()<br /> 
      boolean dumpParentTransactions(String par)<br /> <br /> <br /><br />
Miner Class<br /><br />
   vars<br />
      PriorityQueue<Transaction><br />
      Queue<String> readyHashes<br /><br />
   funcs<br />
        HashMap<String, List<Transaction>> readFile() <br />
        activateChildNode(HashMap<String, List<Transaction>> transactions)<br /> 
        int performTransaction(Transaction transaction) <br />
   

