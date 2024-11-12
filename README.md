# Realtime Balance System

## Build

* Build the docker image
    ```bash
    eval $(minikube docker-env)
    ./gradlew :account-service:bootBuildImage
    ./gradlew :transaction-service:bootBuildImage
    ```

## How to deploy

* Create a kubernetes namespace for the application
    ```bash
    kubectl create namespace rt-balance
    ```
* Deploy via helm
    ```bash
    helm install test ./helm-chart/ --namespace rt-balance
    ```
    Or this to update
    ```bash
    helm upgrade test ./helm-chart/ -n rt-balance
    ```

* Tear down
    ```bash
    helm uninstall test -n rt-balance
    ```

## Other commands

* Check what is deployed
    ```bash
    kubectl get all -n rt-balance
    ```
* Check Docker context
    ```bash
    docker context ls
    ```
* Set Docker context
    ```bash
    docker context use default
    eval $(minikube docker-env)
    ```
  
## Environment Setup

### Install required tools for local development
  ```shell
  brew install minikube
  brew install helm
  ```

### Access the application from Host
  ```shell
  minikube -n rt-balance service account-db --url
  ```
