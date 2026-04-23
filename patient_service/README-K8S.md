# Hướng Dẫn Vận Hành Dự Án Trên Kubernetes (Cập nhật Mới)

Dưới đây là cẩm nang thiết lập bài bản nhất (đã sửa lỗi Frontend kết nối). Để không bị rối, hãy bật **4 cửa sổ Terminal (Powershell) riêng biệt**.

---

### 🟢 Giai đoạn 1: Khởi động hệ thống (Terminal 1)

**1. Dọn rác môi trường cũ**
Luôn phải làm để dập tắt Docker Compose, tránh xung đột bất ngờ.
```bash
docker compose down
```

**2. Đóng gói Code thành Image (Build)**
*(Lưu ý: Cần làm bước này mỗi khi bạn chỉnh sửa code Java hoặc file `index.html` của Web).*
```bash
docker build -t patient-service:1.0 .
```

**3. Đẩy hệ thống lên K8s**
Khởi tạo lần đầu:
```bash
kubectl apply -f k8s-config.yaml
```

**🔥 TRƯỜNG HỢP BẠN VỪA SỬA CODE:** Nếu hệ thống đã chạy sẵn trước đó mà bạn vừa sửa Frontend/Backend, hãy gọi lệnh dưới đây để K8s đập bỏ bản cũ và tải luôn bản code mới (Zero Downtime):
```bash
kubectl rollout restart deployment/patient-service-deployment
```

---

### 🟢 Giai đoạn 2: Vận hành & Kết nối

**4. Mở Cổng Backend cho Web (Terminal 2)**
Code Frontend của bạn hiện đang cài cắm kết nối tới `http://localhost:18082`. Lệnh này là chiếc cầu nối luân chuyển y hệt.
```bash
kubectl port-forward svc/patient-service-service 18082:8080
```
👉 *Gõ xong có chữ `Forwarding...` thì để yên đó chạy ngầm.*

**5. Mở Cổng Database để quản lý (Terminal 3)**
Muốn sửa bằng tay dữ liệu (DataGrip/DBeaver) thì mở thêm cổng này.
```bash
kubectl port-forward svc/mysql-service 3307:3306
```
👉 *Kết nối IDE: `localhost | Port: 3307 | root | 123456 | patient_db`.*

**6. Mở Radar dò log ứng dụng (Terminal 4)**
Chế độ rình rập, bỏ qua log cũ và chỉ hiện những lỗi/thông báo vừa mới phát sinh.
```bash
kubectl logs deployment/patient-service-deployment -f --tail=0
```
👉 *Màn hình sẽ màu đen. Nhưng ngay khi lên Web truy cập "Tìm Bệnh Nhân", log sẽ bay lên đây y hệt ma trận!*
