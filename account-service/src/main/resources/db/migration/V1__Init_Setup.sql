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

CREATE TABLE IF NOT EXISTS transaction_log
(
    id               uuid PRIMARY KEY,
    transaction_data jsonb NOT NULL,
    create_time      timestamp   NOT NULL DEFAULT now()
);
