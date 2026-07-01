# Référentiel BAN

Application de traitement et d'import de la Base Adresse Nationale (BAN) à partir de fichiers CSV.

## Prérequis

- Java (JDK)
- Maven (pour les commandes `mvn`)

## Étape 1 — Cloner le projet

```bash
git clone https://github.com/B4suru/ReferentielBAN.git
cd ReferentielBAN/BAN
```

## Étape 2 — Build du projet

```bash
mvn clean install -DskipTests
```

## Étape 3 — Lancer le projet

Le projet doit d'abord être compilé, puis exécuté avec la commande suivante :

```bash
java -jar .\target\BAN-0.0.1-SNAPSHOT.jar
```

Une fois lancé, l'application vous demandera :
1. Le chemin du fichier CSV à traiter
2. Un code postal (optionnel, pour filtrer les données)
3. Un code INSEE (optionnel, pour filtrer les données)

Vous pourrez ensuite enchaîner le traitement de plusieurs fichiers à la suite.

Une fois le traitement des fichiers terminé (réponse `n` à la question "Voulez-vous traiter un autre fichier ?"), l'**API reste active** et continue de tourner dans le terminal — c'est normal, elle reste disponible pour être interrogée (voir section API ci-dessous).
 
Pour **arrêter l'application**, retournez dans le terminal et faites `Ctrl + C`.
 
## API
 
Une documentation Swagger est disponible une fois l'application lancée, à l'adresse suivante :
 
```
http://localhost:8080/swagger-ui.html
```

## Logs

Un dossier `Logs/` est créé à la racine du projet. Chaque exécution génère un fichier de log horodaté (`Logs(yyyy-MM-dd'T'HH-mm-ss.SSS).txt`) contenant le détail de chaque traitement (fichier traité, filtres appliqués, durée, lignes lues/écrites/filtrées/supprimées).
