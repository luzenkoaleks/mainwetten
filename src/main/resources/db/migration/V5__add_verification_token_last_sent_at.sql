ALTER TABLE email_verification_token
    ADD COLUMN last_sent_at TIMESTAMPTZ;

UPDATE email_verification_token
SET last_sent_at = created_at;

ALTER TABLE email_verification_token
    ALTER COLUMN last_sent_at SET NOT NULL;
