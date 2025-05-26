# ThirdPlace Relay

This directory contains the files necessary to run the ThirdPlace relay server.
It is meant to be deployed to a compute service that accepts docker images.
We are starting off with [Akash](https://akash.network/).

## Running

### Build & Run local

All scripts for running are found in the `/dev_scripts/` directory.
They require setting the environment variable `$TP_RELAY_ROOT` which should contain
the complete (or relative from `~`) path to this root directory.
For example, `c:\users\documents\mycode\ThirdPlace\tp-relay\` or `~/mycode/ThirdPlace/tp-relay/`.

If you get the error that it could not find the relay file after checking you have the environment variable set,
restart your editor or terminal shell. You can check if it is set and identified correctly with:

```sh
$ echo "$TP_RELAY_ROOT"

> "C:\Users\user\Documents\ThirdPlace\tp-relay"
```

```sh
$ docker build -t tp-relay .
$ docker run -p 9090:9090 tp-relay
```

Or you can use the `build_and_run.sh` file to do both of these

```sh
$ sh build_and_run.sh
```

### Build & Deploy

#### Docker

```sh
$ docker tag tp-relay yourdockerhub/tp-relay
$ docker push yourdockerhub/tp-relay
```

#### IPFS (This should be taken care of for you by the Docker scripts)

This step only applies if you are playing around with publishing the contents to IPFS.
The writing of the folder and publishing to IPFS is done by the `publish_relay.sh` script.
This part basically outlines the steps that script takes.

The client peers rely on having the relay server's IP published to the known IPFS address.
For local development, it is recommended to install the IPFS [desktop client](https://docs.ipfs.tech/install/ipfs-desktop/), but not necessary.
__You do need to install the IPFS CLI ([Kubo](https://docs.ipfs.tech/install/command-line/)).__


<details>
  <summary>Click to expand</summary>
For the first time, you need to create a key:

```sh
$ ipfs key gen --type=rsa --size=2048 tp-relay-key
```

Add the top level relay directory to IPFS. Store the content ID (CID) in a local var:

```sh
$ CID=ipfs add -r -Q ./tp-relay-ipfs
```

Pin it to local device so it doesn't dissappear into the ether:

```sh
$ ipfs pin add $CID
```

Publish it to the named key so we can reference it via static address:

```sh
$ ipfs name publish --key="tp-relay-key" "/ipfs/$CID"

> Published to [IPNS domain]: /ipfs/[CID]
```

NOTE: This step may take a bit of time as it published the content to peers.

The production instance uses the key from the official ThirdPlace account.
For local development, you will use the IPNS address for your IPFS account.
This is the [IPNS domain] from the output of the publish command.

If you want to test that your IPNS domain can be resolved, or that the contents are updated or reachable,
you can use the following commands:

- Resolve your IPNS domain

```sh
$ ipfs name resolve /ipns/[IPNS domain]

> /ipfs/[CID of top level tp-relay directory]
```

- Retrieve the contents

```sh
$ ipfs cat /ipns/[IPNS domain]/relay-addr.txt

> [contents of relay-addr.txt]
```
</details>


### What is the "tp-ipfs/" directory?

This directory is used to setup the docker image used by akash.
It contains the IPFS information used for the ThirdPlace IPFS account.
This is how we can publish to IPFS/IPNS on Akash, and also how we mount your IPFS account on the image for dev use.

## Future

The idea for this directory is to be deployed on Akash.
The deployment is described by `deploy.yaml`.
