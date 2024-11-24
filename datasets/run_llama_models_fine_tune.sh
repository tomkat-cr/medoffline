#!/bin/bash
# run_llama_models_fine_tune.sh
# 2024-11-24 | CR
# Run the llama_models_fine_tune.py script
#
if [ ! -d "venv" ]; then
    echo ""
    echo "Creating virtual environment..."
    python -m venv venv
fi

source venv/bin/activate

if [ ! -f "llama_models_fine_tune_requirements.txt" ]; then
    pip install --upgrade pip
    pip install torch torchtune transformers pandas scikit-learn
    pip freeze > llama_models_fine_tune_requirements.txt
else
    pip install -r llama_models_fine_tune_requirements.txt
fi

echo ""
echo "Running llama_models_fine_tune..."
python llama_models_fine_tune.py $1 $2 $3 $4

deactivate
rm -rf venv
# rm -f llama_models_fine_tune_requirements.txt
