FROM openjdk:8-alpine

COPY target/ilp-performance-*.jar /opt/ilp-performance.jar
COPY runSimulationDocker.sh /opt/runSimulation.sh

ENV SIMULATION=""
ENV CONCURRENCY=10
ENV RAMP_UP=1
ENV HOLD_FOR=10
ENV THROUGHPUT=100
CMD /opt/runSimulation.sh ${SIMULATION} ${CONCURRENCY} ${RAMP_UP} ${HOLD_FOR} ${THROUGHPUT}