#!/bin/bash

echo "Stopping cluster..."

gfsh -e "run --file=$PWD/../bin/stopCluster.gfsh"

rm -Rf ServerB/ ServerA/ LocatorX/

echo "Cluster stopped!"
