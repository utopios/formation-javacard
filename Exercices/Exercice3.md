Exercice : E-Wallet (Gestion d’un solde persistant)
Contexte
Vous devez créer une applet Java Card qui gère un « Wallet » (porte-monnaie électronique). L’objectif est de manipuler la mémoire persistante de la carte (EEPROM) pour conserver le solde après un reset de la carte.

Spécifications fonctionnelles
Champ persistant balance :

Stocké en mémoire non volatile (EEPROM).
Conserve sa valeur même après un reset ou retrait de la carte.
Commandes APDU à implémenter :

INS_GET_BALANCE : renvoyer la valeur courante de balance.
INS_CREDIT : ajouter un montant donné au solde balance.
INS_DEBIT : retirer un montant donné du solde balance.
(Optionnel) INS_RESET_BALANCE : remettre le solde à zéro.
Gestion des transactions :

Les opérations de mise à jour de balance doivent être protégées par une transaction pour éviter tout risque de corruption en cas de retrait brutal de la carte.
Vérifications :

Empêcher le débit si le solde est insuffisant.
Gérer proprement les erreurs (par exemple, si le montant transmis n’est pas valide).
Test :

Vérifier que la valeur de balance est bien persistante (redémarrer la carte / simulateur, puis relire le solde).
Tester plusieurs crédits / débits et observer l’évolution du solde.