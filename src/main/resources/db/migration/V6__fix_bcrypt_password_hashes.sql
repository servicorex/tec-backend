-- ==============================================================================
-- Flyway Migration V6: Update Production BCrypt Password Hashes
-- Generated with BCrypt Cost Factor 12
-- ==============================================================================

UPDATE users SET password_hash = '$2a$12$FBorTGFqJX/7o/eOzyQRGOSu9ZiN1kPtKBUWcEPJCOKuv5Eufw0N6' WHERE username = '23NE1A05K3';
UPDATE users SET password_hash = '$2a$12$1J8Sr.NI2RBJvOhh7b8jT.L8c/lyN1TB.cwq4hZrvJgrPFfw5wOQy' WHERE username = 'admin@tecnrt.org';
UPDATE users SET password_hash = '$2a$12$gU/PGorcBKbchecH.X8wG.qZZRsnMGTwx18OmbTp.3YWjtVwKR54K' WHERE username = 'hod.cse@tecnrt.org';
UPDATE users SET password_hash = '$2a$12$f0hBInCIKCJwk.9FRUXihuuBMDXxwEHhwrV0zU1Hhgmup9163seFa' WHERE username = 'hod.aiml@tecnrt.org';
UPDATE users SET password_hash = '$2a$12$pwquTJn68w0QZSpydH7oreTzCQe4YZC5aRY0o3vpryp.r4KRdinuK' WHERE username = 'hod.aids@tecnrt.org';
UPDATE users SET password_hash = '$2a$12$Jb8ZpCwUUaNqr/u5rHjeG.1wzGPHEoE3vk7aiQKwFpAlJmBuAeqke' WHERE username = 'hod.it@tecnrt.org';
UPDATE users SET password_hash = '$2a$12$cdGfJgax/sQ8VMVEXGiN6OnjI9BoikQ/1ipH5CFCoiR/P.Q63KEMm' WHERE username = 'hod.ece@tecnrt.org';
UPDATE users SET password_hash = '$2a$12$yroDi3XK31nEeWCf1vP0Iu79zVbmMahyHAPjtRx/vCmBF..4p1lhC' WHERE username = 'hod.eee@tecnrt.org';

