CREATE TABLE app_user (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fish_species (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    base_points INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE bet (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    scoring_mode VARCHAR(30) NOT NULL,
    created_by_id BIGINT NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT bet_dates_check CHECK (end_date >= start_date),
    CONSTRAINT bet_scoring_mode_check CHECK (scoring_mode IN ('TOTAL_POINTS', 'BEST_PER_SPECIES'))
);

CREATE TABLE bet_participant (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bet_id BIGINT NOT NULL REFERENCES bet(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT bet_participant_status_check CHECK (status IN ('INVITED', 'ACCEPTED', 'DECLINED')),
    CONSTRAINT bet_participant_unique UNIQUE (bet_id, user_id)
);

CREATE TABLE catch_entry (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bet_id BIGINT NOT NULL REFERENCES bet(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    fish_species_id BIGINT NOT NULL REFERENCES fish_species(id),
    length_cm NUMERIC(5,1) NOT NULL,
    caught_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT catch_entry_length_check CHECK (length_cm > 0)
);

INSERT INTO fish_species (name, base_points) VALUES
('Barsch', 10),
('Karpfen', 12),
('Hecht', 15),
('Zander', 15),
('Forelle', 11),
('Wels', 20),
('Aal', 14),
('Schleie', 13),
('Brasse', 8),
('Rotauge', 5);
