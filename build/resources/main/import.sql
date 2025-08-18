-- Insert two users with encrypted passwords (using BCrypt)
-- Password for user1: Test1234 (matches the pattern with one uppercase, two digits, and lowercase letters)
-- Password for user2: Admin5678 (matches the pattern with one uppercase, two digits, and lowercase letters)

-- Insert User 1 with UUID without hyphens
INSERT INTO users (id, name, email, password, created, last_login, is_active) VALUES ('11111111111111111111111111111111', 'John Doe', 'john.doe@example.com', '$2a$10$3Qmb7KO.6OU6bQZiUHF7aOLExQK4OMm4CySkWw6IsFnWnWBG9AKlG', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- Insert User 2 with UUID without hyphens
INSERT INTO users (id, name, email, password, created, last_login, is_active) VALUES ('22222222222222222222222222222222', 'Jane Smith', 'jane.smith@example.com', '$2a$10$dH7.ES6LVnGBKbTOhW0q9.VyjHD1Jw.wE.YVP5kLk7DAdJ8eFUOxO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true);

-- Insert phones for User 1 with explicit IDs
INSERT INTO phones (id, number, city_code, country_code, user_id) VALUES ('33333333333333333333333333333333', 123456789, 1, '+1', '11111111111111111111111111111111');

INSERT INTO phones (id, number, city_code, country_code, user_id) VALUES ('44444444444444444444444444444444', 987654321, 2, '+1', '11111111111111111111111111111111');

-- Insert phones for User 2 with explicit IDs
INSERT INTO phones (id, number, city_code, country_code, user_id) VALUES ('55555555555555555555555555555555', 555123456, 3, '+44', '22222222222222222222222222222222');