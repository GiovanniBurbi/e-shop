 #!/bin/bash

    echo "Starting replica set initialize"
    until curl http://27017:27017/serverStatus\?text\=1 2>&1 | grep uptime | head -1;
    do
      sleep 1
    done
    sleep 2
    echo "Connection finished"
    echo "Creating replica set"
    mongo --host mongodb:27017 <<EOF
	rs.initiate();  
EOF
    echo "replica set created"