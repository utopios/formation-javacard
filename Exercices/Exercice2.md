**Exercice : Gestion d’un PIN et d’un PUK sur Java Card**  
1. Créez une applet Java Card qui gère un code PIN protégé par un nombre limité de tentatives.  
2. Lorsque le PIN est bloqué (après plusieurs erreurs), proposez un code PUK (avec ses propres tentatives limitées) permettant de :  
   - Vérifier le PUK.  
   - Débloquer et réinitialiser le PIN si le PUK est correct.  
   - Bloquer définitivement la carte en cas de tentatives de PUK épuisées.  
3. Définissez clairement les commandes APDU pour :  
   - Vérifier le PIN.  
   - Débloquer le PIN via le PUK.  
   - (Optionnel) Mettre à jour le PIN ou le PUK.