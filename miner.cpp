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

struct blockPriority {

bool operator() (Block a, Block b)
{
    return a.get_fee() < b.get_fee() || (a.get_fee()==b.get_fee()&&a.get_weight()>b.get_weight());
}

};

priority_queue<Block, vector<Block>, blockPriority> pq;

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
            w += x;
        }
        par_id = w;
        //cout << id << ", " << fee << ", " << weight << ", " << par_id << "\n";
        if (id!=""&&id!="tx_id") 
            {
                Block block(id, fee, weight, par_id);
                //cout << "Pushing...\n";
                pq.push(block);
            }
    }    
 
}


int main()
{
    read_csv();   
    cout << pq.size() << "\n";
    return 0;
}