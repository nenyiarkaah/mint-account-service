-- Creates the login AbolrousHazem with password '340$Uuxwp7Mcxo7Khy'.
CREATE LOGIN AbolrousHazem
    WITH PASSWORD = '340$Uuxwp7Mcxo7Khy';
GO

-- Creates a database user for the login created above.
CREATE USER AbolrousHazem FOR LOGIN AbolrousHazem;
GO

USE [mint_test]
GO
EXEC sp_addrolemember N'db_datareader', N'AbolrousHazem'
EXEC sp_addrolemember N'db_datawriter', N'AbolrousHazem'
EXEC sp_addrolemember N'db_ddladmin', N'AbolrousHazem'

GO