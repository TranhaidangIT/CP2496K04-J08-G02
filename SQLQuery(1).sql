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

-- MOVIES
CREATE TABLE movies (
    movieId INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    duration INT NOT NULL CHECK (duration > 0),
    genre VARCHAR(100),
    description TEXT,
    directedBy VARCHAR(100),
    language VARCHAR(50),
    poster VARCHAR(255),
    ageRating VARCHAR(10),
    releaseDate DATE NOT NULL,
    createdAt DATETIME DEFAULT GETDATE()
);

-- SCREENING ROOMS
CREATE TABLE screeningRooms (
    roomId INT IDENTITY(1,1) PRIMARY KEY,
    roomNumber VARCHAR(20) UNIQUE NOT NULL,
    seatingLayout TEXT NOT NULL,
    totalCapacity INT NOT NULL CHECK (totalCapacity > 0),
    roomType VARCHAR(50),
    equipment TEXT,
    roomStatus VARCHAR(20) NOT NULL DEFAULT 'Available'
        CHECK (roomStatus IN ('Available', 'Maintenance', 'Out of Order')),
    createdAt DATETIME DEFAULT GETDATE()
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

-- LOCKER ASSIGNMENTS
CREATE TABLE lockerAssignments (
    assignmentId INT IDENTITY(1,1) PRIMARY KEY,
    lockerId INT NOT NULL,
    pinCode CHAR(4) NOT NULL,
    itemDescription NVARCHAR(255),
    assignedAt DATETIME DEFAULT GETDATE(),
    releasedAt DATETIME NULL,
    FOREIGN KEY (lockerId) REFERENCES lockers(lockerId)
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

CREATE TABLE SeatType (
    seatTypeId INT PRIMARY KEY,
    seatTypeName VARCHAR(50),
    price DECIMAL(10,2)
);

CREATE TABLE Seat (
    seatId INT PRIMARY KEY,
    roomId INT,
    seatRow CHAR(1),
    seatColumn INT,
    seatTypeId INT, -- FK
    isActive BIT,
    FOREIGN KEY (seatTypeId) REFERENCES SeatType(seatTypeId)
);

-- TICKETS
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
    FOREIGN KEY (ticketId) REFERENCES tickets(ticketId),
    FOREIGN KEY (seatId) REFERENCES Seat(seatId)
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
CREATE TABLE invoices (
    invoiceId INT PRIMARY KEY IDENTITY(1,1),
    invoiceNumber VARCHAR(50) UNIQUE NOT NULL,
    ticketCode VARCHAR(50),
    employeeId VARCHAR(20),
    customerInfo VARCHAR(200),
    subtotal DECIMAL(10,2),
    tax DECIMAL(10,2),
    discount DECIMAL(10,2),
    totalAmount DECIMAL(10,2),
    paymentMethod VARCHAR(50),
    receivedAmount DECIMAL(10,2),
    changeAmount DECIMAL(10,2),
    createdAt DATETIME DEFAULT GETDATE()
);

EXEC sp_help 'tickets';

-- Ki?m tra c?t ticketCode trong b?ng lockerAssignments
EXEC sp_help 'lockerAssignments';

ALTER TABLE lockerAssignments 
ADD ticketCode VARCHAR(20),
    CONSTRAINT FK_LockerAssignments_Tickets FOREIGN KEY (ticketCode) REFERENCES tickets(ticketCode);

CREATE INDEX idx_locker_assignments_ticket ON lockerAssignments(ticketCode);
