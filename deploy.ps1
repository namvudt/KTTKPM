# ============================================
# Deploy Healthcare System to Kubernetes
# ============================================
# Yêu cầu: Docker Desktop + Kubernetes đã bật
# Chạy: .\deploy.ps1
# ============================================

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Healthcare Microservices - K8s Deployment"  -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# --- Step 1: Build Docker Images ---
Write-Host "[1/3] Building Docker images..." -ForegroundColor Yellow

Write-Host "  -> Building patient-service:1.0" -ForegroundColor Gray
docker build -t patient-service:1.0 ./patient_service
if ($LASTEXITCODE -ne 0) { Write-Host "FAILED: patient-service" -ForegroundColor Red; exit 1 }

Write-Host "  -> Building doctor-service:1.0" -ForegroundColor Gray
docker build -t doctor-service:1.0 ./doctor_service
if ($LASTEXITCODE -ne 0) { Write-Host "FAILED: doctor-service" -ForegroundColor Red; exit 1 }

Write-Host "  -> Building booking-service:1.0" -ForegroundColor Gray
docker build -t booking-service:1.0 ./booking_service
if ($LASTEXITCODE -ne 0) { Write-Host "FAILED: booking-service" -ForegroundColor Red; exit 1 }

Write-Host "  -> Building api-gateway:1.0" -ForegroundColor Gray
docker build -t api-gateway:1.0 ./api_gateway
if ($LASTEXITCODE -ne 0) { Write-Host "FAILED: api-gateway" -ForegroundColor Red; exit 1 }

Write-Host "  -> Building frontend:1.0" -ForegroundColor Gray
docker build -t frontend:1.0 ./frontend
if ($LASTEXITCODE -ne 0) { Write-Host "FAILED: frontend" -ForegroundColor Red; exit 1 }

Write-Host "[1/3] All images built successfully!" -ForegroundColor Green
Write-Host ""

# --- Step 2: Apply K8s Manifests ---
Write-Host "[2/3] Applying Kubernetes manifests..." -ForegroundColor Yellow

Write-Host "  -> Creating namespace" -ForegroundColor Gray
kubectl apply -f ./k8s/namespace.yaml

Write-Host "  -> Deploying MySQL" -ForegroundColor Gray
kubectl apply -f ./k8s/mysql.yaml

Write-Host "  -> Waiting for MySQL to be ready (30s)..." -ForegroundColor Gray
Start-Sleep -Seconds 30

Write-Host "  -> Deploying Patient Service" -ForegroundColor Gray
kubectl apply -f ./k8s/patient-service.yaml

Write-Host "  -> Deploying Doctor Service" -ForegroundColor Gray
kubectl apply -f ./k8s/doctor-service.yaml

Write-Host "  -> Deploying Booking Service" -ForegroundColor Gray
kubectl apply -f ./k8s/booking-service.yaml

Write-Host "  -> Deploying API Gateway" -ForegroundColor Gray
kubectl apply -f ./k8s/api-gateway.yaml

Write-Host "  -> Deploying Frontend" -ForegroundColor Gray
kubectl apply -f ./k8s/frontend.yaml

Write-Host "[2/3] All manifests applied!" -ForegroundColor Green
Write-Host ""

# --- Step 3: Check Status ---
Write-Host "[3/3] Checking pod status..." -ForegroundColor Yellow
Start-Sleep -Seconds 10
kubectl get pods -n healthcare -o wide

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Deployment complete!" -ForegroundColor Green
Write-Host "  Frontend:  http://localhost:30000" -ForegroundColor White
Write-Host "  Check pods: kubectl get pods -n healthcare" -ForegroundColor White
Write-Host "  View logs:  kubectl logs -f <pod-name> -n healthcare" -ForegroundColor White
Write-Host "============================================" -ForegroundColor Cyan
