### **README.md**

```markdown
# Contrôle de Caméra avec Calcul NDVI via MQTT

Ce projet implémente un système permettant de contrôler une caméra sur une Raspberry Pi via MQTT, de capturer une photo, de calculer l'indice NDVI, et d'envoyer automatiquement le résultat à un client distant (par exemple, une machine Windows).

---

## **Table des Matières**
1. [Description du Projet](#description-du-projet)
2. [Prérequis](#prérequis)
3. [Installation](#installation)
4. [Structure du Projet](#structure-du-projet)
5. [Fonctionnement](#fonctionnement)
6. [Tester le Système](#tester-le-système)
7. [Extensions Futures](#extensions-futures)

---

## **Description du Projet**
Ce projet utilise :
- Une **Raspberry Pi** équipée d'une caméra pour capturer des photos.
- **MQTT** (Mosquitto) comme protocole de communication pour envoyer des commandes et recevoir des résultats.
- Des outils Python pour :
  - Capturer une photo via `libcamera`.
  - Calculer l'indice NDVI (Normalized Difference Vegetation Index) pour analyser la santé des plantes.
  - Publier les résultats sur un broker MQTT.

Le résultat est envoyé à un client distant qui peut recevoir et afficher les données de manière automatique.

---

## **Prérequis**

### **Matériel**
- Une Raspberry Pi avec une caméra compatible configurée.
- Un ordinateur distant (par exemple, Windows ou Linux) pour envoyer des commandes et recevoir les résultats.

### **Logiciels**
- **Raspberry Pi** :
  - Mosquitto (broker MQTT) installé et configuré.
  - Python 3 avec les bibliothèques suivantes :
    - `paho-mqtt`
    - `opencv-python`
    - `numpy`

- **Windows** :
  - Mosquitto client installé pour envoyer et recevoir des messages MQTT.

---

## **Installation**

### **1. Configuration de Mosquitto sur Raspberry Pi**
1. Installez Mosquitto et ses clients :
   ```bash
   sudo apt update
   sudo apt install mosquitto mosquitto-clients
   ```
2. Configurez le fichier `/etc/mosquitto/mosquitto.conf` :
   ```bash
   sudo nano /etc/mosquitto/mosquitto.conf
   ```
   Ajoutez les lignes suivantes :
   ```
   listener 1883
   allow_anonymous true
   ```
3. Redémarrez Mosquitto :
   ```bash
   sudo systemctl restart mosquitto
   ```

### **2. Installer les dépendances Python sur la Raspberry Pi**
Installez les bibliothèques nécessaires :
```bash
pip3 install paho-mqtt opencv-python numpy
```

### **3. Configuration des Clients MQTT sur Windows**
1. Téléchargez Mosquitto depuis [mosquitto.org](https://mosquitto.org/download/).
2. Installez Mosquitto en activant les outils en ligne de commande.
3. Ajoutez Mosquitto au `PATH` pour exécuter `mosquitto_pub` et `mosquitto_sub` depuis n'importe quel terminal.

---

## **Structure du Projet**

```
├── raspberry_handler.py   # Script principal pour la Raspberry Pi
├── README.md              # Documentation
└── photo/                 # Répertoire où les photos capturées sont enregistrées
```

---

## **Fonctionnement**

1. **Commandes MQTT :**
   - Le client Windows publie une commande sur le topic `camera/command` pour déclencher une photo.
   - Exemple :
     ```bash
     mosquitto_pub -h <IP_Raspberry> -t camera/command -m "take_photo"
     ```

2. **Capture et Traitement :**
   - La Raspberry Pi :
     - Prend une photo.
     - Calcule l'indice NDVI (Normalized Difference Vegetation Index) pour évaluer la santé des plantes.

3. **Résultats MQTT :**
   - Le résultat est publié sur le topic `raspberry/result`.
   - Exemple :
     ```bash
     NDVI moyen : 0.68. La plante est en bonne santé.
     ```

---

## **Tester le Système**

### **1. Sur la Raspberry Pi**
1. Lancer le broker MQTT :
   ```bash
   sudo systemctl start mosquitto
   ```
2. Exécuter le script Python :
   ```bash
   python3 raspberry_handler.py
   ```

### **2. Depuis Windows**
1. Envoyer une commande pour capturer une photo :
   ```bash
   mosquitto_pub -h <IP_Raspberry> -t camera/command -m "take_photo"
   ```
2. Écouter les résultats publiés par la Raspberry Pi :
   ```bash
   mosquitto_sub -h <IP_Raspberry> -t raspberry/result
   ```

---

## **Extensions Futures**
- **Base de données :** Ajouter un système pour sauvegarder les résultats dans une base de données.
- **Interface Utilisateur :** Créer une interface graphique (par exemple, avec Streamlit ou Flask) pour afficher les résultats.
- **Analyse Multispectrale :** Intégrer des capteurs pour capturer des bandes spectrales supplémentaires.

---

## **Contributions**
Les contributions sont les bienvenues ! Veuillez soumettre un pull request ou ouvrir une issue pour toute suggestion.

---

## **Licence**
Ce projet est sous licence MIT. Consultez le fichier `LICENSE` pour plus de détails.
```

---

### **Comment utiliser ce README :**
1. **Placez ce fichier `README.md` dans le répertoire de votre branche GitHub.**
2. Ajoutez vos fichiers Python et configurez correctement le dépôt.
3. **Poussez la branche sur GitHub** :
   ```bash
   git add .
   git commit -m "Ajout du projet NDVI via MQTT"
   git push origin <branch_name>
   ```

