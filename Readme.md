
# Hướng Dẫn Chạy Dự Án

Tài liệu này ghi lại các lệnh thường dùng để chạy dự án ở local, build lại image, xem log và truy cập các service đã deploy lên Kubernetes.

---

## 1. Chạy local bằng Spring Boot / Vite

Nếu bạn muốn chạy từng service ở máy local để debug nhanh, mở nhiều terminal PowerShell và chạy từng lệnh sau:

```powershell
# Terminal 1: Patient Service  (port 8080)
cd patient_service && mvn spring-boot:run

# Terminal 2: Booking Service  (port 8081)
cd booking_service && mvn spring-boot:run

# Terminal 3: Doctor Service   (port 8082)
cd doctor_service && mvn spring-boot:run

# Terminal 4: API Gateway      (port 9000)
cd api_gateway && mvn spring-boot:run

# Terminal 5: Frontend
cd frontend && npm run dev
```

---

## 2. Kiểm tra trạng thái pod trên Kubernetes

```powershell
kubectl get pods -n healthcare
```

---

## 3. Build lại image và restart deployment trên Kubernetes

Khi bạn sửa code và muốn cập nhật lên Kubernetes, build lại Docker image rồi restart deployment để pod kéo image mới.

### Patient Service

```powershell
docker build -t patient-service:1.0 ./patient_service
kubectl rollout restart deployment patient-service -n healthcare
```

### Doctor Service

```powershell
docker build -t doctor-service:1.0 ./doctor_service
kubectl rollout restart deployment doctor-service -n healthcare
```

### Booking Service

```powershell
docker build -t booking-service:1.0 ./booking_service
kubectl rollout restart deployment booking-service -n healthcare
```

### API Gateway

```powershell
docker build -t api-gateway:1.0 ./api_gateway
kubectl rollout restart deployment api-gateway -n healthcare
```

### Frontend

```powershell
docker build -t frontend:1.0 ./frontend
kubectl rollout restart deployment frontend -n healthcare
```

---

## 4. Xem log từng service

Xem log theo deployment thay vì tên pod để không bị lỗi khi pod đổi tên.

### API Gateway

```powershell
kubectl logs -n healthcare deployment/api-gateway -f
```

### Booking Service

```powershell
kubectl logs -n healthcare deployment/booking-service -f
```

### Doctor Service

```powershell
kubectl logs -n healthcare deployment/doctor-service -f
```

### Patient Service

```powershell
kubectl logs -n healthcare deployment/patient-service -f
```

### Frontend

```powershell
kubectl logs -n healthcare deployment/frontend -f
```

### MySQL

```powershell
kubectl logs -n healthcare deployment/mysql-deployment -f
```

---

## 5. Truy cập service từ local bằng port-forward

Dùng `kubectl port-forward` để mở trực tiếp từng service trên máy local.  
Chú ý: mỗi service chạy trên **port nội bộ khác nhau** bên trong container (không phải đều là 8080).

```powershell
Start-Job { kubectl port-forward -n healthcare deployment/frontend        8080:80   }
Start-Job { kubectl port-forward -n healthcare deployment/api-gateway     8081:9000 }
Start-Job { kubectl port-forward -n healthcare deployment/patient-service 8082:8080 }
Start-Job { kubectl port-forward -n healthcare deployment/booking-service 8083:8081 }
Start-Job { kubectl port-forward -n healthcare deployment/doctor-service  8084:8082 }
```

### Cổng truy cập tương ứng

| Service | URL local | Port nội bộ |
|---|---|---|
| Frontend | `http://localhost:8080` | `80` |
| API Gateway | `http://localhost:8081` | `9000` |
| Patient Service | `http://localhost:8082` | `8080` |
| Booking Service | `http://localhost:8083` | `8081` |
| Doctor Service | `http://localhost:8084` | `8082` |

### Swagger UI

Sau khi port-forward, truy cập Swagger của từng service:

- Patient Service: `http://localhost:8082/swagger-ui/index.html`
- Booking Service: `http://localhost:8083/swagger-ui/index.html`
- Doctor Service:  `http://localhost:8084/swagger-ui/index.html`

> ⚠️ Luôn mở Swagger bằng cách nhập URL vào **tab mới** của trình duyệt, không click từ trang lỗi.

---

## 6. Xem database MySQL

### Bước 1 — Port-forward MySQL ra máy local

```powershell
Start-Job { kubectl port-forward -n healthcare deployment/mysql-deployment 3307:3306 }
```

> Dùng port `3307` thay vì `3306` để tránh xung đột với MySQL local (nếu có cài).

### Bước 2 — Kết nối bằng GUI tool (DBeaver / Workbench / TablePlus)

Tạo kết nối mới với thông tin:

| Trường | Giá trị |
|---|---|
| Host | `127.0.0.1` |
| Port | `3307` |
| Username | `root` |
| Password | `123456` |

### Bước 3 — Kết nối bằng dòng lệnh (nếu có cài MySQL client)

```powershell
mysql -h 127.0.0.1 -P 3307 -u root -p123456
```

### Các database trong hệ thống

| Database | Service |
|---|---|
| `patient_db` | Patient Service |
| `doctor_db` | Doctor Service |
| `booking_db` | Booking Service |

---

## 7. Ghi chú nhanh

- `kubectl rollout restart` chỉ restart pod, **không** build lại image.
- Nếu chỉ muốn chạy lại service đã deploy → không cần build lại image.
- Nếu sửa code → cần build lại image rồi mới restart deployment.
- Mỗi lần chạy `Start-Job` port-forward, chỉ chạy **1 job cho mỗi cổng**. Nếu bị lỗi `TIME_WAIT`, chờ vài giây rồi thử lại.
- Kiểm tra job còn sống không: `Get-Job` → State phải là `Running`, không phải `Completed`.
