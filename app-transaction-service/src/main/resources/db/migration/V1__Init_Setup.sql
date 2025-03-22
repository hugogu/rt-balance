CREATE TABLE transaction
(
    id               RAW(16)       PRIMARY KEY,
    from_account     RAW(16)       NOT NULL,
    to_account       RAW(16)       NOT NULL,
    amount           decimal(19,4) NOT NULL,
    currency         char(3)       NOT NULL,
    status           varchar(16)   NOT NULL,
    transaction_time timestamp     NOT NULL,
    settle_time      timestamp     NOT NULL,
    create_time      timestamp     DEFAULT CURRENT_TIMESTAMP,
    last_update      timestamp     DEFAULT CURRENT_TIMESTAMP,
    version          integer       DEFAULT 0
);

CREATE INDEX idx_txn_from_account ON transaction (from_account);
CREATE INDEX idx_txn_to_account  ON transaction (to_account);
CREATE INDEX idx_txn_create_time ON transaction (create_time);
CREATE INDEX idx_txn_last_update ON transaction (last_update);
