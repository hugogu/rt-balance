-- Follows: https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html
CREATE TABLE outbox
(
    id             RAW(16) PRIMARY KEY,
    aggregate_type varchar(255)  NOT NULL,
    aggregate_id   varchar(255)  NOT NULL,
    type           varchar(255)  NOT NULL,
    payload        CLOB          NOT NULL,
    create_time    timestamp     DEFAULT CURRENT_TIMESTAMP,
    -- A typical outbox table is not supposed to be updated,
    -- these two columns are for manual inspection only.
    last_update    timestamp     DEFAULT CURRENT_TIMESTAMP,
    version        integer       DEFAULT 0
);

CREATE INDEX idx_outbox_aggregate_id ON outbox (aggregate_id);
CREATE INDEX idx_outbox_create_time ON outbox (create_time);
