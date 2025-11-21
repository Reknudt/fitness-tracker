CREATE TABLE media (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL, -- путь в S3 / MinIO
    uploaded_at TIMESTAMP DEFAULT now()
    -- status
);
