ALTER TABLE enrollments
ADD COLUMN idempotency_key VARCHAR(255);

CREATE UNIQUE INDEX uq_enrollments_idempotency_key
ON enrollments(idempotency_key);