# Realtime Balance System ![CICD Status](https://github.com/hugogu/rt-balance/actions/workflows/build-and-test.yml/badge.svg)

You may need to refer to the [Development Environment](./docs/DevelopmentEnvironment.md) for setting up the local environment.

## Build

Having docker & minikube running is a prerequisite for building the docker image.

* Build the docker image. 
    ```bash
    eval $(minikube docker-env)
    ./gradlew :account-service:bootBuildImage
    ./gradlew :transaction-service:bootBuildImage
    ```

## Deploy

* Deploy via helm
    ```bash
    helm install test ./helm-chart/ --namespace default
    ```
    Or this to update
    ```bash
    helm upgrade test ./helm-chart/ --recreate-pods -n default 
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
Please refer to the [load-testing](./load-testing/README.md) for more details.

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

### Access the application from Host
  ```shell
  minikube -n rt-balance service account-db --url
  ```

## Key References

* [Kubernetes Metrics Server](https://github.com/kubernetes-sigs/metrics-server/tree/master/charts/metrics-server)
