-- Tạo database
CREATE DATABASE cinema_management;
GO

USE cinema_management;
GO

-- USERS
CREATE TABLE users (
    userId INT IDENTITY(1,1) PRIMARY KEY,
    employeeId VARCHAR(10) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    fullName NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('Admin', 'Manager', 'Employee')) NOT NULL,
    createdAt DATETIME DEFAULT GETDATE()
);

-- PASSWORD RESET
CREATE TABLE passwordResetRequests (
    requestId INT IDENTITY(1,1) PRIMARY KEY,
    staffId INT NOT NULL,
    requestTime DATETIME DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'Pending' CHECK (status IN ('Pending', 'Approved', 'Rejected')),
    handledBy INT,
    handledTime DATETIME,
    FOREIGN KEY (staffId) REFERENCES users(userId),
    FOREIGN KEY (handledBy) REFERENCES users(userId)
);

--LANGUAGES--
CREATE TABLE languages (
    languageId INT IDENTITY(1,1) PRIMARY KEY,
    languageName NVARCHAR(50) NOT NULL UNIQUE
);

--AGE-RATING--
CREATE TABLE ageRatings (
    ageRatingCode VARCHAR(10) PRIMARY KEY,
    description NVARCHAR(100) NOT NULL
);

--GENRES--
CREATE TABLE genres (
    genreId INT IDENTITY(1,1) PRIMARY KEY,
    genreName NVARCHAR(100) NOT NULL UNIQUE
);

-- MOVIES
CREATE TABLE movies (
    movieId INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    duration INT NOT NULL CHECK (duration > 0),
    description TEXT,
    directedBy VARCHAR(100),
    languageId INT,
    poster VARCHAR(255),
    ageRatingCode VARCHAR(10),
    releaseDate DATE NOT NULL,
    createdAt DATETIME DEFAULT GETDATE()

	CONSTRAINT FK_movies_languages FOREIGN KEY (languageId) REFERENCES languages(languageId),
    CONSTRAINT FK_movies_ageratings FOREIGN KEY (ageRatingCode) REFERENCES ageRatings(ageRatingCode)
);

CREATE TABLE movieGenres (
    movieId INT NOT NULL,
    genreId INT NOT NULL,
    PRIMARY KEY (movieId, genreId),
    FOREIGN KEY (movieId) REFERENCES movies(movieId),
    FOREIGN KEY (genreId) REFERENCES genres(genreId)
);


-- ROOM TYPES
CREATE TABLE roomTypes (
    roomTypeId INT IDENTITY(1,1) PRIMARY KEY,
    typeName NVARCHAR(50) UNIQUE NOT NULL,
    description NVARCHAR(255),
    maxRows INT CHECK (maxRows > 0),
    maxColumns INT CHECK (maxColumns > 0),
    extraFee DECIMAL(10,2) DEFAULT 0
);

-- SCREENING ROOMS
CREATE TABLE screeningRooms (
    roomId INT IDENTITY(1,1) PRIMARY KEY,
    roomNumber VARCHAR(20) UNIQUE NOT NULL,
    seatingLayout TEXT NOT NULL,
    totalCapacity INT NOT NULL CHECK (totalCapacity > 0),
    roomTypeId INT NOT NULL,
    equipment TEXT,
    roomStatus VARCHAR(20) NOT NULL DEFAULT 'Available'
        CHECK (roomStatus IN ('Available', 'Maintenance', 'Out of Order')),
    createdAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (roomTypeId) REFERENCES roomTypes(roomTypeId)
);

-- SEAT TYPES
CREATE TABLE seatTypes (
    seatTypeId INT IDENTITY(1,1) PRIMARY KEY,
    typeName NVARCHAR(50) UNIQUE NOT NULL,
    extraFee DECIMAL(10,2) NOT NULL DEFAULT 0
);

-- SEATS
CREATE TABLE seats (
    seatId INT IDENTITY(1,1) PRIMARY KEY,
    roomId INT NOT NULL,
    seatRow CHAR(1) NOT NULL,
    seatColumn INT NOT NULL,
    seatTypeId INT,
    isActive BIT DEFAULT 1,
    UNIQUE (roomId, seatRow, seatColumn),
    FOREIGN KEY (roomId) REFERENCES screeningRooms(roomId),
    FOREIGN KEY (seatTypeId) REFERENCES seatTypes(seatTypeId)
);

-- SHOWTIMES
CREATE TABLE showtimes (
    showtimeId INT IDENTITY(1,1) PRIMARY KEY,
    movieId INT NOT NULL,
    roomId INT NOT NULL,
    showDate DATE NOT NULL,
    showTime TIME NOT NULL,
	endTime TIME,
    createdAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (movieId) REFERENCES movies(movieId),
    FOREIGN KEY (roomId) REFERENCES screeningRooms(roomId)
);

-- SERVICE CATEGORIES
CREATE TABLE serviceCategories (
    categoryId INT IDENTITY(1,1) PRIMARY KEY,
    categoryName NVARCHAR(50) UNIQUE NOT NULL
);

-- SERVICES
CREATE TABLE services (
    serviceId INT IDENTITY(1,1) PRIMARY KEY,
    serviceName NVARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    categoryId INT NOT NULL,
    createdAt DATETIME DEFAULT GETDATE(),
    img VARCHAR(255),
    FOREIGN KEY (categoryId) REFERENCES serviceCategories(categoryId)
);

-- LOCKERS
CREATE TABLE lockers (
    lockerId INT IDENTITY(1,1) PRIMARY KEY,
    lockerNumber VARCHAR(20) UNIQUE NOT NULL,
    locationInfo NVARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'Available'
        CHECK (status IN ('Available', 'Occupied', 'Maintenance')),
    createdAt DATETIME DEFAULT GETDATE()
);

CREATE TABLE lockerAssignments (
    assignmentId INT IDENTITY(1,1) PRIMARY KEY,
    lockerId INT NOT NULL,
    pinCode CHAR(4) NOT NULL,
    itemDescription NVARCHAR(255),
    assignedAt DATETIME DEFAULT GETDATE(),
    releasedAt DATETIME NULL,
    ticketCode VARCHAR(20),
    FOREIGN KEY (lockerId) REFERENCES lockers(lockerId),
    FOREIGN KEY (ticketCode) REFERENCES tickets(ticketCode)
);
-- LOCKER HISTORY
CREATE TABLE lockerHistory (
    historyId INT IDENTITY(1,1) PRIMARY KEY,
    lockerId INT NOT NULL,
    userAction NVARCHAR(100),
    actionTime DATETIME DEFAULT GETDATE(),
    userInfo NVARCHAR(255),
    FOREIGN KEY (lockerId) REFERENCES lockers(lockerId)
);

CREATE TABLE tickets (
    ticketId INT IDENTITY(1,1) PRIMARY KEY,
    ticketCode VARCHAR(20) UNIQUE NOT NULL,
    customerName NVARCHAR(100),
    ticketType VARCHAR(50),
    showtimeId INT NOT NULL,
    soldBy INT NOT NULL,
    soldAt DATETIME DEFAULT GETDATE(),
    totalPrice DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (showtimeId) REFERENCES showtimes(showtimeId),
    FOREIGN KEY (soldBy) REFERENCES users(userId)
);

-- TICKET SEATS
CREATE TABLE ticketSeats (
    ticketId INT NOT NULL,
    seatId INT NOT NULL,
    PRIMARY KEY (ticketId, seatId),
    FOREIGN KEY (ticketId) REFERENCES tickets(ticketId),
    FOREIGN KEY (seatId) REFERENCES seats(seatId)
);

-- TICKET SERVICES
CREATE TABLE ticketServices (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ticketId INT NOT NULL,
    serviceId INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    servicePrice DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (ticketId) REFERENCES tickets(ticketId),
    FOREIGN KEY (serviceId) REFERENCES services(serviceId)
);

-- TICKET PAYMENTS
CREATE TABLE ticketPayments (
    paymentId INT IDENTITY(1,1) PRIMARY KEY,
    ticketId INT NOT NULL,
    paymentMethod VARCHAR(50) NOT NULL,
    paidAmount DECIMAL(10,2) NOT NULL,
    paymentTime DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ticketId) REFERENCES tickets(ticketId)
);

CREATE INDEX idx_locker_assignments_ticket ON lockerAssignments(ticketCode);

--Insert data--
INSERT INTO genres (genreName) VALUES
(N'Action'),
(N'Adventure'),
(N'Animation'),
(N'Biography'),
(N'Comedy'),
(N'Crime'),
(N'Documentary'),
(N'Drama'),
(N'Family'),
(N'Fantasy'),
(N'History'),
(N'Horror'),
(N'Musical'),
(N'Mystery'),
(N'Romance'),
(N'Science Fiction'),
(N'Sport'),
(N'Thriller'),
(N'War'),
(N'Western'),
(N'Superhero'),
(N'Noir'),
(N'Psychological'),
(N'Suspense');

INSERT INTO languages (languageName) VALUES
(N'English'),
(N'Korean'),
(N'Japanese'),
(N'Vietnamese'),
(N'French'),
(N'Chinese'),
(N'Hindi'),
(N'German'),
(N'Thai'),
(N'Italian'),
(N'Spanish'),
(N'Portuguese'),
(N'Russian'),
(N'Indonesian'),
(N'Filipino'),
(N'Arabic');


INSERT INTO ageRatings (ageRatingCode, description) VALUES
('C', N'Restricted. For adults only (18+).'),
('K', N'Suitable for children. Animated or educational content.'),
('P', N'General audience. Suitable for all ages.'),
('T13', N'Not suitable for children under 13 years old.'),
('T16', N'Not suitable for viewers under 16 years old. May contain mature content.'),
('T18', N'Not suitable for viewers under 18 years old. Contains strong content.');

INSERT INTO roomTypes (typeName, description, maxRows, maxColumns, extraFee) VALUES
(N'Standard', N'Standard screening room', 15, 15, 0.00),
(N'Gold Class', N'Premium Gold Class room', 15, 15, 201.00);

INSERT INTO seatTypes (typeName, extraFee) VALUES
(N'Standard', 0.00),
(N'VIP', 6.00),
(N'Sweetbox', 131.00),
(N'Gold', 201.00);

