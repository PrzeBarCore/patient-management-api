CREATE TABLE medical_order (
    id BIGSERIAL PRIMARY KEY,
    registration_date_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    patient_id BIGINT NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patient(id)
);