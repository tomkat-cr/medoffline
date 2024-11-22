# MedOffLine ✨

Asistente Médico Offline para Comunidades Remotas

![Hackathon Cover image](./assets/DALL·E%202024-11-22%2009.13.31%20-%20A%20vibrant%20and%20colorful%20illustration%20of%20a%20mobile%20app%20concept%20named%20MedOffLine.%20The%20image%20showcases%20an%20offline%20medical%20assistant%20designed%20for%20remote%20com.webp)

## Introduccion

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/MedOffLine.circled.logo.500.png"
    title="MedOffLine logo by Carlos J. Ramirez"
/>

MedOffLine es un asistente de inteligencia artificial ligero, basado en los modelos Llama, que brinde orientación médica básica en comunidades sin acceso a internet. El sistema podría funcionar en dispositivos móviles de bajo costo y ofrecer:

## Funcionalidades clave

* Diagnósticos preliminares basados en síntomas.
* Guías de primeros auxilios.
* Información sobre medicamentos básicos (dosis, efectos secundarios).
* Orientación sobre cuándo y dónde buscar ayuda médica.
* Material educativo sobre prevención de enfermedades comunes.

## El Problema

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/67481623-2eba-4512-b106-4d25ddc30a75.jpeg"
    title="Social relevance: regions across Latin America face significant healthcare access challenges, especially in rural areas"
/>

Relevancia social:

Venezuela y muchas regiones de América Latina enfrentan graves problemas de acceso a la salud, especialmente en zonas rurales. Este proyecto resuelve una necesidad básica y urgente.

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

* Uso eficiente de tecnología:

Llama 3.2 es ideal porque incluye modelos ligeros que pueden funcionar en dispositivos móviles con recursos limitados, incluso sin conexión a internet.

* Escalabilidad:

Puede ampliarse a otros países o regiones con desafíos similares.
Podría traducirse a lenguas indígenas o adaptarse a contextos específicos.

* Viabilidad técnica y económica:

No requiere hardware sofisticado ni infraestructura costosa.
Puede financiarse mediante asociaciones con ONGs, gobiernos locales o iniciativas de salud pública.

<hr>

# MedOffLine ✨

Offline Medical Assistant for Remote Communities

![Hackathon Cover image](./assets/1eb5de0b-c739-4547-920f-14d35f875db0.jpeg)

## Introduction

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/MedOffLine.circled.logo.500.png"
    title="MedOffLine logo by Carlos J. Ramirez"
/>

MedOffLine is a cutting-edge application designed to revolutionize the real estate market by simplifying the process for both buyers and sellers. Utilizing an advanced AI  Assistant technology, the app offers a conversational interface where users can enter their real estate preferences and requirements.

## Key Features

* Preliminary diagnostics based on symptoms.
* First aid guides.
* Information on basic medications (dosages, side effects).
* Guidance on when and where to seek medical assistance.
* Educational materials on preventing common illnesses.

## The Problem

<img 
    align="right"
    width="100"
    height="100"
    src="./assets/8bdffc8e-b8f0-4f9a-a883-dd7bcb70b3a6.jpeg"
    title="Social relevance: regions across Latin America face significant healthcare access challenges, especially in rural areas"
/>

Social Relevance:

Colombia, Venezuela and many regions across Latin America face significant healthcare access challenges, especially in rural areas. This project addresses a critical and urgent need.

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
* MongoDB Atlas
* Executorch
* Java
* Python
* Streamlit
* GenericSuite
* AI/ML API or Sambanova

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

### Run the Application

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
![App Screenshot](./assets/screenshots/Screenshot%202024-11-17%20at%2010.11.29 AM.png)

Main Page with IBM watsonx assistan opened
![App Screenshot](./assets/screenshots/Screenshot%202024-11-17%20at%2010.13.26 AM.png)

## Context

This project was developed as part of the [Llama Impact Pan-LATAM Hackathon](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es) organized by [Lablab.ai](https://lablab.ai).

![Hackathon banner image](./assets/hackathon-llama-impact-pan-latam-es-official-banner.webp)

- Project submission page: [MedOffLine](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es/the-fynbots/medoffline)

- Presentation video: [Llama Impact Pan-LATAM Hackathon - MedOffLine App Video Presentation](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es/the-fynbots/medoffline)

<!--
- Presentation document: [Llama Impact Pan-LATAM Hackathon - MedOffLine App Maker Presentation](https://storage.googleapis.com/lablab-static-eu/presentations/submissions/xxx.pdf)
-->

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

