# Connector Performance
This document describes a standardized load and performance testing suite that can be used to gauge the performance and
 resiliency of an [ILPv4 
Connector](https://github.com/interledger4j/ilp-connector) under load.

## Performance Testing Considerations
Testing a connector involves several facets of the overall Interledger payment protocol stack. For example, it's 
useful to measure each of the following metrics:

1. Fulfilled Packets-per-second.
1. Rejected Packets-per-second.
1. Unauthenticated Packets-per-second.

The fulfill/reject metrics should be measured using different payment paths with varying characteristics such as
 FX-rate conversions, payment path lengths, and other variables inherent in various topologies.

# Performance Test Topolgies

## Overview
This harness utilizes four different topologies for its testing:

1. **Single Connector with Loopback Links**  
This topology contains a single connector with 2 loopback 
links. The first link always fulfills; the second link always rejects with a `T02 Peer Busy` response to simulate a peer link that is being throttled.

1. **Single Connector with SPSP Peer**  
This topology contains a single connector with 2 child links that support [SPSP](https://github.com/interledger/rfcs
/blob/master/0009-simple-payment-setup-protocol/0009-simple-payment-setup-protocol.md). 
 
1. **Two Connectors with ILP-over-HTTP**  
This type of topology involves only a two connectors, each with a single ILP-over-HTTP link.

1. **4 Connector Multihop**  
This topology simulates four connectors (3 hops) with each operating ILP-over-HTTP links to simulate three total
 currency conversions.

## Single Connector with Loopback Links
This topology contains a single connector with 2 loopback 
links. The first link always fulfills; the second link always rejects with a `T02 Peer Busy` response to simulate a peer link that is being throttled.

### Topology Diagram
```text
                ┌───────────────────────┐                
                │                       │                
                │ https://jc.ilpv4.dev  │                
                │                       │                
                └───────────────────────┘                
                            △                            
             ┌──────────────┴──────────────┐             
             ▽                             ▽             
┌─────────────────────────┐   ┌─────────────────────────┐
│                         │   │                         │
│Account: lt-lb-fulfiller │   │ Account: lt-lb-rejector │
│                         │   │                         │
└─────────────────────────┘   └─────────────────────────┘
```

### Ingress Account
To create the `lt-ingress` account, which is used for all ingress into the connector, execute the following command:

```text
curl --location --request POST 'https://jc.ilpv4.dev/accounts' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
--data-raw '{
  "accountId": "lt-ingress",
  "accountRelationship": "CHILD",
  "linkType": "ILP_OVER_HTTP",
  "assetCode": "USD",
  "assetScale": "4",
  "customSettings": {
        "ilpOverHttp.incoming.auth_type": "SIMPLE",
        "ilpOverHttp.incoming.simple.auth_token": "shh",
        "ilpOverHttp.outgoing.auth_type": "SIMPLE",
        "ilpOverHttp.outgoing.simple.auth_token": "shh",
        "ilpOverHttp.outgoing.url": "https://money.ilpv4.dev/ilp"
   }
}'
```


### Fulfil Account
To create the `lt-lb-fulfiller` account, execute the following command:

```text
curl --location --request POST 'https://jc.ilpv4.dev/accounts' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
--data-raw '{
  "accountId": "lt-lb-fulfiller",
  "accountRelationship": "CHILD",
  "linkType": "LOOPBACK",
  "assetCode": "XRP",
  "assetScale": "9",
  "customSettings": {
  	"simulatedRejectErrorCode":"T02"
  }
}'
```

### Reject Account
To create the `lt-lb-rejector` account, execute the follwoing command:

```text
curl --location --request POST 'https://jc.ilpv4.dev/accounts' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
--data-raw '{
  "accountId": "lt-lb-rejector",
  "accountRelationship": "CHILD",
  "linkType": "LOOPBACK",
  "assetCode": "XRP",
  "assetScale": "9",
  "customSettings": {
  	"simulatedRejectErrorCode":"T02"
  }
}'
```

## Single Connector with HTTP Links
This topology contains a single connector with 2 ILP-over-HTTP links that excercise actual outbound network
 connectivity from the Connector. Both links connect to the an SPSP receiver that fulfills or rejects depending on the 
 condition in the packet created using SPSP and STREAM.

### Topology Diagram
```text
 ┌──────────────────┐       ┌─────────────────────────┐      
 │                  │       │   Account: `lt-spsp`    │      
 │ Load Test Sender │──────▶│  (Type: ILP-over-HTTP)  │      
 │                  │       └─────────────────────────┘      
 └──────────────────┘                    │                   
                          ┌──────────────┘                   
                          │                                  
                          ▽                                  
              ┌───────────────────────┐                      
              │                       │                      
              │    ILPv4 Connector    │                      
              │ https://jc.ilpv4.dev  │                      
              │                       │                      
              └───────────────────────┘                      
                          │                                  
             ┌────────────┴────────────────────┐             
             ▽                                 ▽             
┌─────────────────────────┐       ┌─────────────────────────┐
│  Account: `lt-spsp-rs`  │       │ Account: `lt-spsp-java` │
│  (Type: ILP-over-HTTP)  │       │  (Type: ILP-over-HTTP)  │
└─────────────────────────┘       └─────────────────────────┘
             │                                 │             
             ▽                                 ▽             
 ┌───────────────────────┐         ┌───────────────────────┐ 
 │      SPSP Server      │         │      SPSP Server      │ 
 │https://rc3.xpring.dev │         │ https://jc.ilpv4.dev  │ 
 └───────────────────────┘         └───────────────────────┘ 
```

### Ingress Account
To create the `lt-ingress` account, which is used for all ingress into the connector, execute the following command:

```text
curl --location --request POST 'https://jc.ilpv4.dev/accounts' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
--data-raw '{
  "accountId": "lt-ingress",
  "accountRelationship": "CHILD",
  "linkType": "ILP_OVER_HTTP",
  "assetCode": "USD",
  "assetScale": "4",
  "customSettings": {
        "ilpOverHttp.incoming.auth_type": "SIMPLE",
        "ilpOverHttp.incoming.simple.auth_token": "shh",
        "ilpOverHttp.outgoing.auth_type": "SIMPLE",
        "ilpOverHttp.outgoing.simple.auth_token": "shh",
        "ilpOverHttp.outgoing.url": "https://money.ilpv4.dev/ilp"
   }
}'
```


### Java SPSP Link
To create the `lt-spsp-java` account, execute the following command:

```text
curl --location --request POST 'https://jc.ilpv4.dev/accounts' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
--data-raw '{
  "accountId": "lt-spsp-java",
  "accountRelationship": "CHILD",
  "linkType": "ILP_OVER_HTTP",
  "assetCode": "XRP",
  "assetScale": "9",
  "customSettings": {
  	"ilpOverHttp.incoming.auth_type": "SIMPLE",
    "ilpOverHttp.incoming.simple.auth_token": "shh",
    "ilpOverHttp.outgoing.auth_type": "SIMPLE",
    "ilpOverHttp.outgoing.simple.auth_token": "shh",
      "ilpOverHttp.outgoing.url": "https://jc.ilpv4.dev/accounts/lt-spsp-java/ilp"
  }
}'
```

### Rust SPSP Link
To create the `lt-spsp-rs` account, execute the follwoing command:

```text
curl --location --request POST 'https://jc.ilpv4.dev/accounts' \
--header 'Content-Type: application/json' \
--header 'Accept: application/json' \
--header 'Authorization: Basic YWRtaW46cGFzc3dvcmQ=' \
--data-raw '{
  "accountId": "lt-spsp-rs",
  "accountRelationship": "CHILD",
  "linkType": "ILP_OVER_HTTP",
  "assetCode": "XRP",
  "assetScale": "9",
  "customSettings": {
  	"ilpOverHttp.incoming.auth_type": "SIMPLE",
    "ilpOverHttp.incoming.simple.auth_token": "shh",
    "ilpOverHttp.outgoing.auth_type": "SIMPLE",
    "ilpOverHttp.outgoing.simple.auth_token": "shh",
    "ilpOverHttp.outgoing.url": "https://jc.ilpv4.dev/accounts/lt-spsp-rs/ilp"
  }
}'
```

## Two Connectors with ILP-over-HTTP
Coming soon.

## Four Connectors with ILP-over-HTTP
Coming soon.

# GCP Infrastructure and resources

Load tests are executed via Google Cloud Platform in a setup that largely reflects the AWS Fargate product.

The load tests are contained within a configurable Docker container called `interledger4j/ilp-performance`. This
container has an executable jar inside of it and is based on Alpine Java 8 at the time of this document being written. 
This is due to our use of the Gatling framework which is Scala based and seems to have indigestion when it comes to
later versions of Java.

## Docker container parameters
* `-e SIMULATION=<simulation name` specifies the test to be run
* `-v /path/to/results:results` mounts a volume to the location where Gatling writes reports. In GCP cases 
`/path/to/results` will likely be a GCP Storage Bucket.

More to come...

## High level overview

This description is a bottom up account of the process we're using, rather than top down.

### Cloud Function for creating VMs

We rely on a Google Cloud function that will spawn a virtual machine configured with Docker and the container we seek 
to run

```json
{
  "simulation": "SomeSimulation"
}
```

## Resources

https://medium.com/google-cloud/running-a-serverless-batch-workload-on-gcp-with-cloud-scheduler-cloud-functions-and-compute-86c2bd573f25

https://medium.com/google-cloud/manage-google-compute-engine-with-node-js-eef8e7a111b4

https://stackoverflow.com/questions/32856043/mount-google-cloud-storage-bucket-to-instance

https://medium.com/google-cloud/scheduled-mirror-sync-sftp-to-gcs-b167d0eb487a