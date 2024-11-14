CREATE TABLE IF NOT EXISTS transaction
(
    id               uuid PRIMARY KEY,
    from_account     uuid          NOT NULL,
    to_account       uuid          NOT NULL,
    amount           decimal(10,4) NOT NULL,
    currency         char(3)       NOT NULL,
    transaction_time timestamp     NOT NULL,
    settle_time      timestamp     NOT NULL,
    create_time      timestamp     NOT NULL DEFAULT now(),
    last_update      timestamp     NOT NULL DEFAULT now(),
    version          integer       NOT NULL DEFAULT 0
)  WITH(FILLFACTOR=85);

CREATE INDEX idx_txn_from_account ON transaction (from_account);
CREATE INDEX idx_txn_to_account  ON transaction (to_account);
CREATE INDEX idx_txn_create_time ON transaction USING BRIN(create_time);
CREATE INDEX idx_txn_last_update ON transaction USING BRIN(last_update);
