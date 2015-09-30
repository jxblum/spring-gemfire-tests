#!/bin/bash

# NOTE this Bash shell script assumes the user is running this example in working directory... $PWD/tmp

echo "Starting GemFire cluster and creating /Example PARTITION Region..."

gfsh -e "set variable --name=PWD --value=$PWD" -e "run --file=$PWD/../bin/createExamplePartitionRegion.gfsh"

echo "GemFire cluster started!"
