import hashlib
while(1):
    f = open("dispatch-status.txt", "r")
    par_id = str(f.readline())
    if par_id=="rootElem":
        par_id = ""
    f = open("dispatch-status.txt", "r")
    id = str(f.readline())
    f.close()
    toHash = par_id+id
    hashValue = hashlib.sha256(toHash.encode())
    f = open("dispatch-status.txt", "w")
    f.write(hashValue.hexdigest())
    f.close()