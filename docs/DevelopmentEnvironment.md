# Development Environment

## For Mac

### System Requirements

In order to hold whole cluster in local, you'd better have 16GB of RAM and 4 CPU cores for the Docker environment.

### Basic Tools

* [Homebrew](https://brew.sh/)

### Install Docker Desktop

* [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* Install [Docker Compose](https://docs.docker.com/compose/install/) for local development and testing.
* Install [HELM](https://helm.sh/docs/) && [minikube](https://minikube.sigs.k8s.io/docs/) for k8s deployment
  ```shell
  brew install helm
  brew install minikube
  ```
  Then you can start minikube and switch k8s context to minikube
  ```shell
  minikube start
  kubectl config use-context minikube
  ```
* Install [Grafana K6](https://grafana.com/docs/k6/latest/set-up/install-k6/) for load testing
  ```shell
  brew install k6
  ```

### Install components/addons for local k8s deployment

  ```shell
  # This is required to enable HPA in kubernetes
  minikube addons enable metrics-server
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
* **[Optional]** Introduce Metrics Server in Helm.
  ```shell
  helm repo add metrics-server https://kubernetes-sigs.github.io/metrics-server/
  helm repo update
  helm install metrics-server metrics-server/metrics-server --namespace kube-system
  ```

### License Requirements

#### [KPow](https://docs.factorhouse.io/kpow-ee/about/introduction/)

It is used to manage Kafka cluster in local. [A license is required](https://factorhouse.io/kpow/community/#individual) to use it in docker-compose. 

This requirement can be omitted if you don't need it.

## For Windows

TBD
