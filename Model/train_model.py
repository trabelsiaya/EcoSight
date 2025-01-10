import os
import torch
import torch.nn as nn
import torch.optim as optim
from torchvision import models, transforms, datasets
from torch.utils.data import DataLoader
import matplotlib.pyplot as plt
from sklearn.metrics import classification_report, confusion_matrix
import seaborn as sns
from time import time
import numpy as np
from collections import Counter
import json

# Chemins des datasets
training_dir = "archive1/plantnet_300K/images_train"
validation_dir = "archive1/plantnet_300K/images_val"
testing_dir = "archive1/plantnet_300K/images_test"

# Préparation des transformations
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

# Charger les datasets
train_dataset = datasets.ImageFolder(root=training_dir, transform=transform)
val_dataset = datasets.ImageFolder(root=validation_dir, transform=transform)
test_dataset = datasets.ImageFolder(root=testing_dir, transform=transform)

train_loader = DataLoader(train_dataset, batch_size=32, shuffle=True)
val_loader = DataLoader(val_dataset, batch_size=32, shuffle=False)
test_loader = DataLoader(test_dataset, batch_size=32, shuffle=False)

# Calcul des poids alpha pour Focal Loss
class_image_counts = []
for idx, class_name in enumerate(train_dataset.classes):
    class_path = os.path.join(training_dir, class_name)
    if os.path.isdir(class_path):
        count = len(os.listdir(class_path))
        class_image_counts.append(count)
    else:
        class_image_counts.append(0)

alpha = []
for count in class_image_counts:
    if count < 150:
        alpha.append(4.0)  # Poids élevé pour les classes minoritaires
    elif 150 <= count <= 300:
        alpha.append(1.0)  # Poids neutre
    else:
        alpha.append(0.5)  # Poids réduit pour les classes majoritaires
alpha = torch.tensor(alpha)

# Définir la Focal Loss
class FocalLoss(nn.Module):
    def __init__(self, alpha, gamma=3):
        super(FocalLoss, self).__init__()
        self.alpha = alpha
        self.gamma = gamma

    def forward(self, inputs, targets):
        if self.alpha.device != targets.device:
            self.alpha = self.alpha.to(targets.device)
        ce_loss = nn.CrossEntropyLoss(reduction='none')(inputs, targets)
        pt = torch.exp(-ce_loss)
        focal_loss = self.alpha[targets] * (1 - pt) ** self.gamma * ce_loss
        return focal_loss.mean()

# Initialiser le modèle
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = models.efficientnet_b0(pretrained=True)
num_classes = len(train_dataset.classes)
model.classifier[1] = nn.Linear(model.classifier[1].in_features, num_classes)
model = model.to(device)

criterion = FocalLoss(alpha=alpha, gamma=3)
optimizer = optim.Adam(model.parameters(), lr=0.001)

# Entraînement du modèle
train_losses, val_losses = [], []
epochs = 12
for epoch in range(epochs):
    start_time = time()
    model.train()
    train_loss = 0

    for batch_idx, (images, labels) in enumerate(train_loader):
        images, labels = images.to(device), labels.to(device)

        optimizer.zero_grad()
        outputs = model(images)
        loss = criterion(outputs, labels)
        loss.backward()
        optimizer.step()
        train_loss += loss.item()

        # Estimation du temps restant
        elapsed_time = time() - start_time
        estimated_time = elapsed_time / (batch_idx + 1) * (len(train_loader) - batch_idx - 1)
        print(f"Epoch [{epoch + 1}/{epochs}], Batch [{batch_idx + 1}/{len(train_loader)}], Loss: {loss.item():.4f}, "
              f"Time Remaining: {estimated_time:.2f} seconds", end="\r")

    train_loss /= len(train_loader)
    train_losses.append(train_loss)

    # Validation
    model.eval()
    val_loss = 0
    with torch.no_grad():
        for images, labels in val_loader:
            images, labels = images.to(device), labels.to(device)
            outputs = model(images)
            loss = criterion(outputs, labels)
            val_loss += loss.item()
    val_loss /= len(val_loader)
    val_losses.append(val_loss)

    print(f"\nEpoch [{epoch + 1}/{epochs}], Train Loss: {train_loss:.4f}, Val Loss: {val_loss:.4f}")

# Évaluation sur les données de test
model.eval()
y_true, y_pred = [], []
with torch.no_grad():
    for images, labels in test_loader:
        images, labels = images.to(device), labels.to(device)
        outputs = model(images)
        _, preds = torch.max(outputs, 1)
        y_true.extend(labels.cpu().numpy())
        y_pred.extend(preds.cpu().numpy())

# Rapport de classification
print("\nRapport de classification :")
report = classification_report(y_true, y_pred, target_names=test_dataset.classes, output_dict=True)
print(classification_report(y_true, y_pred, target_names=test_dataset.classes))

# Nombre d'images d'entraînement par classe
train_class_counts = Counter(train_dataset.targets)

# Ajouter le nombre d'images d'entraînement au rapport
for idx, class_name in enumerate(test_dataset.classes):
    if class_name in report:
        report[class_name]['training_samples'] = train_class_counts.get(idx, 0)

# Enregistrer le rapport au format JSON
with open('classification_report.json', 'w') as json_file:
    json.dump(report, json_file, indent=4)

print("Le rapport de classification a été enregistré dans 'classification_report.json'.")

# Matrice de confusion
cm = confusion_matrix(y_true, y_pred)
plt.figure(figsize=(12, 8))
sns.heatmap(cm, annot=False, fmt="d", cmap="Blues", xticklabels=test_dataset.classes, yticklabels=test_dataset.classes)
plt.title("Matrice de confusion")
plt.xlabel("Prédictions")
plt.ylabel("Vérités")
plt.show()

# Courbes de perte
plt.figure(figsize=(10, 5))
plt.plot(train_losses, label="Train Loss")
plt.plot(val_losses, label="Validation Loss")
plt.xlabel("Époques")
plt.ylabel("Perte")
plt.title("Courbes de perte")
plt.legend()
plt.show()
