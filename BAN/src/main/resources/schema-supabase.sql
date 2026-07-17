CREATE TABLE IF NOT EXISTS ban (
     id TEXT PRIMARY KEY,
     numero INTEGER,
     rep TEXT,
     nom_voie TEXT,
     code_postal INTEGER,
     code_insee TEXT,
     nom_commune TEXT,
     x REAL,
     y REAL,
     lon REAL,
     lat REAL
);