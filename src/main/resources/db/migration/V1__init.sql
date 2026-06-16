CREATE TABLE app_user (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          username VARCHAR(50) NOT NULL UNIQUE,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fish_species (
                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              name VARCHAR(80) NOT NULL UNIQUE,
                              category VARCHAR(30) NOT NULL,
                              active BOOLEAN NOT NULL DEFAULT TRUE,
                              CONSTRAINT fish_species_category_check CHECK (category IN ('FRESHWATER', 'SALTWATER'))
);

CREATE TABLE bet (
                     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                     title VARCHAR(100) NOT NULL,
                     description TEXT,
                     start_date DATE NOT NULL,
                     end_date DATE NOT NULL,
                     scoring_mode VARCHAR(30) NOT NULL,
                     fish_category VARCHAR(30) NOT NULL DEFAULT 'ALL',
                     created_by_id BIGINT NOT NULL REFERENCES app_user(id),
                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     CONSTRAINT bet_dates_check CHECK (end_date >= start_date),
                     CONSTRAINT bet_scoring_mode_check CHECK (scoring_mode IN ('TOTAL_POINTS', 'BEST_PER_SPECIES')),
                     CONSTRAINT bet_fish_category_check CHECK (fish_category IN ('ALL', 'FRESHWATER', 'SALTWATER'))
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

CREATE TABLE catch_record (
                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
                              fish_species_id BIGINT NOT NULL REFERENCES fish_species(id),
                              length_cm NUMERIC(5,1) NOT NULL,
                              caught_at TIMESTAMPTZ NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT catch_record_length_check CHECK (length_cm > 0)
);

CREATE TABLE catch_assignment (
                                  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  catch_record_id BIGINT NOT NULL REFERENCES catch_record(id) ON DELETE CASCADE,
                                  bet_id BIGINT NOT NULL REFERENCES bet(id) ON DELETE CASCADE,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT catch_assignment_unique UNIQUE (catch_record_id, bet_id)
);

INSERT INTO fish_species (name, category) VALUES
-- Süßwasser
('Aal', 'FRESHWATER'),
('Äsche', 'FRESHWATER'),
('Barbe', 'FRESHWATER'),
('Barsch', 'FRESHWATER'),
('Brasse', 'FRESHWATER'),
('Döbel', 'FRESHWATER'),
('Forelle', 'FRESHWATER'),
('Graskarpfen', 'FRESHWATER'),
('Hecht', 'FRESHWATER'),
('Nase', 'FRESHWATER'),
('Quappe', 'FRESHWATER'),
('Rapfen', 'FRESHWATER'),
('Rotauge', 'FRESHWATER'),
('Rotfeder', 'FRESHWATER'),
('Saibling', 'FRESHWATER'),
('Schleie', 'FRESHWATER'),
('Schuppenkarpfen', 'FRESHWATER'),
('Spiegelkarpfen', 'FRESHWATER'),
('Stör', 'FRESHWATER'),
('Wels', 'FRESHWATER'),
('Zander', 'FRESHWATER'),

-- Salzwasser
('Dorsch', 'SALTWATER'),
('Hering', 'SALTWATER'),
('Heilbutt', 'SALTWATER'),
('Kliesche', 'SALTWATER'),
('Leng', 'SALTWATER'),
('Lumb', 'SALTWATER'),
('Köhler', 'SALTWATER'),
('Makrele', 'SALTWATER'),
('Meerforelle', 'SALTWATER'),
('Pollack', 'SALTWATER'),
('Rotbarsch', 'SALTWATER'),
('Scholle', 'SALTWATER'),
('Seelachs', 'SALTWATER'),
('Seeskorpion', 'SALTWATER'),
('Steinbeißer', 'SALTWATER'),
('Steinbutt', 'SALTWATER'),
('Wittling', 'SALTWATER'),
('Wolfsbarsch', 'SALTWATER');