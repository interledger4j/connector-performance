#!/bin/sh

JAR_LOCATION=/opt/ilp-performance.jar

echo Jar location: "$JAR_LOCATION"

simulation_name=$1

echo Running simulation: "$simulation_name"

java -cp "$JAR_LOCATION" io.gatling.app.Gatling -s "$simulation_name"