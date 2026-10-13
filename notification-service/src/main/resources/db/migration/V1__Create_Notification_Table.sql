CREATE TABLE  notification(
request_id UUID PRIMARY KEY,
reference_type VARCHAR(50) NOT NULL,
reference_id BIGINT NOT NULL,
notification_type VARCHAR(50) NOT NULL,
status VARCHAR(50) NOT NULL,
created_at TIMESTAMP NOT NULL,
processed_at TIMESTAMP
);