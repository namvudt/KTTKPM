/**
 * Shared sidebar component used across all authenticated pages.
 */
export function renderSidebar(activePage, patient, logout) {
    return `
        <aside class="sidebar">
            <div class="sidebar-brand">
                <h2>🏥 HealthCare</h2>
                <p>Hệ thống đặt lịch khám</p>
            </div>
            <nav class="sidebar-nav">
                <div class="nav-item ${activePage === 'dashboard' ? 'active' : ''}" data-nav="dashboard">
                    <span class="icon">🏠</span> Trang chủ
                </div>
                <div class="nav-item ${activePage === 'booking' ? 'active' : ''}" data-nav="booking">
                    <span class="icon">📅</span> Đặt lịch khám
                </div>
                <div class="nav-item ${activePage === 'history' ? 'active' : ''}" data-nav="history">
                    <span class="icon">📜</span> Lịch sử khám
                </div>
            </nav>
            <div class="sidebar-footer">
                <div class="user-info">
                    <div class="user-avatar">${(patient.ten || 'U').charAt(0).toUpperCase()}</div>
                    <div class="user-details">
                        <div class="name">${patient.ten || 'Bệnh nhân'}</div>
                        <div class="role">ID: ${patient.id} · ${patient.ma || ''}</div>
                    </div>
                </div>
                <button class="btn btn-outline btn-full btn-sm" id="logout-btn" style="margin-top:12px">
                    Đăng xuất
                </button>
            </div>
        </aside>
    `;
}
