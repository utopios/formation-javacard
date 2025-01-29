### **Sujet de TP : "Smart Metering and Smart Grid avec communication inter-applications en Java Card"**

#### **Contexte**

Le Smart Grid (réseau électrique intelligent) repose sur la collecte et l'analyse en temps réel des données de consommation provenant de compteurs intelligents (Smart Meters). Ces données sont utilisées pour optimiser la gestion de l'énergie à l'échelle locale et globale.

Dans ce TP, nous allons implémenter un système simplifié sur une carte Java Card, comprenant :

1. **MeterApplet** : Un compteur intelligent simulant la mesure de la consommation énergétique.
2. **AggregatorApplet** : Un agrégateur de données collectant les informations des différents **MeterApplets**.
3. **GridApplet** : Une entité centrale responsable de l'analyse des données agrégées et de la prise de décision.

Les applets doivent communiquer directement entre elles à l'aide des interfaces partageables Java Card (`Shareable`). La communication sera sécurisée via des mécanismes d'authentification, de chiffrement, et de signature pour garantir la confidentialité et l'intégrité des données.

---

#### **Objectifs pédagogiques**

1. Découvrir et manipuler les mécanismes de **communication inter-applications** sur Java Card.
2. Implémenter un système distribué avec **interfaces partageables (SIO)**.
3. Renforcer la sécurité des échanges grâce à des concepts tels que :
   - Authentification mutuelle.
   - Chiffrement AES.
   - Signature des messages pour garantir leur intégrité.
4. Simuler un vrai cas pratique lié à la gestion de l'énergie.

---

#### **Scénario**

1. Les **MeterApplets** mesurent la consommation énergétique (valeurs simulées).
2. L'**AggregatorApplet** interroge directement les **MeterApplets** pour récupérer leurs données, les agrège, et fournit ces données agrégées au **GridApplet**.
3. Le **GridApplet** interagit uniquement avec l'**AggregatorApplet** pour obtenir les données consolidées, les analyser, et retourner un rapport global.

---

#### **Contraintes**

- Les communications entre applets doivent être **directes** et passer par des interfaces partageables.
- La sécurité doit inclure :
  - **Authentification** des applets avant tout échange de données.
  - **Chiffrement** des données transmises (par exemple avec AES).
  - **Signature** pour garantir l'intégrité des données et éviter les attaques de type "replay".
- Le client ne doit interagir qu'avec le **GridApplet**.

---

#### **Architecture des applets**

1. **MeterApplet** :
   - Fournit une interface partageable pour permettre à d'autres applets de lire les données de consommation.
   - Chiffre ses données avant de les partager.

2. **AggregatorApplet** :
   - Accède directement aux **MeterApplets** via leurs interfaces partageables.
   - Agrège les données reçues.
   - Protège les données agrégées en les chiffrant avant de les transmettre au **GridApplet**.

3. **GridApplet** :
   - Orchestre tout le système.
   - Authentifie l'**AggregatorApplet** avant de lui demander les données agrégées.
   - Analyse les données reçues et retourne un rapport global au client.

---

#### **Tâches demandées**

1. Implémenter le **MeterApplet** avec une interface partageable et des mécanismes de sécurité (authentification et chiffrement).
2. Implémenter l'**AggregatorApplet**, capable de :
   - Collecter les données directement depuis les **MeterApplets**.
   - Déchiffrer et agréger les données.
3. Implémenter le **GridApplet**, qui :
   - Interagit uniquement avec l'**AggregatorApplet**.
   - Assure la sécurité des échanges via signature des requêtes.
4. Tester le flux complet où :
   - Le client parle uniquement au **GridApplet**.
   - Les interactions entre les applets (MeterApplet, AggregatorApplet, GridApplet) sont totalement automatisées.


