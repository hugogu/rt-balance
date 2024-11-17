CREATE TABLE IF NOT EXISTS account
(
    id             uuid PRIMARY KEY,
    account_ccy    varchar(3)    NOT NULL,
    account_num    varchar(32)   NOT NULL,
    status         varchar(16)   NOT NULL,
    balance        decimal(10,4) NOT NULL,
    locked_balance decimal(10,4) NOT NULL,
    create_time    timestamp     NOT NULL DEFAULT now(),
    last_update    timestamp     NOT NULL DEFAULT now(),
    version        integer       NOT NULL DEFAULT 0
);

CREATE INDEX idx_account_account_num ON account (account_num);
CREATE INDEX idx_account_create_time ON account USING BRIN(create_time);
CREATE INDEX idx_account_last_update ON account USING BRIN(create_time);

CREATE TABLE IF NOT EXISTS transaction_log
(
    id               uuid PRIMARY KEY,
    status           varchar(8)  NOT NULL,
    transaction_data jsonb       NOT NULL,
    create_time      timestamp   NOT NULL DEFAULT now(),
    last_update      timestamp   NOT NULL DEFAULT now(),
    version          integer     NOT NULL DEFAULT 0
) WITH(FILLFACTOR=85);

CREATE INDEX idx_txn_log_create_time ON transaction_log USING BRIN(create_time);
