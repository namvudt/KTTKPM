import API from '../api.js';

export function renderLogin(app, navigate) {
    app.innerHTML = `
        <div class="login-page">
            <div class="login-card">
                <span class="login-icon">🏥</span>
                <h1>Đặt lịch khám bệnh</h1>
                <p class="subtitle">Đăng nhập bằng mã bệnh nhân và số điện thoại</p>
                <div id="login-alert"></div>
                <form id="login-form">
                    <div class="form-group">
                        <label>Mã bệnh nhân</label>
                        <input type="text" id="patient-code" placeholder="VD: BN001" required />
                    </div>
                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input type="tel" id="patient-phone" placeholder="VD: 0901234567" required />
                    </div>
                    <button type="submit" class="btn btn-primary btn-full" id="login-btn">
                        Đăng nhập
                    </button>
                </form>
            </div>
        </div>
    `;

    const form = document.getElementById('login-form');
    const alertBox = document.getElementById('login-alert');
    const loginBtn = document.getElementById('login-btn');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const code = document.getElementById('patient-code').value.trim();
        const phone = document.getElementById('patient-phone').value.trim();

        if (!code || !phone) {
            alertBox.innerHTML = '<div class="alert alert-error">Vui lòng nhập đầy đủ mã và số điện thoại</div>';
            return;
        }

        loginBtn.textContent = 'Đang xác thực...';
        loginBtn.disabled = true;

        try {
            const patient = await API.getPatientByCode(code);

            // Verify phone number matches
            const patientPhone = patient.SDT || patient.sdt || '';
            if (patientPhone !== phone) {
                alertBox.innerHTML = '<div class="alert alert-error">Số điện thoại không đúng</div>';
                return;
            }

            localStorage.setItem('patient', JSON.stringify(patient));
            navigate('dashboard');
        } catch (err) {
            alertBox.innerHTML = `<div class="alert alert-error">Không tìm thấy bệnh nhân với mã: ${code}</div>`;
        } finally {
            loginBtn.textContent = 'Đăng nhập';
            loginBtn.disabled = false;
        }
    });
}
