-- Follows: https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html
CREATE TABLE IF NOT EXISTS outbox
(
    id             uuid PRIMARY KEY,
    aggregate_type varchar(255)  NOT NULL,
    aggregate_id   varchar(255)  NOT NULL,
    type           varchar(255)  NOT NULL,
    payload        jsonb         NOT NULL,
    create_time    timestamp     NOT NULL DEFAULT now(),
    -- A typical outbox table is not supposed to be updated,
    -- these two columns are for manual inspection only.
    last_update    timestamp     NOT NULL DEFAULT now(),
    version        integer       NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate_id ON outbox (aggregate_id);
CREATE INDEX IF NOT EXISTS idx_outbox_create_time ON outbox USING BRIN(create_time);
