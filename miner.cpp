#include<bits/stdc++.h>
#include<fstream>
#define ll long long
using namespace std;
ll strtoll(string s)
{
    ll res = 0;
    ll p = 1;
    for ( int i=s.size()-1; i>=0; i-- )
        {
            res += (p*(s[i]-'0'));
            p *= 10;
        }
    return res;
}

class Block {

private:
    ll fee;
    ll weight;
    string id;
    string par_id;
public:
    Block(string id, string fee, string weight, string par_id)
    {
        this->id = id;
        this->fee = strtoll(fee);
        this->weight = strtoll(weight);
        this->par_id = par_id;
    }
    string get_id()
    {
        return id;
    }    
    string get_par_id()
    {
        return par_id;
    }
    ll get_fee()
    {
        return fee;
    }
    ll get_weight()
    {
        return weight;
    }
};

//unordered_set<string> available; 


struct blockPriority {

bool operator() (Block a, Block b)
{
    return a.get_fee() < b.get_fee() || (a.get_fee()==b.get_fee()&&a.get_weight()>b.get_weight());
}

};

priority_queue<Block, vector<Block>, blockPriority> available;
map<string, Block> notInitiable; // Parent transaction not complete

void read_csv()
{
    ifstream mempool;
    mempool.open("mempool1.csv");

    while (mempool.good())
    {
        string line;
        getline(mempool, line, '\n');
        string w;
        int c = 0;
        string id, fee, weight, par_id;
        for ( auto x: line )
        {
            if (x==',')
            {
                c++;
            //    cout << c << "\n";
                if (c==1)   id = w;
                if (c==2)   fee = w;
                if (c==3)   weight = w;
                w = "";
            }
            else
                w += x;
        }
        par_id = w;
        //cout << id << ", " << fee << ", " << weight << ", " << par_id << "\n";
        if (id!=""&&id!="tx_id") 
            {
                //cout << par_id << "\n";
                Block block(id, fee, weight, par_id);
                //cout << "Pushing...\n";
                if (par_id=="")
                    available.push(block);
                else{
                    pair<string, Block> p = make_pair(par_id, block);
                    notInitiable.insert(p);
                    //notInitiable[par_id] = block;     
                }
            }
    }    
 
}
unordered_set<string> resolvedHeaders;
void dispatchUpdater()
{
    ifstream res;
    res.open("dispatch-status.txt");
    string hashValue;
    getline(res, hashValue, '\n');
    resolvedHeaders.insert(hashValue);
}

void dispatch(Block block)
{
    ofstream req;
    req.open("dispatch-status.txt");
    string request = block.get_par_id(); // + ", " + block.get_id();
    if (request=="")    request = "rootElem";
    req << request;
    req << "\n";
    request = block.get_id();
    req << request;
    req.close();
}


int main()
{
    read_csv();   
    ll threshold = 4000000;
    cout << available.size() << " " << notInitiable.size();
 
    //cout << pq.size() << "\n";
    // I have my transactions

    resolvedHeaders.insert("");
    while (!available.empty())
    {
        vector<Block> dispatcher;
        ll wt_sum = 0;
        while(!available.empty())
        {
            Block b = available.top();
            if (wt_sum+b.get_weight()>threshold)  break;
            dispatcher.push_back(b);
            wt_sum += b.get_weight();
            available.pop();
        }
        for( int i=0; i<dispatcher.size(); i++ )
            dispatch(dispatcher[i]);
    }


    // Looking for possible transactions
    
    
    
    
    
    
    
    return 0;
}