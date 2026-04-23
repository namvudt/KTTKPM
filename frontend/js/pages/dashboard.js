import { renderSidebar } from './sidebar.js';
import API from '../api.js';

export async function renderDashboard(app, navigate, patient, logout) {
    let ticketCount = 0;
    try {
        const tickets = await API.getTicketsByPatient(patient.id);
        ticketCount = tickets.length;
    } catch (e) { /* ignore */ }

    app.innerHTML = `
        <div class="layout">
            ${renderSidebar('dashboard', patient, logout)}
            <div class="main-content fade-in">
                <div class="page-header">
                    <h1>Xin chào, ${patient.ten} 👋</h1>
                    <p>Chào mừng bạn đến với hệ thống đặt lịch khám bệnh</p>
                </div>

                <div class="stats-grid">
                    <div class="stat-card">
                        <span class="stat-icon">📋</span>
                        <div class="stat-value">${ticketCount}</div>
                        <div class="stat-label">Tổng lịch khám</div>
                    </div>
                    <div class="stat-card">
                        <span class="stat-icon">👤</span>
                        <div class="stat-value">${patient.ma || 'N/A'}</div>
                        <div class="stat-label">Mã bệnh nhân</div>
                    </div>
                    <div class="stat-card">
                        <span class="stat-icon">📞</span>
                        <div class="stat-value">${patient.SDT || patient.sdt || 'N/A'}</div>
                        <div class="stat-label">Số điện thoại</div>
                    </div>
                </div>

                <div class="quick-actions">
                    <div class="action-card" id="action-booking">
                        <span class="action-icon">📅</span>
                        <div class="action-title">Đặt lịch khám</div>
                        <div class="action-desc">Chọn ca khám, bác sĩ và đặt lịch hẹn mới</div>
                    </div>
                    <div class="action-card" id="action-history">
                        <span class="action-icon">📜</span>
                        <div class="action-title">Xem lịch sử</div>
                        <div class="action-desc">Xem tất cả các lịch khám đã đặt trước đó</div>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.getElementById('action-booking').addEventListener('click', () => navigate('booking'));
    document.getElementById('action-history').addEventListener('click', () => navigate('history'));
    bindSidebar(navigate, logout);
}

function bindSidebar(navigate, logout) {
    document.querySelectorAll('[data-nav]').forEach(el => {
        el.addEventListener('click', () => navigate(el.dataset.nav));
    });
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) logoutBtn.addEventListener('click', logout);
}
