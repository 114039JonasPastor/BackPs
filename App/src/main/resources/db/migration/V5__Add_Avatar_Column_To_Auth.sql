-- Add avatar column to Auth table
ALTER TABLE Auth ADD COLUMN IF NOT EXISTS avatar VARCHAR(500);
