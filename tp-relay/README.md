# ThirdPlace Relay

This directory contains the files necessary to run the ThirdPlace relay server.
It is meant to be deployed to a compute service that accepts docker images.
We are starting off with [Akash](https://akash.network/).

## Build & Run local

```sh
$ docker build -t tp-relay .
$ docker run -p 9090:9090 tp-relay
```

Or you can use the `build_and_run.sh` file to do both of these

```sh
$ sh build_and_run.sh
```

## Build & Deploy

```sh
$ docker tag tp-relay yourdockerhub/tp-relay
$ docker push yourdockerhub/tp-relay
```
