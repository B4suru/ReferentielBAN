PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;
PRAGMA cache_size = -3000000;
PRAGMA temp_store = MEMORY;

DROP TABLE IF EXISTS ban;

CREATE TABLE ban (
     id TEXT PRIMARY KEY,
     numero INTEGER,
     rep TEXT,
     nom_voie TEXT,
     code_postal INTEGER,
     code_insee INTEGER,
     nom_commune TEXT,
     x REAL,
     y REAL,
     lon REAL,
     lat REAL
);