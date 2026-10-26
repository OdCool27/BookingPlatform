-- Requested initial accounts. Passwords are BCrypt hashes; accounts are already activated.
-- Deliberately fail on conflicting emails/IDs rather than overwrite an existing account
-- or silently grant administrator access to a different user. Flyway applies this once.
INSERT INTO Users (email, passwordHash, firstName, lastName, gender, contactNum,
                   profilePictureURL, isActivated, activationCode, activationCodeExpiry)
VALUES
    ('odanecollins@live.com', '$2a$10$dDoBYbBH8eitlPlNZKMVguHgIulCyqNslPHGu5z.dqT3zZ9qAl58W',
     'Odane', 'Collins', 'MALE', '+1-876-555-0100', NULL, 1, NULL, NULL),
    ('john.smith@example.com', '$2a$10$5qFOUmtkFPiibUbXMn8c5eRohw33dArf14YAU8lEzNfZcM4kYGl9C',
     'John', 'Smith', 'MALE', '+1-876-555-0101', NULL, 1, NULL, NULL),
    ('phil.collins@example.com', '$2a$10$cdP2FvcpI2653iGGpgTYl.0V5K9UNQut5pEfH.xOkKoY8YdL7VJ86',
     'Phil', 'Collins', 'MALE', '+1-876-555-0102', NULL, 1, NULL, NULL);

-- The existing admin representation is an employee, not a new user-role table.
INSERT INTO Employees (empID, email, accessLevel, jobRole, hireDate, terminationDate)
VALUES ('E-ODCO-10001', 'odanecollins@live.com', 'SUPERADMIN', 'ADMIN', '2026-10-26', NULL);

INSERT INTO Guests (guestID, email, nationality, points, dob, paymentMethodID,
                    paymentUserID, paymentType, isDefault, nickname, preferences)
VALUES ('G-JOSM-10001', 'john.smith@example.com', 'Jamaican', 0, '1990-05-15',
        NULL, NULL, 'NONE', 0, NULL, '["Quiet","Wi-Fi"]');

INSERT INTO PropertyOwners (ownerID, email, balance, totalEarnings)
VALUES ('PO-PHCO-10001', 'phil.collins@example.com', 0, 0);
