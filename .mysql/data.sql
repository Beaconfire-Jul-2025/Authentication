USE AuthenticationService;

INSERT INTO User (Username, Email, Password, ActiveFlag) VALUES
                                                             ('HR', 'HR@example.com', '$2a$10$skzrTD6WdaCLsP/3Ujkpp.v37u8elsrn7BOcenY5Vhqsq/PzJtL2C', TRUE), -- adminadmin
                                                             ('user1', 'user1@example.com', '$2a$10$znERm4Oyojv5U5HX9riTsOr3SUcqvUNJewGx/OCHplgARe7FVfBHq', TRUE), -- password123
                                                             ('user2', 'user2@example.com', '$2a$10$znERm4Oyojv5U5HX9riTsOr3SUcqvUNJewGx/OCHplgARe7FVfBHq', FALSE); -- password123

INSERT INTO Role (RoleName, RoleDescription) VALUES
                                                 ('ROLE_HR', 'HR with full system access'),
                                                 ('ROLE_EMPLOYEE', 'Standard authenticated employee');

INSERT INTO UserRole (UserID, RoleID, ActiveFlag) VALUES
                                                      ((SELECT ID FROM User WHERE Username = 'HR'), (SELECT ID FROM Role WHERE RoleName = 'ROLE_HR'), TRUE),
                                                      ((SELECT ID FROM User WHERE Username = 'user1'), (SELECT ID FROM Role WHERE RoleName = 'ROLE_EMPLOYEE'), TRUE),
                                                      ((SELECT ID FROM User WHERE Username = 'user2'), (SELECT ID FROM Role WHERE RoleName = 'ROLE_EMPLOYEE'), TRUE);

INSERT INTO RegistrationToken (Token, Email, ExpirationDate, CreateBy) VALUES
                                                                           ('token_abc123', 'newuser1@example.com', '2025-07-15 23:59:59', (SELECT ID FROM User WHERE Username = 'HR')),
                                                                           ('token_def456', 'newuser2@example.com', '2025-07-20 23:59:59', (SELECT ID FROM User WHERE Username = 'HR'));