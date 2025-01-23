### **Sujet : Développement et Déploiement d'une Applet JavaCard**

#### **Contexte :**
Une carte JavaCard est un microcontrôleur sécurisé capable d'exécuter des applets. Dans cet exercice, vous allez développer une applet simple pour répondre à différentes commandes APDU et la déployer sur un simulateur.

---

#### **Objectif :**
Créer une applet JavaCard appelée `SimpleApplet` capable de :
1. Répondre à une commande de sélection.
2. Traiter trois types de commandes APDU, chacune associée à une fonctionnalité simple.
3. Être testée via des commandes interactives envoyées depuis un client Java.

---

#### **Spécifications :**

1. **AID de l'applet :**
   - Nom : `SimpleApplet`
   - AID : `A00000006203010C0101` (arbitraire pour cet exercice).

2. **Commandes APDU prises en charge :**
   - **Instruction INS 0x10** : Retourne un message d'accueil "Hello, JavaCard!".
   - **Instruction INS 0x20** : Reçoit des données en entrée et les renvoie inchangées.
   - **Instruction INS 0x30** : Incrémente un compteur interne à chaque appel et retourne la nouvelle valeur.

3. **Structure de la commande APDU :**
   - **CLA** : 0x80 (class propriétaire).
   - **INS** : L'une des trois instructions spécifiées ci-dessus.
   - **P1/P2** : Fixés à 0x00 pour cet exercice.
   - **Lc** : Longueur des données (si présentes).
   - **Data** : Données transmises (si présentes).

4. **Structure de la réponse APDU :**
   - Données retournées selon l'instruction.
   - SW (Status Word) :
     - **0x9000** : Succès.
     - **0x6D00** : Instruction non supportée.
     - **0x6A80** : Données invalides.

---

#### **Travail à réaliser :**

1. **Développement de l'applet :**
   - Implémentez la classe `SimpleApplet` en respectant les spécifications.
   - Utilisez un tableau `byte[]` pour gérer le retour des données.
   - Implémentez un compteur `short` pour l'instruction 0x30.

2. **Déploiement de l'applet :**
   - Compilez le fichier `.java` en `.cap` (format requis par JavaCard).
   - Déployez l'applet sur un simulateur JavaCard en utilisant un client compatible.

3. **Test de l'applet :**
   - Envoyez les commandes APDU suivantes via un client interactif :
     - **Sélectionner l'applet** : `00 A4 04 00 0A A0 00 00 00 62 03 01 0C 01 01`
     - **INS 0x10** : `80 10 00 00 00`
     - **INS 0x20** avec données : `80 20 00 00 04 41 42 43 44`
     - **INS 0x30** : `80 30 00 00 00`

---

#### **Résultats attendus :**

1. **INS 0x10** : Retourne les octets représentant la chaîne "Hello, JavaCard!" suivi de SW=0x9000.
2. **INS 0x20** : Retourne les données reçues en entrée (ex. `41 42 43 44`) suivi de SW=0x9000.
3. **INS 0x30** : Retourne la valeur incrémentée du compteur (ex. `00 01`, `00 02`, etc.) suivi de SW=0x9000.
4. Toute autre instruction doit retourner SW=0x6D00.
