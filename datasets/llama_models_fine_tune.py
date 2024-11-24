"""
# llama_models_fine_tune.py
# 2024-11-24 | CR

Step 1: Requirements:

pip install --upgrade pip
pip install torch torchtune transformers pandas scikit-learn
"""
import sys
import os
import json

from transformers import LlamaForCausalLM, LlamaTokenizer
from sklearn.model_selection import train_test_split
import pandas as pd
import torch
from torch.utils.data import Dataset
from torchtune import Trainer


class QADataset(Dataset):
    def __init__(self, data, tokenizer, max_length=128):
        self.data = data
        self.tokenizer = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        entry = self.data[idx]
        inputs = self.tokenizer(
            entry["input_text"],
            max_length=self.max_length,
            truncation=True,
            padding="max_length",
            return_tensors="pt"
        )
        labels = self.tokenizer(
            entry["target_text"],
            max_length=self.max_length,
            truncation=True,
            padding="max_length",
            return_tensors="pt"
        ).input_ids
        return {
            "input_ids": inputs.input_ids.flatten(),
            "attention_mask": inputs.attention_mask.flatten(),
            "labels": labels.flatten()
        }


class LlamaModelsFineTune:
    def __init__(self, params: dict):
        self.params = params
        self.input_file_path = self.replace_envvars(
            self.params.get("input_file_path"))
        self.model_name = self.params.get("model_name")
        self.model_path = self.params.get("model_path")
        self.output_dir = self.replace_envvars(self.params.get("output_dir"))

        if self.input_file_path is None:
            raise ValueError("Input file path is required.")
        if self.output_dir is None:
            raise ValueError("Output directory is required.")
        if self.model_name is None:
            raise ValueError("Model name is required.")

        self.train_data = None
        self.val_data = None
        self.tokenizer = None
        self.model = None
        self.train_dataset = None
        self.val_dataset = None

    def replace_envvars(self, path):
        if path is None:
            return None
        replacements = [
            ("$HOME", os.path.expanduser("~")),
            ("${HOME}", os.path.expanduser("~")),
            ("$PWD", os.getcwd()),
            ("${PWD}", os.getcwd()),
        ]
        for envvar in replacements:
            path = path.replace(f"${envvar[0]}", os.environ[envvar[1]])
        return path

    def prepare_dataset(self):
        """
        Step 2: Prepare Dataset
        Assume your CSV file has two columns: "question" and "answer".
        """
        # Load the dataset
        df = pd.read_csv(self.input_file_path)

        # Split into training and validation sets
        # (e.g., 80% training, 20% validation)
        train_df, val_df = train_test_split(
            df,
            test_size=0.2,
            random_state=42
        )

        # Convert to lists
        self.train_data = [
            {
                "input_text": row["question"],
                "target_text": row["answer"]
            }
            for _, row in train_df.iterrows()]
        self.val_data = [
            {
                "input_text": row["question"],
                "target_text": row["answer"]
            }
            for _, row in val_df.iterrows()]

        # Save the training and validation data
        # (optional, but useful for debugging)
        output_file_path = os.path.join(self.output_dir, "train_data.json")
        with open(output_file_path, "w") as f:
            json.dump(self.train_data, f, indent=4)
        output_file_path = os.path.join(self.output_dir, "val_data.json")
        with open(output_file_path, "w") as f:
            json.dump(self.val_data, f, indent=4)

    def load_llama_model_and_tokenizer(self):
        """
        Step 3: Load LLaMA Model and Tokenizer
        You'll use the transformers library to load Meta's LLaMA model and
        tokenizer.
        """

        # Load the tokenizer and model (assuming they are available locally or
        # can be downloaded)
        # e.g. model_name = "meta-llama-3B"
        self.tokenizer = LlamaTokenizer.from_pretrained(self.model_name)
        self.model = LlamaForCausalLM.from_pretrained(self.model_name)

    def create_custom_dataset_for_torchtune(self):
        """
        Step 4: Create Custom Dataset for Torchtune
        Now, you need to create a custom dataset for Torchtune.
        """
        self.train_dataset = QADataset(self.train_data, self.tokenizer)
        self.val_dataset = QADataset(self.val_data, self.tokenizer)

    def fine_tune_llama_model(self):
        """
        Step 5: Set Up the Training Loop with Torchtune
        Now, you can set up and initiate the fine-tuning process with
        Torchtune.
        """
        # Define the training arguments
        train_args = {
            "epochs": 3,  # Number of epochs
            "batch_size": 8,  # Batch size
            "learning_rate": 5e-5,  # Learning rate
            "logging_steps": 10,  # Log after every 10 steps
            # Use GPU if available
            "device": "cuda" if torch.cuda.is_available() else "cpu"
        }

        # Create the trainer
        trainer = Trainer(
            model=self.model,
            train_dataset=self.train_dataset,
            val_dataset=self.val_dataset,
            train_args=train_args
        )

        # Start training
        trainer.train()

    def save_fine_tuned_model(self):
        """
        Step 6: Save the Fine-Tuned Model
        After training, save your fine-tuned model for later use.
        """
        self.model.save_pretrained(self.output_dir)
        self.tokenizer.save_pretrained(self.output_dir)

    def prepare_output_dir(self):
        if not os.path.exists(self.output_dir):
            os.makedirs(self.output_dir)

    def run(self):
        print("")
        print(f"Fine-tuning the LLaMA model: {self.model_name}")
        print("")
        print(f"Checking the output directory: {self.output_dir}")
        print("")
        self.prepare_output_dir()
        print(f"Preparing the dataset: {self.input_file_path}")
        self.prepare_dataset()
        print("")
        print("Loading the LLaMA model and tokenizer...")
        self.load_llama_model_and_tokenizer()
        print("")
        print("Creating a custom dataset for Torchtune...")
        self.create_custom_dataset_for_torchtune()
        print("")
        print(f"Fine-tuning the LLaMA model: {self.model_name}")
        self.fine_tune_llama_model()
        print("")
        print(f"Saving the fine-tuned model in: {self.output_dir}")
        self.save_fine_tuned_model()
        print("")
        print(f"Fine-tuning complete!. Results saved in: {self.output_dir}")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("")
        print("The JSON config file is required and must have the following"
              " structure: for example:")
        print("""
{
    "input_file_path": "first_aid_instructions_translated_es.csv",
    "model_name": "meta-llama/Llama-3.2-3B-Instruct",
    "model_path": "$HOME/.llama/Llama-3.2-3B-Instruct",
    "output_dir": "$HOME/.llama/Llama-3.2-3B-Instruct/trained"
}
        """)
        print("")
        input_config_file_path = input(
            "Enter the path to the JSON config file"
            "\n(e.g. 'first_aid_instructions.json'): ")
        # input_csv_file_path = input(
        #     "Enter the path to the CSV input file"
        #     "\n(e.g. 'first_aid_instructions_translated_es.csv'): ")
        # model_name = input(
        #     "Enter the model ID in Hugging Face's model hub"
        #     "\n(e.g. 'meta-llama/Llama-3.2-3B-Instruct'): ")
        # output_dir = input(
        #     "Enter the output directory to save the fine-tuned model"
        #     "\n(e.g. '/home/{user_name}/.llama/Llama-3.2-3B-Instruct'): ")
        # if not input_csv_file_path or not model_name or not output_dir:
        #     print("Usage: python llama_models_fine_tune.py"
        #           "<input_csv_file_path> <model_name> <output_dir>")
        #     sys.exit(1)
        # params = {
        #     "input_file_path": input_csv_file_path,
        #     "model_name": model_name,
        #     "output_dir": output_dir,
        # }

    input_config_file_path = sys.argv[1]
    if not os.path.exists(input_config_file_path):
        print(f"File not found: {input_config_file_path}")
        sys.exit(1)
    with open(input_config_file_path, "r") as f:
        params = json.load(f)

    llama_fine_tune = LlamaModelsFineTune(params)
    llama_fine_tune.run()
