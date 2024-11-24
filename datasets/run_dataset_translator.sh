#!/bin/bash
# run_dataset_translator.sh
# Script to run the dataset translator
# 2024-11-23 | CR
# Usage: ./run_dataset_translator.sh <input_file> <output_file> <language>
#
if [ ! -d "venv" ]; then
    echo ""
    echo "Creating virtual environment..."
    python -m venv venv
fi

source venv/bin/activate

if [ ! -f "dataset_translator_requirements.txt" ]; then
    pip install --upgrade pip
    pip install deep-translator pandas
    pip freeze > dataset_translator_requirements.txt
else
    pip install -r dataset_translator_requirements.txt
fi

echo ""
echo "Running dataset translator..."
python dataset_translator.py $1 $2 $3 $4

deactivate
rm -rf venv
# rm -f dataset_translator_requirements.txt
