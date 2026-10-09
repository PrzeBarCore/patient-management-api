alter table medical_order
    add version BIGINT NOT NULL DEFAULT 0;