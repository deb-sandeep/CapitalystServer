-- This script applying a stock split to current and historic data
-- stored in Capitalyst.
--
-- WARNING: This seriously modifies the core trade data. 
--          -> DO NOT run this while drunk
--          -> Take a backup of the database before running it.

SET @SPLIT_RATIO.   = 10 ;
SET @SYMBOL_IDIRECT = 'NIFBEE' collate utf8mb4_unicode_ci ;
SET @SYMBOL_NSE     = 'NIFTYBEES' collate utf8mb4_unicode_ci ;
SET @SPLIT_DATE     = '2019-12-19' ;

-- EQUITY_HOLDING --------------------------------------------------------------
SELECT *
FROM equity_holding
WHERE 
    quantity > 0 AND
    symbol_icici = @SYMBOL_IDIRECT ;

UPDATE equity_holding
SET
    quantity = quantity*@SPLIT_RATIO,
    avg_cost_price = avg_cost_price/@SPLIT_RATIO
WHERE 
    quantity > 0 AND
    symbol_icici = @SYMBOL_IDIRECT ;

-- EQUITY_TRADE ----------------------------------------------------------------
SELECT *
FROM equity_trade
WHERE 
    trade_date < @SPLIT_DATE AND
    holding_id in (
        SELECT id
        FROM equity_holding
        WHERE symbol_icici = @SYMBOL_IDIRECT
    ) 
ORDER BY
    trade_date DESC ;

UPDATE equity_trade
SET 
    quantity = quantity*@SPLIT_RATIO
WHERE 
    trade_date < @SPLIT_DATE AND
    holding_id in (
        SELECT id
        FROM equity_holding
        WHERE symbol_icici = @SYMBOL_IDIRECT
    ) ;
    
-- EQUITY_TXN ------------------------------------------------------------------
SELECT *
FROM equity_txn
WHERE
    txn_date < @SPLIT_DATE AND
    holding_id in (
        SELECT id
        FROM equity_holding
        WHERE symbol_icici = @SYMBOL_IDIRECT
    ) 
ORDER BY
    txn_date DESC ;

UPDATE equity_txn
SET
    quantity = quantity*@SPLIT_RATIO,
    txn_price = txn_price/@SPLIT_RATIO
WHERE
    txn_date < @SPLIT_DATE AND
    holding_id in (
        SELECT id
        FROM equity_holding
        WHERE symbol_icici = @SYMBOL_IDIRECT
    ) ;

-- EQUITY_DAILY_GAIN -----------------------------------------------------------
SELECT *
FROM equity_daily_gain
WHERE 
    date < @SPLIT_DATE AND
    holding_id in (
        SELECT id
        FROM equity_holding
        WHERE symbol_icici = @SYMBOL_IDIRECT
    ) ;

UPDATE equity_daily_gain
SET 
    quantity = quantity*@SPLIT_RATIO,
    closing_unit_price = closing_unit_price/@SPLIT_RATIO
WHERE 
    date < @SPLIT_DATE AND
    holding_id in (
        SELECT id
        FROM equity_holding
        WHERE symbol_icici = @SYMBOL_IDIRECT
    ) ;

-- EQUITY_INDICATORS -----------------------------------------------------------
SELECT *
FROM equity_indicators
WHERE
    as_on_date < @SPLIT_DATE AND
    symbol_nse = @SYMBOL_NSE ;

UPDATE equity_indicators
SET 
    current_price = current_price/@SPLIT_RATIO
WHERE
    as_on_date < @SPLIT_DATE AND
    symbol_nse = @SYMBOL_NSE ;

-- EQUITY_INDICATORS_HIST ------------------------------------------------------
SELECT *
FROM equity_indicators_hist
WHERE
    as_on_date < @SPLIT_DATE AND
    symbol_nse = @SYMBOL_NSE ;

UPDATE equity_indicators_hist
SET 
    current_price = current_price/@SPLIT_RATIO
WHERE 
    as_on_date < @SPLIT_DATE AND
    symbol_nse = @SYMBOL_NSE ;

-- HISTORIC_EQ_DATA ------------------------------------------------------------
SELECT *
FROM historic_eq_data
WHERE 
    date < @SPLIT_DATE AND
    symbol=@SYMBOL_NSE 
ORDER BY
    date DESC ;

UPDATE historic_eq_data
SET
    open            = open/@SPLIT_RATIO,
    high            = high/@SPLIT_RATIO,
    low             = low/@SPLIT_RATIO,
    close           = close/@SPLIT_RATIO,
    prev_close      = prev_close/@SPLIT_RATIO,
    total_trade_qty = total_trade_qty*@SPLIT_RATIO 
WHERE 
    date < @SPLIT_DATE AND
    symbol=@SYMBOL_NSE ;

-- EQUITY_MASTER ---------------------------------------------------------------
UPDATE equity_master
SET
    close = close/@SPLIT_RATIO,
    prev_close = prev_close/@SPLIT_RATIO,
    high_52w = high_52w/@SPLIT_RATIO,
    low_52w = low_52w/@SPLIT_RATIO
WHERE
    symbol = @SYMBOL_NSE ;
    
    
    
    
    
    
    
    
    
    