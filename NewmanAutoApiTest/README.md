# Newman auto api test
- Author: Chung-Lun LU
- Date: June 15, 2025

## Build and Deploy
### Build the image
```shell
docker build -t chunglunlu/newman-api-test:0.0.1 .
```
### Run the image
```shell
docker run -p 8080:8080 chunglunlu/newman-api-test:0.0.1
```
### Kubernetes deploy
```shell
kubectl create namespace newman
kubectl apply -f configmap.yaml -n newman
kubectl apply -f deployment.yaml -n newman
kubectl apply -f svc.yaml -n newman
```

### Kubernetes delete
```shell
kubectl delete svc newman-svc -n newman
kubectl delete deployment newman-deployment -n newman
kubectl delete configmap newman-config -n newman
```