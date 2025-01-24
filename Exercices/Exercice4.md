**Sujet** : Implémentation d'une Applet Java Card pour l'Authentification Mutuelle avec ECDH et Signature
**Contexte** : Développer une applet Java Card permettant une authentification mutuelle entre un terminal et la carte. La carte génère une paire de clés ECC pour établir un secret partagé via ECDH. Elle signe également des challenges pour prouver son identité au terminal.

Objectifs :

Générer et gérer une paire de clés ECC sur la carte.
Effectuer un échange de clé sécurisé via ECDH pour établir une clé AES de session.
Signer un challenge avec une clé privée ECC pour prouver l'identité de la carte.
Vérifier un challenge signé par le terminal