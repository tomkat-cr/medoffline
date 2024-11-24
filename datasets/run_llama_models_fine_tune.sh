#!/bin/bash
# run_llama_models_fine_tune.sh
# 2024-11-24 | CR
# Run the llama_models_fine_tune.py script
#
JSON_CONFIG_FILE="$1"
if [ "$JSON_CONFIG_FILE" = "" ]; then
    JSON_CONFIG_FILE="first_aid_instructions.json"
fi
if [ ! -f "$JSON_CONFIG_FILE" ]; then
    echo "Error: JSON config file not found."
    exit 1
fi
    
if [ ! -f "$JSON_CONFIG_FILE" ]; then
    echo "Error: JSON config file not found."
    exit 1
fi

if [ ! -d "venv" ]; then
    echo ""
    echo "Creating virtual environment..."
    python -m venv venv
fi

if ! source venv/bin/activate
then
    if ! . venv/bin/activate
    then
        echo "Error: Unable to activate virtual environment."
        exit 1
    fi
fi

if [ ! -f "llama_models_fine_tune_requirements.txt" ]; then
    pip install --upgrade pip
    pip install torch torchvision torchao
    pip install torchtune transformers pandas scikit-learn
    pip freeze > llama_models_fine_tune_requirements.txt
else
    pip install -r llama_models_fine_tune_requirements.txt
fi

echo ""
echo "Running llama_models_fine_tune..."
python llama_models_fine_tune.py $JSON_CONFIG_FILE $2 $3 $4

deactivate
rm -rf venv
# rm -f llama_models_fine_tune_requirements.txt
