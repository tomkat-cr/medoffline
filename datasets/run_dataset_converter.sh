#!/bin/bash
# run_dataset_converter.sh
# Script to run the dataset converter
# 2024-11-24 | CR
# Usage: ./run_dataset_converter.sh <dataset_id> <output_file> <output_format>
#
if [ ! -d "venv" ]; then
    echo ""
    echo "Creating virtual environment..."
    python -m venv venv
fi

source venv/bin/activate

if [ ! -f "dataset_converter_requirements.txt" ]; then
    pip install --upgrade pip
    pip install datasets
    pip freeze > dataset_converter_requirements.txt
else
    pip install -r dataset_converter_requirements.txt
fi

echo ""
echo "Running dataset translator..."
python dataset_converter.py $1 $2 $3 $4

deactivate
rm -rf venv
# rm -f dataset_converter_requirements.txt
