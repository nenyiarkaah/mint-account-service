drop table [mint_test].dbo.account

create table account
(
    id int identity
        primary key,
    name varchar(max) not null,
    account_type varchar(max) not null,
    company varchar(max) not null,
    is_my_account bit not null,
    is_active bit not null,
    mapping_file varchar(max) not null
)
go

-------------------------------------------------
SELECT
    AccountId [id],
    AccountName [name],
    AccountType [account_type],
    Company [company],
    MyAccount [is_my_account],
    IsActive [is_active],
    COALESCE(MappingFile, '') [mapping_file]
INTO [mint_test].dbo.account
FROM [Mint].dbo.Accounts

ALTER TABLE [mint_test].dbo.account
    ADD CONSTRAINT PK_account PRIMARY KEY (id);
    ALTER COLUMN name varchar(max) not null;
    ALTER COLUMN account_type varchar(max) not null;
    ALTER COLUMN company varchar(max) not null;
    ALTER COLUMN mapping_file varchar(max) not null;
