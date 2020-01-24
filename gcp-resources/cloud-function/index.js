const Buffer = require('safe-buffer').Buffer;
const Compute = require('@google-cloud/compute');
const compute = new Compute();

const zone = 'us-central1-a';

const gcpProjectId = 'java-connector-test';

exports.createInstance = (event, context) => {
  const vmName = `load-test-executor-${Date.now()}`;

  console.log('event', event);

  let payload = event;
  if (event['@type'] === 'type.googleapis.com/google.pubsub.v1.PubsubMessage') {
    console.log('Parsing pubsub message');
    payload = JSON.parse(Buffer.from(event.data, 'base64').toString());
  }
  console.log('Parsing payload', payload);
  const simulationDetails = payload; //JSON.parse(payload);

  if (!simulationDetails.simulation) {
    console.log('No simulation specified', simulationDetails);
    return;
  }

  const vmConfig =
    {
      "kind": "compute#instance",
      "name": vmName,
      "zone": `projects/${gcpProjectId}/zones/us-central1-a`,
      "machineType": `projects/${gcpProjectId}/zones/us-central1-a/machineTypes/n1-standard-2`,
      "displayDevice": {
        "enableDisplay": false
      },
      "metadata": {
        "kind": "compute#metadata",
        "items": [
          {
            "key": "startup-script",
            "value": `
gcloud logging write load-test-execution "Executing load test for simulation ${simulationDetails.simulation} from $(hostname)."
export GCSFUSE_REPO=gcsfuse-$(lsb_release -c -s)
echo "deb http://packages.cloud.google.com/apt $GCSFUSE_REPO main" | sudo tee /etc/apt/sources.list.d/gcsfuse.list
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
sudo mkdir -p /var/log/load-tests
sudo apt-get update
sudo apt-get install -y gcsfuse
sudo gcsfuse ilp-shenanigans-load-test-results /var/log/load-tests
sudo apt install -y apt-transport-https ca-certificates curl gnupg2 software-properties-common
curl -fsSL https://download.docker.com/linux/debian/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable"
sudo apt update
apt-cache policy docker-ce
sudo apt-get install -y docker-ce
sudo docker run -e SIMULATION=${simulationDetails.simulation} -e CONCURRENCY=${simulationDetails.concurrency} -e RAMP_UP=${simulationDetails.rampUp} -e HOLD_FOR=${simulationDetails.holdFor} -e THROUGHPUT=${simulationDetails.throughput} -v /var/log/load-tests:/results --name loadtest interledger4j/ilp-performance &> output.log
sudo docker wait loadtest
cat output.log
gcloud logging write load-test-execution "Load test execution results for ${simulationDetails.simulation} from ${vmName}: \`cat output.log\`"
gcp_zone=$(curl -H Metadata-Flavor:Google http://metadata.google.internal/computeMetadata/v1/instance/zone -s | cut -d/ -f4)
gcloud logging write load-test-execution "Shutting down vm with command: gcloud compute instances delete $(hostname) --zone \${gcp_zone}"
gcloud compute instances delete $(hostname) --zone \${gcp_zone}`
          }
        ]
      },
      "tags": {
        "items": []
      },
      "disks": [
        {
          "kind": "compute#attachedDisk",
          "type": "PERSISTENT",
          "boot": true,
          "mode": "READ_WRITE",
          "autoDelete": true,
          "deviceName": "instance-1",
          "initializeParams": {
            "sourceImage": "projects/debian-cloud/global/images/debian-9-stretch-v20191210",
            "diskType": `projects/${gcpProjectId}/zones/us-central1-a/diskTypes/pd-standard`,
            "diskSizeGb": "10"
          },
          "diskEncryptionKey": {}
        }
      ],
      "canIpForward": false,
      "networkInterfaces": [
        {
          "kind": "compute#networkInterface",
          "subnetwork": `projects/${gcpProjectId}/regions/us-central1/subnetworks/default`,
          "accessConfigs": [
            {
              "kind": "compute#accessConfig",
              "name": "External NAT",
              "type": "ONE_TO_ONE_NAT",
              "networkTier": "PREMIUM"
            }
          ],
          "aliasIpRanges": []
        }
      ],
      "description": "",
      "labels": {},
      "scheduling": {
        "preemptible": false,
        "onHostMaintenance": "MIGRATE",
        "automaticRestart": true,
        "nodeAffinities": []
      },
      "deletionProtection": false,
      "reservationAffinity": {
        "consumeReservationType": "ANY_RESERVATION"
      },
      "serviceAccounts": [
        {
          "email": `ilp-performance-self-destruct@${gcpProjectId}.iam.gserviceaccount.com`,
          "scopes": [
            "https://www.googleapis.com/auth/cloud-platform"
          ]
        }
      ]
    };

  try {
    compute.zone(zone)
      .createVM(vmName, vmConfig)
      .then(data => {
        // Operation pending.
        const vm = data[0];
        const operation = data[1];
        console.log(`VM being created: ${vm.id}`);
        console.log(`Operation info: ${operation.id}`);
        return operation.promise();
      })
      .then(() => {
        const message = 'VM created with success, Cloud Function finished execution.';
        console.log(message);
      })
      .catch(err => {
        console.log(err);
      });
  } catch (err) {
    console.log(err);
  }
};