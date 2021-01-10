#!/bin/bash

NR_SERVERS=$(sed -n '/nr_servers/p' ../config.toml | cut -c 14-)

for ((sv = 0; sv < $NR_SERVERS; sv++))
do
	echo "running server id $sv"
	$(sleep 1 | echo $sv | make run_storageserver) &
done

wait
