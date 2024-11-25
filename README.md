# MedOffLine ✨

Asistente de IA Offline para Comunidades Remotas

![Hackathon Cover image](./assets/MedOffLine-cover-image.webp)

## Descripción

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/MedOffLine.circled.logo.500.png"
    title="MedOffLine logo by Carlos J. Ramirez"
/>

MedOffLine es un Asistente de IA (Inteligencia Artifical) enfocado a ofrecer orientación de primeros auxilios básicos en comunidades con acceso limitado o sin acceso a Internet, proporcionando:

* Guías de primeros auxilios.
* Diagnósticos preliminares basados en síntomas.
* Información sobre medicamentos básicos (dosis, efectos secundarios).
* Orientación sobre cuándo y dónde buscar ayuda médica.
* Material educativo sobre prevención de enfermedades comunes.

El sistema funciona en dispositivos móviles de mediano costo.

## El Problema

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/67481623-2eba-4512-b106-4d25ddc30a75.jpeg"
    title="Social relevance: regions across Latin America face significant healthcare access challenges, especially in rural areas"
/>

El acceso a la salud es un desafío en comunidades rurales debido a:

- Conectividad limitada.
- Barreras geográficas.
- Escasez de servicios médicos.

## Solución

MedOffLine ofrece una solución 100% offline, ligera y culturalmente inclusiva.

## Impacto directo y medible

<img 
    align="left"
    width="100"
    height="100"
    src="./assets/4d7f3d04-622f-4f0b-bee2-ab0749ab551c.jpeg"
    title="MedOffLine logo by Carlos J. Ramirez"
    style="padding-right: 5px"
/>

Se podría medir el impacto por el número de personas atendidas, consultas realizadas y casos críticos identificados.

Su implementación podría reducir las complicaciones de salud prevenibles.
<br/>
<br/>

## Por qué este proyecto tiene alta viabilidad

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/b9154955-edc2-4b3c-9124-d3c983b76d2c.jpeg"
    title="MedOffLine logo by Carlos J. Ramirez"
/>

Uso eficiente de tecnología:

Llama 3.2 es ideal porque incluye modelos ligeros que pueden funcionar en dispositivos móviles con recursos limitados, incluso sin conexión a internet.

Escalabilidad:

Puede ampliarse a otros países o regiones con desafíos similares.
Podría traducirse a lenguas indígenas o adaptarse a contextos específicos.

Viabilidad técnica y económica:

No requiere hardware sofisticado ni infraestructura costosa.
Puede financiarse mediante asociaciones con ONGs, gobiernos locales o iniciativas de salud pública.

## Technologia utilizada

* Modelos de Meta Llama: 3.2 1B/3B, Llama 3.1 70B
* Pytorch Executorch
* Java
* Python
* Streamlit
* Kaggle

<hr>

# MedOffLine ✨

Offline AI Assistant for Remote Communities

![Hackathon Cover image](./assets/1eb5de0b-c739-4547-920f-14d35f875db0.jpeg)

## Introduction

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/MedOffLine.circled.logo.500.png"
    title="MedOffLine logo by Carlos J. Ramirez"
/>

MedOffLine is an offline AI Assistant that provides basic first aid guidance in communities with limited (or no) Internet access. It offers:

* First aid guides.
* Preliminary diagnostics based on symptoms.
* Information on basic medications (dosages, side effects).
* Guidance on when and where to seek medical assistance.
* Educational materials on preventing common illnesses.

The system runs on mid-range mobile devices.

## The Problem

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/8bdffc8e-b8f0-4f9a-a883-dd7bcb70b3a6.jpeg"
    title="Social relevance: regions across Latin America face significant healthcare access challenges, especially in rural areas"
/>

Access to healthcare is a challenge in rural communities due to:

- Limited connectivity.
- Geographical barriers.
- Scarcity of medical services.

## Solution

MedOffLine offers a 100% offline, lightweight and culturally inclusive solution.

## Direct and Measurable Impact

<img 
    align="left"
    width="100"
    height="100"
    src="./assets/4d7f3d04-622f-4f0b-bee2-ab0749ab551c.jpeg"
    title="MedOffLine logo by Carlos J. Ramirez"
    style="padding-right: 5px"
/>

The impact could be measured by the number of people served, consultations performed, and critical cases identified.

Its implementation could help reduce preventable health complications.
<br/><br/>

## Why this project is highly viable

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/f8938201-f228-4a65-82da-d5bfd52b5b6d.jpeg"
    title="MedOffLine logo by Carlos J. Ramirez"
    style="padding-right: 5px"
/>

Efficient Use of Technology:

Llama 3.2 is an ideal choice as it includes lightweight models capable of running on resource-limited mobile devices, even without internet connectivity.

Scalability:

The solution can be expanded to other countries or regions facing similar challenges.
It could be translated into indigenous languages or adapted to specific cultural contexts.

Technical and Economic Feasibility:

The project does not require sophisticated hardware or costly infrastructure.
It can be funded through partnerships with NGOs, local governments, or public health initiatives.

## Technology Used

* Meta Llama models: 3.2 1B/3B, Llama 3.1 70B
* Pytorch Executorch
* Java
* Python
* Streamlit
* Kaggle

<hr>

## Getting Started

### Prerequisites

- [Python](https://www.python.org/downloads/) 3.10 or higher
- [Git](https://www.atlassian.com/git/tutorials/install-git)
- Make: [Mac](https://formulae.brew.sh/formula/make) | [Windows](https://stackoverflow.com/questions/32127524/how-to-install-and-use-make-in-windows)

### Installation

Clone the repository:
```bash
git clone https://github.com/tomkat-cr/medoffline.git
```

Navigate to the project directory:

```bash
cd medoffline
```

- The website sources can be found in the [public](./public/) directory.

- The Android applicacion can be opened in Android Studio from the directory [android/MedOffLine](./android/MedOffLine)

- The AI Model used in the Android app can be found in [Kaggle](https://www.kaggle.com/models/tomkatcr/llama3.2_3b_pte).

- The Model was generated following these instructions:

1. Follow the instructions to install Executorch on your computer:
    * [https://github.com/pytorch/executorch/tree/main/examples/models/llama#step-1-setup](https://github.com/pytorch/executorch/tree/main/examples/models/llama#step-1-setup)
    * [https://github.com/pytorch/executorch/tree/main/examples/models/llama#step-3-run-on-your-computer-to-validate](https://github.com/pytorch/executorch/tree/main/examples/models/llama#step-3-run-on-your-computer-to-validate)

2. Download the Llama 3.2 lightweight model and tokenizer from [the Meta Llama website](https://www.llama.com/llama-downloads) and copy it to a local directory, e.g. `/home/username/.llama/checkpoints`.

3. Create the output directory, e.g. `/home/username/llama_models`.

4. Convert the model to `.pte` format:

```bash
LLAMA_CHECKPOINT="/home/username/.llama/checkpoints/Llama3.2-3B-Instruct/consolidated.00.pth"
LLAMA_PARAMS="/home/username/.llama/checkpoints/Llama3.2-3B-Instruct/params.json"
OUTPUT_FILE="/home/username/llama_models/llama32_3b_4096_kv_sdpa_xnn_qe_4_32.pte"
MAX_SEQ_LENGTH="4096"

python -m examples.models.llama.export_llama \
    --checkpoint $LLAMA_CHECKPOINT \
	-p $LLAMA_PARAMS \
	-kv \
	--use_sdpa_with_kv_cache \
	-X \
	-qmode 8da4w \
	--group_size 128 \
	--max_seq_length $MAX_SEQ_LENGTH \
	-d fp32 \
	--metadata '{"get_bos_id":128000, "get_eos_ids":[128009, 128001]}' \
	--embedding-quantize 4,32 \
	--output_name $OUTPUT_FILE
```

5. Test the `.pte` model:

```bash
PTE_MODELS_PATH="/home/username/llama_models"
TOKENIZER_MODEL="$PTE_MODELS_PATH/tokenizer.model"
PTE_SOURCE_FILE="$PTE_MODELS_PATH/llama32_3b_4096_kv_sdpa_xnn_qe_4_32.pte"
PROMPT="What the the capital of France?"

cmake-out/examples/models/llama/llama_main --model_path="$PTE_SOURCE_FILE" --tokenizer_path="$TOKENIZER_MODEL" --prompt="$PROMPT"
```

<!--
### Create the .env file

Create a `.env` file in the root directory of the project:

```bash
# You can copy the .env.example file in the root directory of the project
cp .env.example .env
```

The `.env` file should have the following content:

```bash
PYTHON_VERSION=3.10
#
# Together AI
TOGETHER_AI_API_KEY=
# OpenAI
OPENAI_API_KEY=
#
# Database parameters
DB_TYPE=mongodb
# DB_TYPE=json
#
# MongoDB database parameters
MONGODB_URI=mongodb+srv://<user>:<password>@<cluster>.mongodb.net
MONGODB_DB_NAME=MedOffLine-dev
#
# JSON database parameters
# JSON_DB_PATH=./db/conversations.json
```

Replace `TOGETHER_AI_API_KEY` and other access tokens with your actual Together.ai API key, OpenAI, Huggingface, Groq, Nvidia, and Rhymes API keys, respectively.

To use a MongoDB database, comment out `DB_TYPE=json`, uncomment `# DB_TYPE=mongodb`, and replace `YOUR_MONGODB_URI`, `YOUR_MONGODB_DB_NAME`, and `YOUR_MONGODB_COLLECTION_NAME` with your actual MongoDB URI, database name, and collection name, respectively.
-->

### Run the Streamlit application

```bash
# With Make
make run
```

```bash
# Without Make
sh ./public/scripts/run_app.sh run
```

## Usage

Go to your favorite Browser and open the URL provided by the application.

* Locally:<BR/>
  [http://localhost:8503/](http://localhost:8503/)

* Official App:<BR/>
  [https://medoffline.streamlit.app/](https://MedOffLine.streamlit.app/)

## Screenshots

Main Page
![App Screenshot](./assets/screenshots/Screenshot%202024-11-25%20at%201.25.27 PM.png)

App UI
![App UI Screenshot](./assets/screenshots/IMG_0732.jpeg)

## Context

This project was developed as part of the [Llama Impact Pan-LATAM Hackathon](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es) organized by [Lablab.ai](https://lablab.ai).

![Hackathon banner image](./assets/hackathon-llama-impact-pan-latam-es-official-banner.webp)

- Project submission page: [MedOffLine](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es/the-fynbots/medoffline)

- Presentation video: [Llama Impact Pan-LATAM Hackathon - MedOffLine App Video](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es/the-fynbots/medoffline)

- Presentation document: [Llama Impact Pan-LATAM Hackathon - MedOffLine Presentation](https://storage.googleapis.com/lablab-static-eu/presentations/submissions/cm3xdpe8l00113b713e90gmb5/cm3xdpe8l00113b713e90gmb5-1732561102890_zq1alr0cwl.pdf)

- Team: [The FynBots](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es/the-fynbots)

## Contributors

[Carlos J. Ramirez](https://www.linkedin.com/in/carlosjramirez/) | [Omar Tobon](https://www.linkedin.com/in/omar-tobon/)

Please feel free to suggest improvements, report bugs, or make a contribution to the code.

## License

This project is licensed under the terms of the MIT license. See the [LICENSE](LICENSE) file for details.

## Acknowledgements

* [AI at Meta](https://ai.meta.com/) for developing the Llama powerful models and technology.
* [Lablab.ai](https://lablab.ai) for organizing the [Llama Impact Pan-LATAM Hackathon](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es).
* Open-source community for inspiring and supporting collaborative innovation.
* Users and contributors for their feedback and support.

<br/><br/>
<center>

![MedOffLine logo](./assets/MedOffLine.circled.logo.500.png)

</center>

