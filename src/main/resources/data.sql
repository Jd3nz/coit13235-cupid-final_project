INSERT INTO users (display_name, age, bio)
SELECT 'Alex', 24, 'Software developer who enjoys hiking.'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE display_name = 'Alex');

INSERT INTO users (display_name, age, bio)
SELECT 'Jordan', 23, 'Coffee, photography and live music.'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE display_name = 'Jordan');

INSERT INTO users (display_name, age, bio)
SELECT 'Taylor', 25, 'Traveller and amateur cook.'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE display_name = 'Taylor');

INSERT INTO users (display_name, age, bio)
SELECT 'Morgan', 26, 'Designer, reader and dog person.'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE display_name = 'Morgan');
