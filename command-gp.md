
#### Commande GlobalPlateform

- `-i`: Afficher les informations générales de la carte (clair pour une partie des données).
- `-l`: Lister les applets sur la carte (sécurisé), 
    --key: Master key
    --key-enc
    --key-dek
    --key-mac
- ``--install`: Installer un applet ou un package, à partir d'un .cap

- `-a`: Commande APDU sans contrôle de sécurité 
- `-s`: Commande APDU avec un canal sécurisé.

| **Caractéristique**        | **ISD**                            | **SSD**                       |
|-----------------------------|-------------------------------------|-------------------------------|
| **Création**               | Créé par le fabricant.             | Créé par l’ISD.              |
| **Fonction principale**    | Gérer la carte globalement.        | Déléguer des permissions.    |
| **Exemple d’utilisation**  | Installer des applets système.     | Gérer des applets bancaires. |
