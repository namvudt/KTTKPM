package com.example.patientservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Entity representing a Patient record in the database.
 */
@Entity
@Table(name = "patients")
public class Patient {

    /** Primary key, auto-incremented. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Full name of the patient. */
    @NotBlank(message = "Tên bệnh nhân không được để trống")
    @Column(nullable = false)
    private String ten;

    /** Patient code / identifier. */
    @Column(nullable = false, unique = true)
    private String ma;

    /** Phone number of the patient. */
    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(nullable = false,unique = true)
    @JsonProperty("SDT")
    private String SDT;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public Patient() {}

    public Patient(String ten, String ma, String SDT) {
        this.ten = ten;
        this.ma  = ma;
        this.SDT = SDT;
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getSDT() { return SDT; }
    public void setSDT(String SDT) { this.SDT = SDT; }

    @Override
    public String toString() {
        return "Patient{id=" + id + ", ten='" + ten + "', ma='" + ma + "', SDT='" + SDT + "'}";
    }
}
