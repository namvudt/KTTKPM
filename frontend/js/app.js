import API from './api.js';
import { renderLogin } from './pages/login.js';
import { renderDashboard } from './pages/dashboard.js';
import { renderBooking } from './pages/booking.js';
import { renderHistory } from './pages/history.js';

const app = document.getElementById('app');

/**
 * Simple hash-based SPA router.
 */
function navigate(page) {
    window.location.hash = page;
}

function getPatient() {
    const data = localStorage.getItem('patient');
    return data ? JSON.parse(data) : null;
}

function logout() {
    localStorage.removeItem('patient');
    navigate('login');
}

function router() {
    const hash = window.location.hash.replace('#', '') || 'login';
    const patient = getPatient();

    // Guard: redirect to login if not authenticated
    if (hash !== 'login' && !patient) {
        navigate('login');
        return;
    }

    switch (hash) {
        case 'login':
            renderLogin(app, navigate);
            break;
        case 'dashboard':
            renderDashboard(app, navigate, patient, logout);
            break;
        case 'booking':
            renderBooking(app, navigate, patient, logout);
            break;
        case 'history':
            renderHistory(app, navigate, patient, logout);
            break;
        default:
            navigate('dashboard');
    }
}

// Listen for hash changes
window.addEventListener('hashchange', router);

// Initial route
router();

// Export for use in page modules
export { navigate, getPatient, logout, API };
