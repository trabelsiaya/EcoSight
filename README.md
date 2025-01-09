
# Camera Control with NDVI Calculation via MQTT

This project implements a system to control a camera on a Raspberry Pi via MQTT, capture a photo, calculate the NDVI index, and automatically send the result to a remote client (e.g., a Windows machine).

---

## **Table of Contents**
1. [Project Description](#project-description)
2. [Requirements](#requirements)
3. [Installation](#installation)
4. [Project Structure](#project-structure)
5. [How It Works](#how-it-works)
6. [Testing the System](#testing-the-system)
---

## **Project Description**
This project uses:
- A **Raspberry Pi** equipped with a camera to capture photos.
- **MQTT** (Mosquitto) as a communication protocol to send commands and receive results.
- Python tools to:
  - Capture a photo using `libcamera`.
  - Calculate the NDVI (Normalized Difference Vegetation Index) to assess plant health.
  - Publish the results to an MQTT broker.

The result is sent to a remote client that can automatically receive and display the data.

---

## **Requirements**

### **Hardware**
- A Raspberry Pi with a configured compatible camera.
- A remote computer (e.g., Windows or Linux) to send commands and receive results.

### **Software**
- **Raspberry Pi**:
  - Mosquitto (MQTT broker) installed and configured.
  - Python 3 with the following libraries:
    - `paho-mqtt`
    - `opencv-python`
    - `numpy`

- **Windows**:
  - Mosquitto client installed to send and receive MQTT messages.

---

## **Installation**

### **1. Setting up Mosquitto on Raspberry Pi**
1. Install Mosquitto and its clients:
   ```bash
   sudo apt update
   sudo apt install mosquitto mosquitto-clients
   ```
2. Configure the file `/etc/mosquitto/mosquitto.conf`:
   ```bash
   sudo nano /etc/mosquitto/mosquitto.conf
   ```
   Add the following lines:
   ```
   listener 1883
   allow_anonymous true
   ```
3. Restart Mosquitto:
   ```bash
   sudo systemctl restart mosquitto
   ```

### **2. Install Python Dependencies on Raspberry Pi**
Install the required libraries:
```bash
pip3 install paho-mqtt opencv-python numpy
```

### **3. Set up MQTT Clients on Windows**
1. Download Mosquitto from [mosquitto.org](https://mosquitto.org/download/).
2. Install Mosquitto, ensuring to enable the command-line tools.
3. Add Mosquitto to the `PATH` to use `mosquitto_pub` and `mosquitto_sub` from any terminal.

---

## **Project Structure**

```
├── raspberry_handler.py   # Main script for the Raspberry Pi
├── README.md              # Documentation
└── photo/                 # Directory where captured photos are stored
```

---

## **How It Works**

1. **MQTT Commands:**
   - The Windows client publishes a command to the topic `camera/command` to trigger a photo.
   - Example:
     ```bash
     mosquitto_pub -h <IP_Raspberry> -t camera/command -m "take_photo"
     ```

2. **Capture and Processing:**
   - The Raspberry Pi:
     - Captures a photo.
     - Calculates the NDVI (Normalized Difference Vegetation Index) to evaluate plant health.

3. **MQTT Results:**
   - The result is published to the topic `raspberry/result`.
   - Example:
     ```bash
     NDVI Average: 0.68. The plant is healthy.
     ```

---

## **Testing the System**

### **1. On the Raspberry Pi**
1. Start the MQTT broker:
   ```bash
   sudo systemctl start mosquitto
   ```
2. Run the Python script:
   ```bash
   python3 raspberry_handler.py
   ```

### **2. From Windows**
1. Send a command to capture a photo:
   ```bash
   mosquitto_pub -h <IP_Raspberry> -t camera/command -m "take_photo"
   ```
2. Listen for results published by the Raspberry Pi:
   ```bash
   mosquitto_sub -h <IP_Raspberry> -t raspberry/result
