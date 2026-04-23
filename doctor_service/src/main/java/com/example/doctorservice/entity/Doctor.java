package com.example.doctorservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Entity representing a Doctor record in the database.
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full name of the doctor. */
    @NotBlank(message = "Tên bác sĩ không được để trống")
    @Column(nullable = false)
    private String name;

    /** Doctor code / identifier. */
    @Column(nullable = false, unique = true)
    private String code;

    /** Phone number. */
    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(nullable = false, unique = true)
    private String phone;

    /** Specialization (e.g., "Nội khoa", "Ngoại khoa"). */
    @NotBlank(message = "Chuyên khoa không được để trống")
    @Column(nullable = false)
    private String specialization;

    /** Whether the doctor is currently available for booking. */
    @Column(nullable = false)
    private boolean available = true;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public Doctor() {}

    public Doctor(String name, String code, String phone, String specialization) {
        this.name = name;
        this.code = code;
        this.phone = phone;
        this.specialization = specialization;
        this.available = true;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Doctor{id=" + id + ", name='" + name + "', code='" + code +
               "', specialization='" + specialization + "', available=" + available + "}";
    }
}
