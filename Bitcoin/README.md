# cryptoOptimized
# SummerOfBitcoin 
 3 defined class :-  
# Transactions Class
    private int fee;  
    private int weight;  
    private String id;  
    private String par_id;  
    HashSet<String> par_ids = new HashSet<>();  
    HashSet<String> resolved = new HashSet<>();  
  **Resolved is maintained to ensure no child Transaction occurs before parent**      
    
      
        
          
 # TransactionComparator class implements Comparator<Transaction>  
   **used to prioritize the Transactions**  
       If it arrives before parent in heap, push it down.    
       Prioritized based on greater fee.
       If can't be seperated lesser weight gets higher priority.  
       If weight becomes greater than threshold, new BLOCK is created.  
# Miner Class   
    threshold weight taken as 4, 000, 000  ( As Suggested )  
    Heap of all executable transaction  

#  Data Structure Used  
     Heap with user defined prioritization.  

#  Greedy Approach Used
     Keep Track of all the **child Transactions of a particular transaction** using a **HashMap< <tx_id>, List<Transactions> >**      
     If all the parent transactions are over, we place the child in the Heap of Executable Transactions 
   

#   Workflow 
     Till All Transactions aren't over :     
   **If a Transaction doesn't have any parent Transactions left to be complete push it in heap of possible transactions**   
     Pop the top of the Heap and work with this transaction  
     If wt of Current Transaction + wt of Transactions in this Block > threshold:  
     <t />    Create a new block after pushing this block to the ledger
     else:    
     <t />   Push it in the same Block and resume with the rest of this process.  
   
     
      
     
     
   
   
   
     
       
         
         
   Basic Details:  
    All code is in Java ( Miner.java )   
    Block printed in newlines in block.txt  
    For seperation between different blocks  look at blocks_with_newline_bw_them.txt   
    
   
