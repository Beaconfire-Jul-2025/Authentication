-- ======================
-- AUTHENTICATION SERVICE TABLES
-- ======================

CREATE DATABASE IF NOT EXISTS AuthenticationService;
USE AuthenticationService;

-- User table
CREATE TABLE User (
                      id INT PRIMARY KEY AUTO_INCREMENT,
                      username VARCHAR(255) NOT NULL UNIQUE,
                      email VARCHAR(255) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      lastModificationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      activeFlag BOOLEAN DEFAULT TRUE,

                      INDEX idx_username (username),
                      INDEX idx_email (email),
                      INDEX idx_active (activeFlag)
);

-- Role table
CREATE TABLE Role (
                      id INT PRIMARY KEY AUTO_INCREMENT,
                      roleName VARCHAR(100) NOT NULL UNIQUE,
                      roleDescription TEXT,
                      createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      lastModificationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                      INDEX idx_role_name (roleName)
);

-- UserRole junction table
CREATE TABLE UserRole (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          userId INT NOT NULL,
                          roleId INT NOT NULL,
                          activeFlag BOOLEAN DEFAULT TRUE,
                          createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          lastModificationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
                          FOREIGN KEY (roleId) REFERENCES Role(id) ON DELETE CASCADE,
                          UNIQUE KEY unique_user_role (userId, roleId),
                          INDEX idx_user_id (userId),
                          INDEX idx_role_id (roleId)
);

-- RegistrationToken table
CREATE TABLE RegistrationToken (
                                   id INT PRIMARY KEY AUTO_INCREMENT,
                                   token VARCHAR(255) NOT NULL UNIQUE,
                                   email VARCHAR(255) NOT NULL,
                                   expirationDate DATETIME NOT NULL,
                                   createDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   createBy INT,

                                   FOREIGN KEY (createBy) REFERENCES User(id) ON DELETE SET NULL,
                                   INDEX idx_token (token),
                                   INDEX idx_email (email),
                                   INDEX idx_expiration (expirationDate)
);
