CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMPTZ NOT NULL
);

CREATE INDEX persistent_logins_username_idx
    ON persistent_logins (username);
