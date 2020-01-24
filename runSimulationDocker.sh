#!/bin/sh

JAR_LOCATION=/opt/ilp-performance.jar

echo Jar location: "$JAR_LOCATION"

simulation_name=$1
concurrency=$2
rampUp=$3
holdFor=$4
throughput=$5

echo Running simulation: "$simulation_name"
echo concurrency: "$concurrency"
echo rampUp: "$rampUp"
echo holdFor: "$holdFor"
echo throughput: "$throughput"

java -Dconcurrency="$concurrency" -Dramp-up="$rampUp" -Dhold-for="$holdFor" -Dthroughput="$throughput" -cp "$JAR_LOCATION" io.gatling.app.Gatling -s "$simulation_name"
