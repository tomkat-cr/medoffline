"""
dataset_translator.py
2024-11-23 | CR | Hackathon Llama Impact Pan-LATAM

Requirements:
pip install deep-translator
"""
from typing import Any
import os
import sys
import json

from deep_translator import GoogleTranslator
import pandas as pd


# Function to translate values
def translate_values(obj: Any, target_lang: str = None,
                     source_lang: str = None):
    if not target_lang:
        target_lang = 'es'
    if not source_lang:
        source_lang = 'en'

    # Initialize the translator
    translator = GoogleTranslator(source=source_lang, target=target_lang)

    # print(f"obj is an instance of {type(obj)}")

    if isinstance(obj, dict):
        return {key: translate_values(value) for key, value in obj.items()}
    elif isinstance(obj, list):
        return [translate_values(item) for item in obj]
    elif isinstance(obj, str):
        return translator.translate(obj)
    elif isinstance(obj, pd.core.frame.DataFrame):
        return obj.applymap(translate_values, target_lang=target_lang,
                            source_lang=source_lang)
    else:
        return obj


def main(file_path: str, translated_file_path: str = None,
         target_lang: str = None, source_lang: str = None):

    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        sys.exit(1)

    # Detect the file type by its extension
    file_ext = os.path.splitext(file_path)[1]
    if not file_ext or (file_ext.lower() != ".json" and 
                        file_ext.lower() != ".csv"):
        print(f"Unsupported file type: {file_ext}. "
              "Valid file types are: json, csv")
        sys.exit(1)
    # Load the JSON file
    # Remove the leading dot and convert to lowercase
    file_ext = file_ext[1:].lower()
    if file_ext == "json":
        with open(file_path, "r") as file:
            data = json.load(file)
    elif file_ext == "csv":
        data = pd.read_csv(file_path)
    else:
        print(f"Unsupported file type: {file_ext}")
        sys.exit(1)

    # Translate the JSON content
    translated_data = translate_values(data)

    if not translated_file_path:
        translated_file_path = file_path.replace(f".{file_ext}", "") + \
                               f"_translated_es.{file_ext}"
    # Save the translated JSON
    with open(translated_file_path, "w", encoding='utf-8') as file:
        if file_ext == "csv":
            translated_data.to_csv(file, index=False)
        else:
            json.dump(translated_data, file, ensure_ascii=False, indent=4)

    print(f"Translated {file_ext.capitalize()} saved to: "
          f" {translated_file_path}")


if __name__ == "__main__":
    # Get the filename from the user if the 1st command line parameter
    # is not provided
    if len(sys.argv) < 2:
        # file_path = "intents.json"
        file_path = input(
            "Enter the path to the JSON or CSV file"
            "\n(e.g. 'first_aid_instructions.csv', 'intents.json'): ")
    else:
        file_path = sys.argv[1]

    if len(sys.argv) > 2:
        target_lang = sys.argv[2]
    else:
        target_lang = "es"

    if len(sys.argv) > 3:
        translated_file_path = sys.argv[3]
    else:
        translated_file_path = None

    if len(sys.argv) > 4:
        source_lang = sys.argv[4]
    else:
        source_lang = "en"

    if not file_path:
        print("Usage: python dataset_translator.py <file_path>")
        sys.exit(1)

    main(file_path, translated_file_path, target_lang, source_lang)
