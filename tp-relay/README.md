# ThirdPlace Relay

This directory contains the files necessary to run the ThirdPlace relay server.
It is meant to be deployed to a compute service that accepts docker images.
We are starting off with [Akash](https://akash.network/).

## Running

### Build & Run local

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
The writing of the folder and publishing to IPFS is done by the `publish_addr.sh` script.
This part basically outlines the steps that script takes.

The client peers rely on having the relay server's IP published to the known IPFS address.
For local development, it is recommended to install the IPFS [desktop client](https://docs.ipfs.tech/install/ipfs-desktop/), but not necessary.
You do need to install the IPFS CLI ([Kubo](https://docs.ipfs.tech/install/command-line/)).

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

## Future

The idea for this directory is to be deployed on Akash.
The deployment is described by `deploy.yaml`.
