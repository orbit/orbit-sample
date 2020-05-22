# Orbit Sample App (The Carnival)

This is a sample app to help illustrate the concepts of an Orbit Client application. It simulates a carnival games, players, and prizes!

## Getting Started

This sample app will install Orbit Server, its depenedencies, and a client application called Carnival. 

### Prerequisites
* [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube) or [Docker for Desktop](https://docs.docker.com/get-docker/)
* [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/) Kubernetes Command Line Tools 
* [Istio](https://istio.io/docs/setup/platform-setup/) for Kubernetes routing and services
* [Skaffold](https://skaffold.dev) to easily bring up the environment
* [Insomnia](https://insomnia.rest/) or [Postman](https://www.postman.com/) for testing

### Clone the repository

```shell script
> git clone git@github.com:orbit/orbit-sample.git
```

### Set up Kubernetes environment

#### Minikube

After [installing](https://kubernetes.io/docs/tasks/tools/install-minikube) Minikube, start up a new Minikube instance:
```shell script
> minikube start
``` 

To verify your Minikube VM is working, you can use the minikube status command.
```shell script
> minikube status
m01
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```

#### Docker for Desktop
With Docker for Desktop running, open settings and enable Kubernetes.

* [Mac](https://docs.docker.com/docker-for-mac/kubernetes/)
* [Windows](https://docs.docker.com/docker-for-windows/kubernetes/)

### Enable Istio
To enable Istio in a cluster, you need to one-time apply the manifest using istioctl:

```shell script
> istioctl manifest apply
```

### Run Skaffold

```shell script
> skaffold dev --port-forward
```

To assure the proper pods are running, you can run kubectl get pod. You should see the pods for the Carnival, Orbit Server, and the Node and Addressable directories.

```shell script
> kubectl get pod
NAME                                           READY   STATUS    RESTARTS   AGE
orbit-addressable-directory-8666f4fbc6-j6lmz   1/1     Running   0          59s
orbit-carnival-5c6f59bb-kw2zr                  1/1     Running   0          59s
orbit-node-directory-bdb45ff8d-g9fjt           1/1     Running   0          59s
orbit-server-78fb97dd58-lx466                  1/1     Running   0          59s
```

Fun tip: Use the [`watch`](https://www.geeksforgeeks.org/watch-command-in-linux-with-examples/
) command to keep track of running containers:
```shell script
> watch kubectl get pod
```

### Test

The Carnival test app exposes a REST endpoint for playing the game.

| Method | Url                     | Payload
|--------|-------------------------|-----------
| GET    | /games                  |
| GET    | /game/{gameId}          |
| GET    | /player/{playerId}      |
| POST   | /player/{playerId}/play | ex. { "game": "BalloonDarts" }


One more endpoint exists for load testing by continuously playing games:

| Method | Url | Payload |
|---|---|---
| GET    | /load/play | ex. { "games": 5, "players": 4, "count": 800 }

Using Postman or Insomnia:



