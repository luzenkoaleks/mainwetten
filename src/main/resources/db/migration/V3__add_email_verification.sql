ALTER TABLE app_user
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE app_user
    ALTER COLUMN email_verified SET DEFAULT FALSE;

CREATE TABLE email_verification_token (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE
        REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX email_verification_token_expires_at_idx
    ON email_verification_token (expires_at);
