import { renderSidebar } from './sidebar.js';
import API from '../api.js';

let bookingState = {
    step: 1,
    date: '',
    timeSlotId: null,
    timeSlotName: '',
    doctorId: null,
    doctorName: '',
    doctorSpecialization: '',
    description: '',
    createdTicket: null,   // phiếu đã tạo (trạng thái NEW) – null nếu chưa tạo
};

export function renderBooking(app, navigate, patient, logout) {
    // Reset state
    bookingState = {
        step: 1, date: '', timeSlotId: null, timeSlotName: '',
        doctorId: null, doctorName: '', doctorSpecialization: '',
        description: '', createdTicket: null,
    };
    renderBookingPage(app, navigate, patient, logout);
}

async function renderBookingPage(app, navigate, patient, logout) {
    app.innerHTML = `
        <div class="layout">
            ${renderSidebar('booking', patient, logout)}
            <div class="main-content fade-in">
                <div class="page-header">
                    <h1>📅 Đặt lịch khám</h1>
                    <p>Chọn ca khám, bác sĩ và hoàn tất đặt lịch</p>
                </div>

                <div class="steps-indicator">
                    <div class="step ${bookingState.step >= 1 ? (bookingState.step > 1 ? 'completed' : 'active') : ''}">
                        <span class="step-number">${bookingState.step > 1 ? '✓' : '1'}</span>
                        Chọn ngày & ca
                    </div>
                    <div class="step ${bookingState.step >= 2 ? (bookingState.step > 2 ? 'completed' : 'active') : ''}">
                        <span class="step-number">${bookingState.step > 2 ? '✓' : '2'}</span>
                        Chọn bác sĩ
                    </div>
                    <div class="step ${bookingState.step >= 3 ? (bookingState.step > 3 ? 'completed' : 'active') : ''}">
                        <span class="step-number">${bookingState.step > 3 ? '✓' : '3'}</span>
                        Xác nhận thông tin
                    </div>
                    <div class="step ${bookingState.step >= 4 ? 'active' : ''}">
                        <span class="step-number">4</span>
                        Đặt lịch
                    </div>
                </div>

                <div class="booking-form-card" id="step-content">
                    <div class="loading"><div class="spinner"></div> Đang tải...</div>
                </div>
            </div>
        </div>
    `;

    bindSidebar(navigate, logout);
    await renderStep(navigate, patient, logout);
}

async function renderStep(navigate, patient, logout) {
    const container = document.getElementById('step-content');

    switch (bookingState.step) {
        case 1: await renderStep1(container); break;
        case 2: await renderStep2(container); break;
        case 3: renderStep3(container, patient, navigate, logout); break;
        case 4: renderStep4(container, patient, navigate, logout); break;
    }
}

// ──────────────────────────────────────────────────────────
// STEP 1: Chọn ngày & ca khám
// ──────────────────────────────────────────────────────────
async function renderStep1(container) {
    const today = new Date().toISOString().split('T')[0];

    let timeSlots = [];
    try {
        timeSlots = await API.getTimeSlots();
    } catch (e) {
        container.innerHTML = '<div class="alert alert-error">Không thể tải danh sách ca khám. Vui lòng kiểm tra Doctor Service (port 8082).</div>';
        return;
    }

    container.innerHTML = `
        <h2>Bước 1: Chọn ngày và ca khám</h2>
        <div class="form-group">
            <label>Ngày khám</label>
            <input type="date" id="booking-date" min="${today}" value="${bookingState.date || today}" />
        </div>
        <div class="form-group">
            <label>Ca khám</label>
            <div class="options-grid" id="timeslot-options">
                ${timeSlots.map(ts => `
                    <div class="option-card ${bookingState.timeSlotId === ts.id ? 'selected' : ''}" data-id="${ts.id}" data-name="${ts.name}">
                        <div class="option-title">🕐 ${ts.name}</div>
                        <div class="option-detail">${ts.startTime} — ${ts.endTime}</div>
                    </div>
                `).join('')}
            </div>
        </div>
        <div id="step1-alert"></div>
        <button class="btn btn-primary" id="next-step1">Tiếp theo →</button>
    `;

    // Select time slot
    document.querySelectorAll('#timeslot-options .option-card').forEach(card => {
        card.addEventListener('click', () => {
            document.querySelectorAll('#timeslot-options .option-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            bookingState.timeSlotId = parseInt(card.dataset.id);
            bookingState.timeSlotName = card.dataset.name;
        });
    });

    document.getElementById('next-step1').addEventListener('click', () => {
        bookingState.date = document.getElementById('booking-date').value;
        if (!bookingState.date) {
            document.getElementById('step1-alert').innerHTML = '<div class="alert alert-error">Vui lòng chọn ngày khám</div>';
            return;
        }
        if (!bookingState.timeSlotId) {
            document.getElementById('step1-alert').innerHTML = '<div class="alert alert-error">Vui lòng chọn ca khám</div>';
            return;
        }
        bookingState.step = 2;
        renderStep(null, null, null);
    });
}

// ──────────────────────────────────────────────────────────
// STEP 2: Chọn bác sĩ
// ──────────────────────────────────────────────────────────
async function renderStep2(container) {
    container.innerHTML = '<div class="loading"><div class="spinner"></div> Đang tải danh sách bác sĩ...</div>';

    let doctors = [];
    let bookedIds = [];
    try {
        [doctors, bookedIds] = await Promise.all([
            API.getDoctorsByTimeSlot(bookingState.timeSlotId),
            API.getBookedDoctorIds(bookingState.timeSlotId, bookingState.date),
        ]);
    } catch (e) {
        container.innerHTML = '<div class="alert alert-error">Không thể tải danh sách bác sĩ</div>';
        return;
    }

    // Filter out doctors that already have an APPROVED booking on this date+timeslot
    const bookedSet = new Set(bookedIds);
    const availableDoctors = doctors.filter(doc => !bookedSet.has(doc.id));

    if (availableDoctors.length === 0) {
        container.innerHTML = `
            <h2>Bước 2: Chọn bác sĩ</h2>
            <div class="empty-state">
                <span class="empty-icon">👨‍⚕️</span>
                <p>Không còn bác sĩ nào khả dụng trong <strong>${bookingState.timeSlotName}</strong> ngày <strong>${bookingState.date}</strong></p>
                <p style="font-size:13px;margin-top:8px">Vui lòng chọn ngày hoặc ca khám khác.</p>
            </div>
            <button class="btn btn-outline" id="back-step2">← Quay lại</button>
        `;
        document.getElementById('back-step2').addEventListener('click', () => {
            bookingState.step = 1;
            renderStep(null, null, null);
        });
        return;
    }

    container.innerHTML = `
        <h2>Bước 2: Chọn bác sĩ — ${bookingState.timeSlotName}</h2>
        <div class="options-grid" id="doctor-options">
            ${availableDoctors.map(doc => `
                <div class="option-card ${bookingState.doctorId === doc.id ? 'selected' : ''}" data-id="${doc.id}" data-name="${doc.name}" data-spec="${doc.specialization}">
                    <div class="option-title">👨‍⚕️ ${doc.name}</div>
                    <div class="option-detail">${doc.specialization}</div>
                    <div class="option-detail">Mã: ${doc.code}</div>
                </div>
            `).join('')}
        </div>
        <div id="step2-alert"></div>
        <div style="display:flex;gap:12px;margin-top:20px">
            <button class="btn btn-outline" id="back-step2">← Quay lại</button>
            <button class="btn btn-primary" id="next-step2">Tiếp theo →</button>
        </div>
    `;

    document.querySelectorAll('#doctor-options .option-card').forEach(card => {
        card.addEventListener('click', () => {
            document.querySelectorAll('#doctor-options .option-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            bookingState.doctorId = parseInt(card.dataset.id);
            bookingState.doctorName = card.dataset.name;
            bookingState.doctorSpecialization = card.dataset.spec;
        });
    });

    document.getElementById('back-step2').addEventListener('click', () => {
        bookingState.step = 1;
        renderStep(null, null, null);
    });

    document.getElementById('next-step2').addEventListener('click', () => {
        if (!bookingState.doctorId) {
            document.getElementById('step2-alert').innerHTML = '<div class="alert alert-error">Vui lòng chọn bác sĩ</div>';
            return;
        }
        bookingState.step = 3;
        renderStep(null, null, null);
    });
}

// ──────────────────────────────────────────────────────────
// STEP 3: Xác nhận thông tin & Tạo phiếu (NEW)
//   - Hiển thị tóm tắt + ô nhập triệu chứng
//   - Nút "Xác nhận" → gọi API tạo phiếu (hoặc update nếu đã có)
//   - Nút "Hủy" → không tạo gì, quay về Step 1
// ──────────────────────────────────────────────────────────
function renderStep3(container, patient, navigate, logout) {
    const p = patient || JSON.parse(localStorage.getItem('patient'));

    container.innerHTML = `
        <h2>Bước 3: Xác nhận thông tin</h2>
        <div class="form-group">
            <label>Mô tả triệu chứng</label>
            <textarea id="booking-desc" placeholder="Mô tả ngắn gọn triệu chứng hoặc lý do khám...">${bookingState.description}</textarea>
        </div>

        <div class="table-card" style="margin-bottom:24px">
            <table>
                <tr><th style="width:160px">Bệnh nhân</th><td>${p.ten} (ID: ${p.id})</td></tr>
                <tr><th>Số điện thoại</th><td>${p.SDT || p.sdt || 'N/A'}</td></tr>
                <tr><th>Ngày khám</th><td>${bookingState.date}</td></tr>
                <tr><th>Ca khám</th><td>${bookingState.timeSlotName}</td></tr>
                <tr><th>Bác sĩ</th><td>${bookingState.doctorName} — ${bookingState.doctorSpecialization}</td></tr>
            </table>
        </div>

        <div id="step3-alert"></div>

        <div style="display:flex;gap:12px">
            <button class="btn btn-outline" id="back-step3">← Quay lại</button>
            <button class="btn btn-danger" id="cancel-step3">✕ Hủy</button>
            <button class="btn btn-primary" id="submit-step3">✓ Xác nhận tạo phiếu</button>
        </div>
    `;

    // Quay lại Step 2
    document.getElementById('back-step3').addEventListener('click', () => {
        bookingState.description = document.getElementById('booking-desc').value;
        bookingState.step = 2;
        renderStep(navigate, p, logout);
    });

    // Hủy → reset hoàn toàn, không tạo phiếu
    document.getElementById('cancel-step3').addEventListener('click', () => {
        if (!confirm('Bạn có chắc muốn hủy? Thông tin đặt lịch sẽ bị xóa.')) return;
        navigate('dashboard');
    });

    // Xác nhận → tạo phiếu NEW (hoặc update nếu đang sửa phiếu cũ)
    document.getElementById('submit-step3').addEventListener('click', async () => {
        const btn = document.getElementById('submit-step3');
        btn.textContent = 'Đang xử lý...';
        btn.disabled = true;

        bookingState.description = document.getElementById('booking-desc').value;

        const payload = {
            patientId: p.id,
            doctorId: bookingState.doctorId,
            name: p.ten,
            phone: p.SDT || p.sdt || '',
            description: bookingState.description,
            date: bookingState.date,
            timeSlotId: bookingState.timeSlotId,
        };

        try {
            let ticket;
            if (bookingState.createdTicket) {
                // Đã có phiếu NEW rồi → gọi UPDATE thay vì tạo mới
                ticket = await API.updateTicket(bookingState.createdTicket.id, payload);
            } else {
                // Chưa có phiếu → tạo mới
                ticket = await API.createTicket(payload);
            }
            bookingState.createdTicket = ticket;
            bookingState.step = 4;

            // Cập nhật lại steps indicator
            updateStepsIndicator();
            renderStep(navigate, p, logout);
        } catch (err) {
            document.getElementById('step3-alert').innerHTML = `<div class="alert alert-error">${err.message}</div>`;
            btn.textContent = '✓ Xác nhận tạo phiếu';
            btn.disabled = false;
        }
    });
}

// ──────────────────────────────────────────────────────────
// STEP 4: Rà soát phiếu (NEW) → Đặt lịch (PENDING)
//   - Hiển thị thông tin phiếu kèm Mã phiếu
//   - Nút "Quay lại sửa" → quay về Step 3 để cập nhật phiếu vừa tạo
//   - Nút "Đặt lịch" → confirm ticketId → PENDING
// ──────────────────────────────────────────────────────────
function renderStep4(container, patient, navigate, logout) {
    const p = patient || JSON.parse(localStorage.getItem('patient'));
    const ticket = bookingState.createdTicket;

    container.innerHTML = `
        <h2>Bước 4: Rà soát & Đặt lịch</h2>
        <p style="color:var(--text-secondary);margin-bottom:16px">
            Phiếu khám đã được khởi tạo. Vui lòng kiểm tra lại thông tin bên dưới trước khi chính thức đặt lịch.
        </p>

        <div class="table-card" style="margin-bottom:24px">
            <table>
                <tr><th style="width:160px">Mã phiếu</th><td><strong>#${ticket.id}</strong></td></tr>
                <tr><th>Trạng thái</th><td><span class="badge badge-new">Mới (NEW)</span></td></tr>
                <tr><th>Bệnh nhân</th><td>${ticket.name} (ID: ${ticket.patientId})</td></tr>
                <tr><th>Số điện thoại</th><td>${ticket.phone || 'N/A'}</td></tr>
                <tr><th>Ngày khám</th><td>${ticket.date}</td></tr>
                <tr><th>Ca khám</th><td>${bookingState.timeSlotName}</td></tr>
                <tr><th>Bác sĩ</th><td>${bookingState.doctorName} — ${bookingState.doctorSpecialization}</td></tr>
                <tr><th>Triệu chứng</th><td>${ticket.description || '—'}</td></tr>
            </table>
        </div>

        <div id="step4-alert"></div>

        <div style="display:flex;gap:12px">
            <button class="btn btn-outline" id="back-step4">← Quay lại sửa thông tin</button>
            <button class="btn btn-primary" id="confirm-step4">📋 Chính thức đặt lịch</button>
        </div>
    `;

    // Quay lại Step 3 để sửa thông tin trên chính phiếu NEW đã tạo
    document.getElementById('back-step4').addEventListener('click', () => {
        bookingState.step = 3;
        updateStepsIndicator();
        renderStep(navigate, p, logout);
    });

    // Chính thức đặt lịch: NEW → PENDING
    document.getElementById('confirm-step4').addEventListener('click', async () => {
        const btn = document.getElementById('confirm-step4');
        btn.textContent = 'Đang xử lý...';
        btn.disabled = true;

        try {
            const confirmedTicket = await API.confirmTicket(ticket.id);

            container.innerHTML = `
                <div style="text-align:center;padding:40px 0">
                    <span style="font-size:64px;display:block;margin-bottom:16px">✅</span>
                    <h2 style="margin-bottom:8px">Đặt lịch thành công!</h2>
                    <p style="color:var(--text-secondary);margin-bottom:8px">Mã phiếu khám: <strong>#${confirmedTicket.id}</strong></p>
                    <p style="color:var(--text-secondary);margin-bottom:24px">Trạng thái: <span class="badge badge-pending">${confirmedTicket.state}</span></p>
                    <div style="display:flex;gap:12px;justify-content:center">
                        <button class="btn btn-outline" id="go-history">Xem lịch sử</button>
                        <button class="btn btn-primary" id="go-dashboard">Về trang chủ</button>
                    </div>
                </div>
            `;
            document.getElementById('go-history').addEventListener('click', () => navigate('history'));
            document.getElementById('go-dashboard').addEventListener('click', () => navigate('dashboard'));
        } catch (err) {
            document.getElementById('step4-alert').innerHTML = `<div class="alert alert-error">${err.message}</div>`;
            btn.textContent = '📋 Chính thức đặt lịch';
            btn.disabled = false;
        }
    });
}

// ──────────────────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────────────────
function updateStepsIndicator() {
    const steps = document.querySelectorAll('.steps-indicator .step');
    if (!steps.length) return;

    steps.forEach((stepEl, idx) => {
        const stepNum = idx + 1;
        stepEl.classList.remove('active', 'completed');
        const numEl = stepEl.querySelector('.step-number');

        if (stepNum < bookingState.step) {
            stepEl.classList.add('completed');
            if (numEl) numEl.textContent = '✓';
        } else if (stepNum === bookingState.step) {
            stepEl.classList.add('active');
            if (numEl) numEl.textContent = String(stepNum);
        } else {
            if (numEl) numEl.textContent = String(stepNum);
        }
    });
}

function bindSidebar(navigate, logout) {
    document.querySelectorAll('[data-nav]').forEach(el => {
        el.addEventListener('click', () => navigate(el.dataset.nav));
    });
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) logoutBtn.addEventListener('click', logout);
}
