# Gestionnaire de QCM
## Contexte
Projet réalisé en L3 informatique (2025-2026). Création d'un gestionnaire de QCM avec Gradle et files de messages MQTT.
- en collaboration avec OYANI Muzhgan
- université : URCA - UFR Sciences Exactes et Naturelles
- professeur référent : FLAUZAC Olivier
- identifiant de la matière : info0502 - Introduction à la programmation répartie

Pour plus de détails sur l'implémentation, un compte-rendu du projet est disponible dans le dépôt.
## Utilisation
__Prérequis__
- JDK 21
- Gradle
- Broker MQTT (Mosquitto)

__Compilation et exécution__
1. Installer et lancer un broker MQTT
2. Dans un premier terminal
```
cd server
./gradlew build
./graldew app:run --args="tcp://[adresse IP et port d'un broker MQTT]"
```
3. Dans un deuxième terminal
```
cd client
./gradlew build
./graldew app:run --args="tcp://[adresse IP et port d'un broker MQTT]" --console=plain
```
## Licence
Ce projet a été réalisé en collaboration avec _OYANI Muzhgan_

Le code est mis à disposition pour __consultation et test local uniquement__. Toute reproduction ou utilisation commerciale est interdite sans notre accord préalable. Voir le fichier [LICENSE](LICENSE) pour plus de détails.
