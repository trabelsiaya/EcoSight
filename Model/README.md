# Plant Classification Project

## Overview

This project focuses on building a comprehensive pipeline for the classification of plant species. Starting with raw data, the project implements preprocessing steps, including cleaning, augmentation, and reduction, followed by model training and evaluation. It also includes clustering for better analysis and understanding of the dataset. 

The deliverables include:
- A trained model (`model.pth`).
- JSON files for species names and classification results.
- Visualizations, including loss curves and confusion matrices.

---

## Table of Contents

1. [Dataset Information](#dataset-information)
2. [Data Preprocessing](#data-preprocessing)
   - [Data Cleaning](#data-cleaning)
   - [Data Augmentation](#data-augmentation)
   - [Data Reduction & Redistribution](#data-reduction--redistribution)
3. [Data Statistics](#data-statistics)
4. [Model Training](#model-training)
   - [Architecture](#architecture)
   - [Loss Function and Optimizer](#loss-function-and-optimizer)
   - [Training Pipeline](#training-pipeline)
5. [Evaluation and Results](#evaluation-and-results)
   - [Confusion Matrix](#confusion-matrix)
   - [Loss Curves](#loss-curves)
   - [Classification Report](#classification-report)
6. [Clustering Analysis](#clustering-analysis)
7. [Files in the Repository](#files-in-the-repository)
8. [Conclusion](#conclusion)

---

## Dataset Information

The dataset consists of plant images distributed across three directories:

1. **Training**: Images used to train the model.
2. **Testing**: Images used to evaluate model performance.
3. **Validation**: Images used for tuning hyperparameters during training.

### Initial Dataset Characteristics

- **Total Images**: 300,000.
- **Classes**: 1,081 unique plant species.
- **Structure**: Each directory contains 1,081 subdirectories, each corresponding to a specific class.

A JSON file (`plantnet300K_species_names.json`) maps class identifiers to their respective plant species names.

---

## Data Preprocessing

### Data Cleaning

- **Objective**: Ensure all images are valid and usable.
- **Process**:
  1. Identify invalid images (e.g., corrupt files, non-image formats).
  2. Log invalid image paths for review.
  3. Remove invalid images from the dataset.

### Data Augmentation

- **Objective**: Balance the dataset by increasing the number of images in underrepresented classes.
- **Methods**:
  - Classes with fewer than **80 images** in training were augmented to reach at least 80 images.
  - Classes with fewer than **10 images** in testing or validation were augmented to reach 10 images.

- **Tools**: Google Custom Search API was used to download additional images when necessary.

### Data Reduction & Redistribution

- **Objective**: Manage classes with an excess of images to maintain dataset balance.
- **Process**:
  1. Use **ResNet-50** to extract feature embeddings for all images.
  2. Calculate distances between embeddings to determine image diversity.
  3. Redistribute excess images:
     - Move excess images from training to testing (up to 110 images per class).
     - If testing is saturated, move excess to validation (up to 110 images per class).
     - If excess remains, the least diverse images are removed.

---

## Data Statistics

### Final Dataset Characteristics

- **Classes Remaining**: 379 classes after removing underrepresented classes.
- **Image Distribution**:
  - **Training**: Max 500 images/class, Min 80 images/class.
  - **Testing**: Max 110 images/class, Min 10 images/class.
  - **Validation**: Max 110 images/class, Min 10 images/class.

---

## Model Training

### Architecture

- **Base Model**: EfficientNet-B0 pretrained on ImageNet.
- **Final Layer**:
  - Original output layer replaced with a linear layer of size 379 to match the number of classes.

### Loss Function and Optimizer

- **Loss Function**: Focal Loss with class-based weights to handle class imbalance.
- **Optimizer**: Adam with a learning rate of 0.001.

### Training Pipeline

- **Epochs**: 12
- **Batch Size**: 32
- **Validation**: Conducted at the end of each epoch to monitor performance.
- **Hardware**: CUDA-enabled GPU for faster training.

---

## Evaluation and Results

### Confusion Matrix

- A confusion matrix was generated to analyze model predictions.
- Due to the large number of classes, the matrix was divided into smaller blocks for better visualization.

### Loss Curves

- Training and validation loss curves were plotted to observe the model's learning behavior.

### Classification Report

- A detailed report was generated, including:
  - Precision, Recall, F1-Score for each class.
  - Support (number of test samples per class).
  - Training, Testing, and Validation image counts.

---

## Clustering Analysis

- **Objective**: Understand class similarities and identify misclassified instances.
- **Method**:
  - Use feature embeddings from ResNet-50 for clustering.
  - Apply clustering algorithms (e.g., K-Means) to group similar classes.
- **Output**:
  - Visualizations of clusters.
  - Identification of classes that are visually or semantically similar.

---

## Files in the Repository

1. **`data/`**: Contains preprocessed datasets (train, test, validation).
2. **`models/`**: Includes the final trained model (`model.pth`).
3. **`results/`**:
   - Confusion matrices.
   - Loss curves.
4. **`scripts/`**: All scripts used in the pipeline, including:
   - Data cleaning.
   - Data augmentation.
   - Model training.
   - Evaluation and clustering.
5. **`plantnet300K_species_names.json`**: Maps class identifiers to plant names.

---

## Conclusion

This project demonstrates a complete end-to-end pipeline for plant species classification. The results showcase a balanced dataset, a robust model, and meaningful visualizations for further analysis.

The repository serves as a comprehensive guide for similar classification tasks, offering reusable scripts, insights, and techniques.

---

## How to Use

1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/plant-classification.git
   cd plant-classification
   ```
   
 2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

3. Train the model:
   ```bash
   python scripts/train_model.py
   ```



4. Visualize results:
   - View confusion matrices in the `results/` directory.
   - Analyze loss curves using the saved plots.

