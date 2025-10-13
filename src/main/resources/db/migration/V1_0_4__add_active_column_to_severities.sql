-- Migration V1.0.4: Add missing active column to severities table
ALTER TABLE severities ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- Update existing records to be active
UPDATE severities SET active = TRUE WHERE active IS NULL;

-- Create index for active column
CREATE INDEX IF NOT EXISTS idx_severity_active ON severities(active);