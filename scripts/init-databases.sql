-- Initialize databases for Swap Lifecycle Management Service
-- This script creates the required databases and basic tables

-- Create CashflowDB database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'CashflowDB')
BEGIN
    CREATE DATABASE CashflowDB;
END
GO

-- Create TradeDB database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'TradeDB')
BEGIN
    CREATE DATABASE TradeDB;
END
GO

-- Create EventStoreDB database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'EventStoreDB')
BEGIN
    CREATE DATABASE EventStoreDB;
END
GO

-- Use CashflowDB
USE CashflowDB;
GO

-- Create cashflows table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[cashflows]') AND type in (N'U'))
BEGIN
    CREATE TABLE cashflows (
        cashflow_id NVARCHAR(50) PRIMARY KEY,
        trade_id NVARCHAR(50) NOT NULL,
        position_id NVARCHAR(50) NOT NULL,
        lot_id NVARCHAR(50),
        cashflow_type NVARCHAR(20) NOT NULL,
        payer_party_id NVARCHAR(50) NOT NULL,
        receiver_party_id NVARCHAR(50) NOT NULL,
        currency NVARCHAR(3) NOT NULL,
        amount DECIMAL(18,2) NOT NULL,
        settlement_date DATE NOT NULL,
        payment_type NVARCHAR(20) NOT NULL,
        cashflow_status NVARCHAR(20) NOT NULL,
        status_transition_timestamp DATETIME2,
        settlement_reference NVARCHAR(100),
        origin_event_id NVARCHAR(50) NOT NULL,
        revision_number INT NOT NULL DEFAULT 1,
        is_cancelled BIT NOT NULL DEFAULT 0,
        created_timestamp DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
        updated_timestamp DATETIME2 NOT NULL DEFAULT GETUTCDATE()
    );
END
GO

PRINT 'Database initialization completed successfully!';
