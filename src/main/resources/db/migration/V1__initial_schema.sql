
-- BookingPlatform
-- V1: Initial SQLite database schema


-- =====================================
-- 1. USERS
-- =====================================

CREATE TABLE Users (
                       userID INTEGER PRIMARY KEY AUTOINCREMENT,
                       email TEXT NOT NULL UNIQUE,
                       passwordHash TEXT NOT NULL,
                       firstName TEXT,
                       lastName TEXT,
                       gender TEXT,
                       contactNum TEXT,
                       profilePictureURL TEXT,
                       isActivated INTEGER NOT NULL DEFAULT 0
                           CHECK (isActivated IN (0, 1)),
                       activationCode TEXT,
                       activationCodeExpiry TEXT
);

-- =====================================
-- 2. PROPERTY OWNERS
-- =====================================

CREATE TABLE PropertyOwners (
                                ownerID TEXT PRIMARY KEY,
                                email TEXT NOT NULL UNIQUE,
                                balance NUMERIC DEFAULT 0,
                                totalEarnings NUMERIC DEFAULT 0,

                                FOREIGN KEY (email)
                                    REFERENCES Users(email)
                                    ON DELETE RESTRICT
);

-- =====================================
-- 3. GUESTS
-- =====================================

CREATE TABLE Guests (
                        guestID TEXT PRIMARY KEY,
                        email TEXT NOT NULL,

                        nationality TEXT,
                        points INTEGER DEFAULT 0,
                        dob TEXT,

                        paymentMethodID TEXT,
                        paymentUserID TEXT,
                        paymentType TEXT,
                        isDefault INTEGER DEFAULT 0
                            CHECK (isDefault IN (0, 1)),

                        nickname TEXT,
                        preferences TEXT,

                        FOREIGN KEY (email)
                            REFERENCES Users(email)
                            ON DELETE RESTRICT
);

-- =====================================
-- 4. EMPLOYEES
-- =====================================

CREATE TABLE Employees (
                           empID TEXT PRIMARY KEY,
                           email TEXT,
                           accessLevel TEXT NOT NULL,
                           jobRole TEXT NOT NULL,
                           hireDate TEXT NOT NULL,
                           terminationDate TEXT,

                           FOREIGN KEY (email)
                               REFERENCES Users(email)
                               ON DELETE RESTRICT
);

-- =====================================
-- 5. PROPERTIES
-- =====================================

CREATE TABLE Properties (
                            propertyID TEXT PRIMARY KEY,
                            ownerID TEXT,
                            propertyName TEXT,
                            description TEXT,

                            street TEXT,
                            suite TEXT,
                            city TEXT,
                            state TEXT,
                            postalCode TEXT,
                            country TEXT,

                            latitude REAL,
                            longitude REAL,
                            houseSize REAL,
                            landSize REAL,

                            baseRate NUMERIC,

                            FOREIGN KEY (ownerID)
                                REFERENCES PropertyOwners(ownerID)
                                ON DELETE RESTRICT
);

-- =====================================
-- 6. BOOKINGS
-- =====================================

CREATE TABLE Bookings (
                          bookingID TEXT PRIMARY KEY,

                          guestID TEXT NOT NULL,
                          propertyID TEXT NOT NULL,

                          arrivalDate TEXT NOT NULL,
                          departureDate TEXT NOT NULL,

                          baseCharge NUMERIC,
                          gct NUMERIC,
                          tef NUMERIC,
                          gart NUMERIC,
                          totalCharge NUMERIC,

                          status TEXT,
                          payStatus TEXT,

                          createdAt TEXT NOT NULL
                              DEFAULT CURRENT_TIMESTAMP,

                          updatedAt TEXT
                              DEFAULT CURRENT_TIMESTAMP,

                          CHECK (departureDate > arrivalDate),

                          FOREIGN KEY (guestID)
                              REFERENCES Guests(guestID)
                              ON DELETE RESTRICT,

                          FOREIGN KEY (propertyID)
                              REFERENCES Properties(propertyID)
                              ON DELETE RESTRICT
);

-- =====================================
-- 7. REVIEWS
-- =====================================

CREATE TABLE Reviews (
                         reviewID INTEGER PRIMARY KEY AUTOINCREMENT,

                         guestID TEXT NOT NULL,
                         propertyID TEXT NOT NULL,
                         bookingID TEXT NOT NULL UNIQUE,

                         rating INTEGER CHECK (rating BETWEEN 1 AND 5),
                         comment TEXT,

                         timeStamp TEXT NOT NULL
                             DEFAULT CURRENT_TIMESTAMP,

                         FOREIGN KEY (guestID)
                             REFERENCES Guests(guestID)
                             ON DELETE RESTRICT,

                         FOREIGN KEY (propertyID)
                             REFERENCES Properties(propertyID)
                             ON DELETE RESTRICT,

                         FOREIGN KEY (bookingID)
                             REFERENCES Bookings(bookingID)
                             ON DELETE RESTRICT
);

-- =====================================
-- 8. INDEXES
-- =====================================

CREATE INDEX idx_bookings_property_dates
    ON Bookings(propertyID, arrivalDate, departureDate);

CREATE INDEX idx_bookings_guest
    ON Bookings(guestID);

CREATE INDEX idx_properties_owner
    ON Properties(ownerID);

CREATE INDEX idx_reviews_property
    ON Reviews(propertyID);

