# Realtime Balance System ![CICD Status](https://github.com/hugogu/rt-balance/actions/workflows/build-and-test.yml/badge.svg)

## Build

* Build the docker image. 
    ```bash
    eval $(minikube docker-env)
    ./gradlew :account-service:bootBuildImage
    ./gradlew :transaction-service:bootBuildImage
    ```
:warning: Ensure the docker context is set to minikube otherwise the image will not be available to the kubernetes cluster.
   * If you are running these image in local and your local is of different CPU architecture,
     you may want to build a native image for better performance by enabling native image by.
     ```shell
     BP_NATIVE_IMAGE=true
     ```

## How to deploy

* Create a kubernetes namespace for the application
    ```bash
    kubectl create namespace rt-balance
    ```
* Deploy via helm
    ```bash
    helm install test ./helm-chart/ --namespace default
    ```
    Or this to update
    ```bash
    helm upgrade test ./helm-chart/ -n default
    ```

* Tear down
    ```bash
    helm uninstall test -n default
    ```
  
## Test

### Expose the service in Helm

* Expose the service to local for testing purpose.
    ```bash
    kubectl port-forward svc/account-db 5433:5432
    kubectl port-forward svc/transaction-db 5434:5432
    kubectl port-forward svc/account-service 8080:8080
    kubectl port-forward svc/transaction-service 8081:8080
    kubectl port-forward svc/ingress-nginx-controller 8080:80
    ```
     OR using minikube if the k8s cluster was in local.
    ```bash
    minikube service account-service --url
    ```

### Stress Test

For a basic stress test that only needs a TPS number with p50,p90,p95,etc, you can use the following commands:

* Account creation stress test
   ```shell
   k6 run ./load-testing/scripts/create-account-stress.js
   ```
* Transaction stress test
   ```shell
   k6 run ./load-testing/scripts/transaction-stress.js
   ```

For advanced stress test to analyze the performance of the system, we'd need to collect the metrics and analyze them.
That would require a more complex setup, please refer to the [load-testing](./load-testing/README.md) for more details.

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
  brew install helm
  brew install minikube
  minikube start
  kubectl config use-context minikube
  # This is required to enable HPA in kubernetes
  minikube addons enable metrics-server
  minikube dashboard
  ```
* Introduce Ingress for the use in helm
  ```shell
  helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
  helm repo update
  helm install ingress-nginx ingress-nginx/ingress-nginx
  ```
* Introduce Prometheus for the use in helm
  ```shell
  helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
  helm repo update
  helm install prometheus prometheus-community/prometheus
  ```

### Install Tools for stress testing
* [Grafana K6](https://grafana.com/docs/k6/latest/set-up/install-k6/)

### Access the application from Host
  ```shell
  minikube -n rt-balance service account-db --url
  ```

## Key References

* [Kubernetes Metrics Server](https://github.com/kubernetes-sigs/metrics-server/tree/master/charts/metrics-server)
