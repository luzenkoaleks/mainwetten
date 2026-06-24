ALTER TABLE bet
    DROP CONSTRAINT bet_created_by_id_fkey;

ALTER TABLE bet
    ALTER COLUMN created_by_id DROP NOT NULL;

ALTER TABLE bet
    ADD CONSTRAINT bet_created_by_id_fkey
        FOREIGN KEY (created_by_id)
        REFERENCES app_user(id)
        ON DELETE SET NULL;


ALTER TABLE bet_participant
    ADD COLUMN invited_by_id BIGINT;

ALTER TABLE bet_participant
    ADD CONSTRAINT bet_participant_invited_by_id_fkey
        FOREIGN KEY (invited_by_id)
        REFERENCES app_user(id)
        ON DELETE SET NULL;

CREATE INDEX bet_participant_invited_by_id_idx
    ON bet_participant (invited_by_id);
