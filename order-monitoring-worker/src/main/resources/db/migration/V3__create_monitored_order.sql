create table monitored_order(
order_id BIGINT primary key,
status VARCHAR(50) NOT NULL,
status_changed_at TIMESTAMP NOT NULL,
deadline_at TIMESTAMP,
monitoring_status VARCHAR(50) NOT NULL,
updated_at TIMESTAMP NOT NULL
);