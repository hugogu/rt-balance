CREATE TABLE account
(
    id             RAW(16) PRIMARY KEY,
    account_ccy    varchar(3)    NOT NULL,
    account_num    varchar(32)   NOT NULL,
    status         varchar(16)   NOT NULL,
    balance        decimal(19,4) NOT NULL,
    locked_balance decimal(19,4) NOT NULL,
    create_time    timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_update    timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version        integer       DEFAULT 0
);

CREATE INDEX idx_account_account_num ON account (account_num);
CREATE INDEX idx_account_create_time ON account (create_time);
CREATE INDEX idx_account_last_update ON account (last_update);

CREATE TABLE transaction_log
(
    id               RAW(16) PRIMARY KEY,
    status           varchar(8)  NOT NULL,
    transaction_data BLOB        NOT NULL,
    create_time      timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_update      timestamp WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version          integer     DEFAULT 0
);

CREATE INDEX idx_txn_log_create_time ON transaction_log (create_time);
