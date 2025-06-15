# 🧪 Newman RESTful API Runner
A lightweight Spring Boot API that accepts a Postman collection (JSON file), executes it using [Newman](https://www.npmjs.com/package/newman), and generates an HTML report that can be retrieved via a REST endpoint.

- Author: Chung-Lun LU
- Date: June 15, 2025

## 🚀 Features
- 📥 Accepts `POST` requests with a Postman collection `.json` file.
- ⚙️ Executes tests via Newman (Postman's CLI).
- 📊 Generates HTML reports with `newman-reporter-html`.
- 🌐 Serves reports via REST endpoint - provides a `GET` API to view/download the report.
- 🐳 Dockerized for deployment with Node.js and Java 17.
- 

## 📡 API Endpoints

### 🔹 `POST /v1/api/executeTest`
Upload a Postman collection JSON file and trigger test execution.

#### Request
- Content-Type: `multipart/form-data`
- Field: `file` — Postman collection file (`.json`)

#### Example with `curl`
```bash
curl -X POST http://localhost:8080/v1/api/executeTest \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/collection.json"
```

#### Response
```json
{
  "report_url": "/v1/api/report/abcd1234.html"
}
```

### 🔹 `GET /v1/api/report/{filename}`
Retrieve the generated HTML report.
#### Example
```bash
GET http://localhost:8080/v1/api/report/abcd1234.html
```
- If successful, return an HTML file as inline content.
- If not found or not .html, return appropriate status code.

## 🐳 Docker Support

### 🔧 Build Docker Image
```bash
docker build -t chunglunlu/newman-api-test:0.0.2 .
```
### ▶️ Run Docker Container
```bash
  docker run -p 8080:8080 chunglunlu/newman-api-test:0.0.2
```

## ⚙️ Configuration (application.properties)
```properties
server.port=8080
upload.folder=/tmp/uploads
report.folder=/tmp/reports
spring.resources.static-locations=classpath:/static/,file:/tmp/
```
Note: You can override these with -e flags in Docker or using environment-specific config.

## Kubernetes deploy
```bash
kubectl create namespace newman
kubectl apply -f configmap.yaml -n newman
kubectl apply -f deployment.yaml -n newman
kubectl apply -f svc.yaml -n newman
```

### Kubernetes delete
```bash
kubectl delete svc newman-svc -n newman
kubectl delete deployment newman-deployment -n newman
kubectl delete configmap newman-config -n newman
```

## 🔧 Dependencies
- Java 17 (via openjdk:17-slim)
- Node.js (via setup script)
- Newman CLI
- newman-reporter-html
- Spring Boot 3.x

## 📁 Project Structure
```
src/
  main/
    java/
      com.chunglun.sre.rest.NewmanRestfulService
    resources/
      application.properties
K8s/
  configmap.yaml
  deployment.yaml
  svc.yaml
Dockerfile
README.md
```
## 📄 Sample
![img.png](execuet.png)
![img_1.png](getReport.png)
## 👨‍💻️ Author
Developed by Chung-Lun Lu