/**
 * API client — all calls go through Vite proxy (/api/*) to avoid CORS.
 */
const API = {
    // ─── Patient Service ────────────────────────────────────────────────────
    async getPatient(id) {
        const res = await fetch(`/api/patients/${id}`);
        if (!res.ok) throw new Error('Không tìm thấy bệnh nhân');
        return res.json();
    },

    async getPatientByCode(ma) {
        const res = await fetch(`/api/patients/code/${ma}`);
        if (!res.ok) throw new Error('Không tìm thấy bệnh nhân với mã: ' + ma);
        return res.json();
    },

    // ─── Doctor Service ─────────────────────────────────────────────────────
    async getTimeSlots() {
        const res = await fetch('/api/timeslots');
        if (!res.ok) throw new Error('Không thể tải danh sách ca khám');
        return res.json();
    },

    async getDoctorsByTimeSlot(timeSlotId) {
        const res = await fetch(`/api/doctors/by-timeslot/${timeSlotId}`);
        if (!res.ok) throw new Error('Không thể tải danh sách bác sĩ');
        return res.json();
    },

    async getBookedDoctorIds(timeSlotId, date) {
        const res = await fetch(`/api/tickets/booked-doctors?timeSlotId=${timeSlotId}&date=${date}`);
        if (!res.ok) return [];
        return res.json();
    },

    // ─── Booking Service ────────────────────────────────────────────────────
    async createTicket(data) {
        const res = await fetch('/api/tickets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Không thể tạo lịch khám');
        }
        return res.json();
    },

    async getTicketsByPatient(patientId) {
        const res = await fetch(`/api/tickets?patientId=${patientId}`);
        if (!res.ok) throw new Error('Không thể tải lịch sử');
        return res.json();
    },

    async confirmTicket(id) {
        const res = await fetch(`/api/tickets/${id}/confirm`, { method: 'POST' });
        if (!res.ok) { const err = await res.json(); throw new Error(err.message); }
        return res.json();
    },

    async cancelTicket(id) {
        const res = await fetch(`/api/tickets/${id}/cancel`, { method: 'POST' });
        if (!res.ok) { const err = await res.json(); throw new Error(err.message); }
        return res.json();
    },

    async updateTicket(id, data) {
        const res = await fetch(`/api/tickets/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Không thể cập nhật ticket');
        }
        return res.json();
    },
};

export default API;
