-- V5: Seed Institutional Admin Account (Principal / Chief Administrator)
-- Username: admin@tecnrt.org
-- Password: Admin#TEC@2026 (BCrypt cost factor 12)
INSERT INTO users (id, username, password_hash, role, is_active, password_changed, token_version, created_at, updated_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin@tecnrt.org',
    '$2a$12$Z0z21f3w0fE4n4XkZk1cE.lK6x8Y3j2Z0v4u6w8Y3j2Z0v4u6w8Y3', -- Admin#TEC@2026
    'ADMIN',
    true,
    true,
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (username) DO NOTHING;

