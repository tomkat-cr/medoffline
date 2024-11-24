"""
dataset_conveter.py
2024-11-24 | CR | Hackathon Llama Impact Pan-LATAM

Requirements:
pip install datasets
"""
import sys
from datasets import load_dataset


def main(dataset_id: str, output_file: str, output_format: str):
    # ds = load_dataset("lextale/FirstAidInstructionsDataset")
    # ds.to_csv("first_aid_instructions.csv")
    ds = load_dataset(dataset_id)
    if output_format == "csv":
        print(f"Converting '{dataset_id}' to CSV...")
        ds['train'].to_csv(output_file, index=False)
        print(f"Dataset converted to CSV. Output saved to {output_file}")
    else:
        print(f"Output format not supported: {output_format}")


if __name__ == "__main__":

    # Parameters:
    # <dataset_id> <output_file> <output_format>

    # Get the filename from the user if the 1st command line parameter
    # is not provided
    if len(sys.argv) < 2:
        dataset_id = input(
            "Enter the Hugging Face Dataset ID (e.g. "
            "'lextale/FirstAidInstructionsDataset'): ")
    else:
        dataset_id = sys.argv[1]

    if len(sys.argv) < 3:
        output_file = input("Enter the path to the output file (g.e. "
                            "'first_aid_instructions.csv'): ")
    else:
        output_file = sys.argv[2]

    if len(sys.argv) > 3:
        output_format = sys.argv[3]
    else:
        output_format = "csv"

    if not dataset_id or not output_file or not output_format:
        print("Usage: python dataset_conveter.py <dataset_id> <output_file>"
              " <output_format>")
        sys.exit(1)

    main(dataset_id, output_file, output_format)
