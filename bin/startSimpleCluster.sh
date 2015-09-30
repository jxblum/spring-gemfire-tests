#!/bin/bash

echo "Starting cluster..."

gfsh -e "run --file=$PWD/../bin/startCluster.gfsh"

echo "Cluster started!"
