import os
from training_export import export_training_data

def auto_train():
    export_training_data(output_file="latest_training.jsonl")
    os.system("python train_gpt4all.py --data latest_training.jsonl")

if __name__ == "__main__":
    auto_train()
