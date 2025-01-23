### **1. Sélection d’un Applet ou d’un AID**
#### Commande :
```plaintext
00 A4 04 00 08 A0 00 00 00 62 03 01 0C 01
```
#### Description :
- **But :** Sélectionne une application ou un fichier en fonction de son AID.
- **Analyse :**
  - **CLA :** `00` (ISO standard).
  - **INS :** `A4` (SELECT).
  - **P1 :** `04` (Sélection par AID).
  - **P2 :** `00` (Pas de contexte supplémentaire).
  - **Lc :** `08` (Longueur de l'AID envoyé).
  - **Data :** `A0 00 00 00 62 03 01 0C 01` (AID à sélectionner).


---

### **2. Vérification d’un Code PIN**
#### Commande :
```plaintext
80 20 00 00 05 31 32 33 34 35
```
#### Description :
- **But :** Vérifie un code PIN (dans cet exemple : `12345`).
- **Analyse :**
  - **CLA :** `80` (Propriétaire).
  - **INS :** `20` (VERIFY).
  - **P1 et P2 :** `00 00` (Pas de contexte spécifique).
  - **Lc :** `05` (Longueur du PIN envoyé).
  - **Data :** `31 32 33 34 35` (Code PIN en ASCII).

---

### **3. Lecture d’un Fichier**
#### Commande :
```plaintext
00 B0 00 00 10
```
#### Description :
- **But :** Lit 16 octets (taille : `10` en hexadécimal) à partir du début d’un fichier sélectionné.
- **Analyse :**
  - **CLA :** `00` (ISO standard).
  - **INS :** `B0` (READ BINARY).
  - **P1 et P2 :** `00 00` (Offset de départ à 0).
  - **Le :** `10` (Demande de 16 octets).

---

### **4. Initialisation d'une session sécurisée**
#### Commande :
```plaintext
80 50 00 00 08 1F FA 9B 04 50 33 77 44
```
#### Description :
- **But :** Démarrer une session sécurisée.
- **Analyse :**
  - **CLA :** `80` (ISO Propriétaire).
  - **INS :** `50` (INITIALIZE UPDATE).
  - **P1 et P2 :** `00 00` (Offset de départ à 0).
  - **Le :** `08` (longueur des données).
