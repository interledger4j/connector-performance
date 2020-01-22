FROM openjdk:8-alpine

COPY target/ilp-performance-*.jar /opt/ilp-performance.jar
COPY runSimulationDocker.sh /opt/runSimulation.sh

ENV SIMULATION=""

CMD /opt/runSimulation.sh ${SIMULATION}