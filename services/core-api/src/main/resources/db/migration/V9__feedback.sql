-- Step 5 feedback (docs/feedback.md). No accounts: a random client ID from the browser, nothing personal.

-- Unused tables from the first idea; "users" held an email, which this design avoids.
DROP TABLE user_product_reports;
DROP TABLE users;

-- Each routine built for a client, with the answers it was built from (without pregnancy and city)
-- and what was shown. Training data for the ranking model in step 6.
CREATE TABLE quiz_sessions (
    id              BIGSERIAL PRIMARY KEY,
    client_id       UUID NOT NULL,
    answers         JSONB NOT NULL,
    climate_signals TEXT[] NOT NULL DEFAULT '{}',
    routine         JSONB NOT NULL,          -- step -> product id shown
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_quiz_sessions_client ON quiz_sessions(client_id);

-- One verdict per client and product; a new verdict replaces the old one.
CREATE TABLE product_feedback (
    client_id  UUID NOT NULL,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    verdict    TEXT NOT NULL CHECK (verdict IN ('LIKED', 'DISLIKED')),
    reason     TEXT CHECK (reason IN ('IRRITATED', 'DIDNT_WORK', 'TEXTURE', 'TOO_PRICEY')),
    session_id BIGINT REFERENCES quiz_sessions(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (client_id, product_id),
    CHECK (verdict = 'DISLIKED' OR reason IS NULL)  -- only a dislike has a reason
);
