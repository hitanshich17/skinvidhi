-- Sun protection factor of sunscreens, as labelled ("SPF 50+" is stored as 50). Routines only use SPF 30+.
ALTER TABLE products ADD COLUMN spf INTEGER CHECK (spf BETWEEN 1 AND 100);
