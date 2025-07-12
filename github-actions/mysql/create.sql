-- ======================
-- AUTHENTICATION SERVICE TABLES
-- ======================

CREATE DATABASE IF NOT EXISTS AuthenticationService;
USE AuthenticationService;

-- User table
CREATE TABLE User (
                      ID INT PRIMARY KEY AUTO_INCREMENT,
                      Username VARCHAR(255) NOT NULL UNIQUE,
                      Email VARCHAR(255) NOT NULL UNIQUE,
                      Password VARCHAR(255) NOT NULL,
                      CreateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      LastModificationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      ActiveFlag BOOLEAN DEFAULT TRUE,

                      INDEX idx_username (Username),
                      INDEX idx_email (Email),
                      INDEX idx_active (ActiveFlag)
);

-- Role table
CREATE TABLE Role (
                      ID INT PRIMARY KEY AUTO_INCREMENT,
                      RoleName VARCHAR(100) NOT NULL UNIQUE,
                      RoleDescription TEXT,
                      CreateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      LastModificationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                      INDEX idx_role_name (RoleName)
);

-- UserRole junction table
CREATE TABLE UserRole (
                          ID INT PRIMARY KEY AUTO_INCREMENT,
                          UserID INT NOT NULL,
                          RoleID INT NOT NULL,
                          ActiveFlag BOOLEAN DEFAULT TRUE,
                          CreateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          LastModificationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          FOREIGN KEY (UserID) REFERENCES User(ID) ON DELETE CASCADE,
                          FOREIGN KEY (RoleID) REFERENCES Role(ID) ON DELETE CASCADE,
                          UNIQUE KEY unique_user_role (UserID, RoleID),
                          INDEX idx_user_id (UserID),
                          INDEX idx_role_id (RoleID)
);

-- RegistrationToken table
CREATE TABLE RegistrationToken (
                                   ID INT PRIMARY KEY AUTO_INCREMENT,
                                   Token VARCHAR(255) NOT NULL UNIQUE,
                                   Email VARCHAR(255) NOT NULL,
                                   ExpirationDate DATETIME NOT NULL,
                                   CreateDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   CreateBy INT,

                                   FOREIGN KEY (CreateBy) REFERENCES User(ID) ON DELETE SET NULL,
                                   INDEX idx_token (Token),
                                   INDEX idx_email (Email),
                                   INDEX idx_expiration (ExpirationDate)
);