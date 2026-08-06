PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;
PRAGMA cache_size = -3000000;
PRAGMA temp_store = MEMORY;

DROP TABLE IF EXISTS ban;
DROP TABLE IF EXISTS dvf;
DROP TABLE IF EXISTS communes;

CREATE TABLE ban (
--CREATE TABLE IF NOT EXISTS  ban(
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
     lat REAL,
     hash TEXT
);

CREATE TABLE dvf (
     id_mutation TEXT,
     date_mutation DATE,
     numero_disposition INTEGER,
     nature_mutation TEXT,
     valeur_fonciere REAL,
     adresse_numero INTEGER,
     adresse_suffixe TEXT,
     adresse_nom_voie TEXT,
     adresse_code_voie TEXT,
     code_postal INTEGER,
     code_commune TEXT,
     nom_commune TEXT,
     code_departement TEXT,
     ancien_code_commune TEXT,
     ancien_nom_commune TEXT,
     id_parcelle TEXT,
     ancien_id_parcelle TEXT,
     numero_volume TEXT,
     lot1_numero TEXT,
     lot1_surface_carrez REAL,
     lot2_numero TEXT,
     lot2_surface_carrez REAL,
     lot3_numero TEXT,
     lot3_surface_carrez REAL,
     lot4_numero TEXT,
     lot4_surface_carrez REAL,
     lot5_numero TEXT,
     lot5_surface_carrez REAL,
     nombre_lots INTEGER,
     code_type_local INTEGER,
     type_local TEXT,
     surface_reelle_bati INTEGER,
     nombre_pieces_principales INTEGER,
     code_nature_culture TEXT,
     nature_culture TEXT,
     code_nature_culture_speciale TEXT,
     nature_culture_speciale TEXT,
     surface_terrain INTEGER,
     longitude REAL,
     latitude REAL
)

CREATE TABLE communes (
    code_insee VARCHAR(5) PRIMARY KEY,
    nom TEXT NOT NULL,
    departement CHAR(3),
    region CHAR(2),
    epci VARCHAR(9),
    geometry JSONB NOT NULL
);