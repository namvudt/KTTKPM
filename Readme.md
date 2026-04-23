# Terminal 1: Patient Service
cd patient_service && mvn spring-boot:run
# Terminal 2: Booking Service
cd booking_service && mvn spring-boot:run
# Terminal 3: Doctor Service
cd doctor_service && mvn spring-boot:run
# Terminal 4: API Gateway ← MỚI
cd api_gateway && mvn spring-boot:run
# Terminal 5: Frontend (restart để nhận vite config mới)
cd frontend && npm run dev