-- One ingredient vector per product for "similar to what you liked" (docs/feedback.md, Similarity).
-- A sparse vector: one dimension per ingredient id, almost all zeros, so only the label's ingredients are stored.
-- No index yet: with ~100 products a full scan is instant; add HNSW when the catalog grows.
CREATE TABLE product_vectors (
    product_id BIGINT PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    embedding  sparsevec(1000000) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
