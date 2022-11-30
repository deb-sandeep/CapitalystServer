SET @SPLIT_RATIO = 5 ;
SET @SYMBOL_NSE  = 'SRF' collate utf8mb4_unicode_ci ;
SET @SPLIT_DATE  = '2021-10-13' ;

SELECT id, symbol, date, open, high, low, close, prev_close
FROM historic_eq_data
WHERE symbol = @SYMBOL_NSE
ORDER BY date DESC ;

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
    symbol=@SYMBOL_NSE  ;