# Référentiel BAN

Application de traitement et d'import de la Base Adresse Nationale (BAN) à partir de fichiers CSV.

## Étape 1 — Cloner le projet

```bash
git clone 
cd ReferentielBAN
```

## Étape 2 — Lancer le projet

Le projet doit d'abord être compilé (voir section Build), puis exécuté avec la commande suivante :

```bash
java -jar BAN-0.0.1-SNAPSHOT.jar
```

Une fois lancé, l'application vous demandera :
1. Le chemin du fichier CSV à traiter
2. Un code postal (optionnel, pour filtrer les données)
3. Un code INSEE (optionnel, pour filtrer les données)

Vous pourrez ensuite enchaîner le traitement de plusieurs fichiers à la suite.

## Logs

Un fichier `Logs.txt` est généré à la racine du projet et contient le détail de chaque traitement (fichier traité, filtres appliqués, durée, lignes lues/écrites/filtrées/supprimées).
