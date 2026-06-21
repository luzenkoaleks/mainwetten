ALTER TABLE email_verification_token
    ALTER COLUMN token_hash TYPE VARCHAR(64);
