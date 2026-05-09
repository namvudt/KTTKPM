import { renderSidebar } from './sidebar.js';
import API from '../api.js';

export async function renderHistory(app, navigate, patient, logout) {
    app.innerHTML = `
        <div class="layout">
            ${renderSidebar('history', patient, logout)}
            <div class="main-content fade-in">
                <div class="page-header">
                    <h1>📜 Lịch sử khám bệnh</h1>
                    <p>Tất cả các lịch khám đã đặt</p>
                </div>
                <div id="history-alert"></div>
                <div id="history-content">
                    <div class="loading"><div class="spinner"></div> Đang tải...</div>
                </div>
            </div>
        </div>
    `;

    bindSidebar(navigate, logout);
    await loadHistory(patient, navigate, logout);
}

async function loadHistory(patient, navigate, logout) {
    const container = document.getElementById('history-content');

    try {
        const tickets = await API.getTicketsByPatient(patient.id);

        if (tickets.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <span class="empty-icon">📋</span>
                    <p>Bạn chưa đặt lịch khám nào</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="table-card">
                <table>
                    <thead>
                        <tr>
                            <th>Mã phiếu</th>
                            <th>Ngày khám</th>
                            <th>Mô tả</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${tickets.map(t => `
                            <tr>
                                <td><strong>#${t.id}</strong></td>
                                <td>${t.date}</td>
                                <td>${t.description || '—'}</td>
                                <td>${renderBadge(t.state)}</td>
                                <td>${renderActions(t)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;

        // Bind action buttons
        document.querySelectorAll('[data-action]').forEach(btn => {
            btn.addEventListener('click', async () => {
                const ticketId = btn.dataset.ticketId;
                const action = btn.dataset.action;

                if (action === 'view') {
                    const ticket = tickets.find(t => t.id == ticketId);
                    showDetailModal(ticket);
                    return;
                }

                if (action === 'edit') {
                    const ticket = tickets.find(t => t.id == ticketId);
                    showEditForm(ticket, patient, navigate, logout);
                    return;
                }

                btn.disabled = true;
                btn.textContent = '...';

                try {
                    if (action === 'confirm') await API.confirmTicket(ticketId);
                    if (action === 'cancel') await API.cancelTicket(ticketId);
                    await loadHistory(patient, navigate, logout);
                } catch (err) {
                    const alertBox = document.getElementById('history-alert');
                    alertBox.innerHTML = `<div class="alert alert-error">${err.message}</div>`;
                    btn.disabled = false;
                }
            });
        });

    } catch (e) {
        container.innerHTML = `<div class="alert alert-error">Không thể tải lịch sử: ${e.message}</div>`;
    }
}

async function showEditForm(ticket, patient, navigate, logout) {
    const container = document.getElementById('history-content');
    const alertBox = document.getElementById('history-alert');
    alertBox.innerHTML = '';

    // Load time slots and doctors
    let timeSlots = [];
    try {
        timeSlots = await API.getTimeSlots();
    } catch (e) {
        alertBox.innerHTML = '<div class="alert alert-error">Không thể tải ca khám</div>';
        return;
    }

    container.innerHTML = `
        <div class="booking-form-card">
            <h2>✏️ Sửa phiếu khám #${ticket.id}</h2>
            <div class="form-row">
                <div class="form-group">
                    <label>Ngày khám</label>
                    <input type="date" id="edit-date" value="${ticket.date}" />
                </div>
                <div class="form-group">
                    <label>Ca khám</label>
                    <select id="edit-timeslot">
                        ${timeSlots.map(ts => `
                            <option value="${ts.id}" ${ts.id == ticket.timeSlotId ? 'selected' : ''}>
                                ${ts.name} (${ts.startTime} - ${ts.endTime})
                            </option>
                        `).join('')}
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label>Bác sĩ</label>
                <div id="doctor-select-area">
                    <div class="loading"><div class="spinner"></div> Đang tải bác sĩ...</div>
                </div>
            </div>
            <div class="form-group">
                <label>Mô tả triệu chứng</label>
                <textarea id="edit-desc">${ticket.description || ''}</textarea>
            </div>
            <div id="edit-alert"></div>
            <div style="display:flex;gap:12px">
                <button class="btn btn-outline" id="edit-cancel">← Quay lại</button>
                <button class="btn btn-primary" id="edit-save">💾 Lưu thay đổi</button>
            </div>
        </div>
    `;

    let selectedDoctorId = ticket.doctorId;

    // Load doctors for current time slot
    async function loadDoctors(timeSlotId) {
        const area = document.getElementById('doctor-select-area');
        area.innerHTML = '<div class="loading"><div class="spinner"></div></div>';
        try {
            const doctors = await API.getDoctorsByTimeSlot(timeSlotId);
            area.innerHTML = `
                <div class="options-grid">
                    ${doctors.map(doc => `
                        <div class="option-card ${doc.id == selectedDoctorId ? 'selected' : ''}" data-doc-id="${doc.id}">
                            <div class="option-title">👨‍⚕️ ${doc.name}</div>
                            <div class="option-detail">${doc.specialization}</div>
                        </div>
                    `).join('')}
                </div>
            `;
            area.querySelectorAll('.option-card').forEach(card => {
                card.addEventListener('click', () => {
                    area.querySelectorAll('.option-card').forEach(c => c.classList.remove('selected'));
                    card.classList.add('selected');
                    selectedDoctorId = parseInt(card.dataset.docId);
                });
            });
        } catch (e) {
            area.innerHTML = '<div class="alert alert-error">Không thể tải bác sĩ</div>';
        }
    }

    await loadDoctors(ticket.timeSlotId);

    // Reload doctors when timeslot changes
    document.getElementById('edit-timeslot').addEventListener('change', async (e) => {
        selectedDoctorId = null;
        await loadDoctors(e.target.value);
    });

    // Cancel
    document.getElementById('edit-cancel').addEventListener('click', () => {
        loadHistory(patient, navigate, logout);
    });

    // Save
    document.getElementById('edit-save').addEventListener('click', async () => {
        const btn = document.getElementById('edit-save');
        const editAlert = document.getElementById('edit-alert');

        if (!selectedDoctorId) {
            editAlert.innerHTML = '<div class="alert alert-error">Vui lòng chọn bác sĩ</div>';
            return;
        }

        btn.textContent = 'Đang lưu...';
        btn.disabled = true;

        try {
            await API.updateTicket(ticket.id, {
                patientId: patient.id,
                doctorId: selectedDoctorId,
                name: patient.ten,
                phone: patient.SDT || patient.sdt || '',
                description: document.getElementById('edit-desc').value,
                date: document.getElementById('edit-date').value,
                timeSlotId: parseInt(document.getElementById('edit-timeslot').value),
            });

            alertBox.innerHTML = '<div class="alert alert-success">✅ Cập nhật thành công!</div>';
            await loadHistory(patient, navigate, logout);
        } catch (err) {
            editAlert.innerHTML = `<div class="alert alert-error">${err.message}</div>`;
            btn.textContent = '💾 Lưu thay đổi';
            btn.disabled = false;
        }
    });
}

function renderBadge(state) {
    const map = {
        'NEW': '<span class="badge badge-new">Mới</span>',
        'PENDING': '<span class="badge badge-pending">Chờ duyệt</span>',
        'APPROVED': '<span class="badge badge-approved">Đã duyệt</span>',
        'REJECTED': '<span class="badge badge-rejected">Đã hủy</span>',
    };
    return map[state] || `<span class="badge">${state}</span>`;
}

function renderActions(ticket) {
    const viewBtn = `<button class="btn btn-sm btn-outline" data-action="view" data-ticket-id="${ticket.id}"> Xem</button>`;

    if (ticket.state === 'NEW') {
        return `
            ${viewBtn}
            <button class="btn btn-sm btn-info" data-action="edit" data-ticket-id="${ticket.id}">✏️ Sửa</button>
            <button class="btn btn-sm btn-success" data-action="confirm" data-ticket-id="${ticket.id}">Xác nhận</button>
            <button class="btn btn-sm btn-danger" data-action="cancel" data-ticket-id="${ticket.id}">Hủy</button>
        `;
    }
    if (ticket.state === 'PENDING') {
        return viewBtn;
    }
    return viewBtn;
}

function showDetailModal(ticket) {
    const existing = document.getElementById('ticket-modal');
    if (existing) existing.remove();

    const modal = document.createElement('div');
    modal.id = 'ticket-modal';
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-card">
            <div class="modal-header">
                <h2>📋 Chi tiết phiếu khám #${ticket.id}</h2>
                <button class="modal-close" id="modal-close-btn">✕</button>
            </div>
            <div class="modal-body">
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="detail-label">Mã phiếu</span>
                        <span class="detail-value">#${ticket.id}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Trạng thái</span>
                        <span class="detail-value">${renderBadge(ticket.state)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Họ tên</span>
                        <span class="detail-value">${ticket.name || '—'}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Số điện thoại</span>
                        <span class="detail-value">${ticket.phone || '—'}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ngày khám</span>
                        <span class="detail-value">📅 ${ticket.date}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Ca khám (ID)</span>
                        <span class="detail-value">Ca #${ticket.timeSlotId || '—'}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">Bác sĩ (ID)</span>
                        <span class="detail-value">👨‍⚕️ Bác sĩ #${ticket.doctorId || '—'}</span>
                    </div>
                    <div class="detail-item detail-item-full">
                        <span class="detail-label">Mô tả triệu chứng</span>
                        <span class="detail-value">${ticket.description || '—'}</span>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    requestAnimationFrame(() => modal.classList.add('visible'));

    document.getElementById('modal-close-btn').addEventListener('click', () => {
        modal.classList.remove('visible');
        setTimeout(() => modal.remove(), 250);
    });
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('visible');
            setTimeout(() => modal.remove(), 250);
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
