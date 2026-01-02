-- Add tenant_id column to refresh_tokens
ALTER TABLE refresh_tokens ADD COLUMN tenant_id UUID;

-- Backfill existing tokens (if any) - THIS REQUIRES MANUAL REVIEW
-- DELETE FROM refresh_tokens WHERE tenant_id IS NULL;

-- Make it non-nullable
ALTER TABLE refresh_tokens ALTER COLUMN tenant_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- Add index for performance
CREATE INDEX idx_refresh_tokens_tenant_id ON refresh_tokens(tenant_id);