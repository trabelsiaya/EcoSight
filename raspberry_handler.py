# -*- coding: utf-8 -*-
import paho.mqtt.client as mqtt
import subprocess
import os
from time import strftime
import cv2
import numpy as np

photo_dir = "/home/pi/Desktop/photo"
os.makedirs(photo_dir, exist_ok=True)

def calculate_ndvi(photo_path):
    try:
        image = cv2.imread(photo_path)
        if image is None:
            return "Erreur : Impossible de charger l'image."

        hsv_image = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
        red_channel = image[:, :, 2]
        nir_channel = hsv_image[:, :, 1]

        denominator = nir_channel.astype(float) + red_channel.astype(float)
        ndvi = np.where(denominator == 0, 0, (nir_channel.astype(float) - red_channel.astype(float)) / denominator)

        avg_ndvi = np.mean(ndvi)

        if avg_ndvi > 0.5:
            return f"NDVI moyen : {avg_ndvi:.2f}. La plante est en bonne sante."
        else:
            return f"NDVI moyen : {avg_ndvi:.2f}. La plante montre des signes de stress."
    except Exception as e:
        return f"Erreur lors du calcul NDVI : {e}"

def on_message(client, userdata, msg):
    if msg.payload.decode() == "take_photo":
        print("Commande recue : Prendre une photo.")
        try:
            photo_name = strftime("photo_%Y-%m-%d_%H-%M-%S.jpg")
            photo_path = os.path.join(photo_dir, photo_name)
            subprocess.run(["libcamera-jpeg", "-o", photo_path], check=True)
            print(f"Photo prise et enregistree sous {photo_path}.")
            result = calculate_ndvi(photo_path)
            client.publish("raspberry/result", result)
            print(f"Resultat envoye : {result}")
        except Exception as e:
            error_message = f"Erreur : {e}"
            print(error_message)
            client.publish("raspberry/result", error_message)

def on_connect(client, userdata, flags, rc):
    print(f"Connecte avec le code de resultat {rc}")
    client.subscribe("camera/command")

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

try:
    client.connect("localhost", 1883, 60)
    print("Connecte au broker MQTT sur localhost.")
except Exception as e:
    print(f"Erreur lors de la connexion au broker MQTT : {e}")

print("En attente de commandes sur le topic 'camera/command'...")
client.loop_forever()
