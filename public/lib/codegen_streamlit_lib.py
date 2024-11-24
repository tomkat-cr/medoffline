"""
Streamlit UI library
"""
from typing import Any, Callable
import os
import time
import uuid
import html
import json

import streamlit as st

from lib.codegen_utilities import (
    log_debug,
    get_date_time,
    get_new_item_id,
    read_file,
    is_an_url,
    path_exists,
)
from lib.codegen_db import CodegenDatabase


DEBUG = True


@st.dialog("Form validation")
def show_popup(title: str, message: str, msg_type: str = "success"):
    """
    Show a streamlit popup with a message
    """
    message = message.replace("\n", "<br>")
    message = message.replace("\r", "<br>")
    st.header(f"{title}")
    if msg_type == "success":
        st.success(message)
    elif msg_type == "error":
        st.error(message)
    elif msg_type == "info":
        st.info(message)
    elif msg_type == "warning":
        st.warning(message)


class StreamlitLib:
    """
    Streamlit UI library
    """
    def __init__(self, params: dict):
        self.params = params

    # General utilities and functions

    def set_new_id(self, id: str = None):
        """
        Set the new id global variable
        """
        st.session_state.new_id = id

    def get_new_id(self):
        """
        Get the new id global variable
        """
        if "new_id" in st.session_state:
            return st.session_state.new_id
        else:
            return "No new_id"

    def set_query_param(self, name, value):
        """
        Set a URL query parameter
        """
        st.query_params[name] = value

    def timer_message(
        self, message: str, type: str,
        container: st.container = None,
        seconds: int = 10
    ):
        """
        Start a timer
        """
        if not container:
            container = st.empty()
        if type == "info":
            alert = container.info(message)
        elif type == "warning":
            alert = container.warning(message)
        elif type == "success":
            alert = container.success(message)
        elif type == "error":
            alert = container.error(message)
        else:
            raise ValueError(f"Invalid type: {type}")
        time.sleep(seconds)
        # Clear the alert
        alert.empty()

    def success_message(self, message: str, container: st.container = None):
        """
        Display a success message
        """
        self.timer_message(message, "success", container)

    def error_message(self, message: str, container: st.container = None):
        """
        Display an error message
        """
        self.timer_message(message, "error", container)

    def info_message(self, message: str, container: st.container = None):
        """
        Display an info message
        """
        self.timer_message(message, "info", container)

    def warning_message(self, message: str, container: st.container = None):
        """
        Display a warning message
        """
        self.timer_message(message, "warning", container)

    # Conversations database

    def init_db(self):
        """
        Initialize the JSON file database
        """
        db_type = os.getenv('DB_TYPE')
        db = None
        if db_type == 'json':
            db = CodegenDatabase("json", {
                "JSON_DB_PATH": os.getenv(
                    'JSON_DB_PATH',
                    self.get_par_value("CONVERSATION_DB_PATH")
                ),
            })
        if db_type == 'mongodb':
            db = CodegenDatabase("mongodb", {
                "MONGODB_URI": os.getenv('MONGODB_URI'),
                "MONGODB_DB_NAME": os.getenv('MONGODB_DB_NAME'),
                "MONGODB_COLLECTION_NAME": os.getenv('MONGODB_COLLECTION_NAME')
            })
        if not db:
            raise ValueError(f"Invalid DB_TYPE: {db_type}")
        return db

    def update_conversations(self):
        """
        Update the side bar conversations from the database
        """
        st.session_state.conversations = self.get_conversations()

    def update_conversation(
        self,
        item: dict = None,
        id: str = None
    ):
        db = self.init_db()
        log_debug(f"UPDATE_CONVERSATION | id: {id} | item: {item}",
                  debug=DEBUG)
        db.save_item(item, id)
        self.set_new_id(id)

    def save_conversation(
        self, type: str,
        question: str,
        answer: str,
        refined_prompt: str = None,
        other_data: dict = None,
        id: str = None
    ):
        """
        Save the conversation in the database
        """
        if not id:
            id = get_new_item_id()
        db = self.init_db()
        item = {
            "type": type,
            "question": question,
            "answer": answer,
            "refined_prompt": refined_prompt,
            "timestamp": time.time(),
        }
        if not other_data:
            other_data = {}
        item.update(other_data)
        db.save_item(item, id)
        self.update_conversations()
        self.recycle_suggestions()
        self.set_new_id(id)
        return id

    def get_conversations(self):
        """
        Returns the conversations in the database
        """
        db = self.init_db()
        conversations = db.get_list("timestamp", "desc")
        # Add the date_time field to each conversation
        for conversation in conversations:
            conversation['date_time'] = get_date_time(
                conversation['timestamp'])
        return conversations

    def get_conversation(self, id: str):
        """
        Returns the conversation in the database
        """
        db = self.init_db()
        conversation = db.get_item(id)
        if conversation:
            # Add the date_time field to the conversation
            conversation['date_time'] = get_date_time(
                conversation['timestamp'])
            return conversation
        return None

    def delete_conversation(self, id: str):
        """
        Delete a conversation from the database
        """
        db = self.init_db()
        db.delete_item(id)
        self.update_conversations()

    def show_conversations(self):
        """
        Show the conversations in the side bar
        """
        title_length = self.get_par_value("CONVERSATION_TITLE_LENGTH")
        st.header("Previous answers")
        for conversation in st.session_state.conversations:
            col1, col2 = st.columns(2, gap="small")
            with col1:
                title = conversation['question']
                title = title.replace("```json", "")
                title = title.replace("```", "")
                title = title.replace("\t", " ")
                title = title.replace("\n", " ")
                title = title.replace("\r", " ")
                title = title.strip()
                st.button(
                    title[:title_length],
                    key=f"{conversation['id']}",
                    help=f"{conversation['type'].capitalize()} generated on " +
                        f"{conversation['date_time']}")
            with col2:
                st.button(
                    "x",
                    key=f"del_{conversation['id']}",
                    on_click=self.delete_conversation,
                    args=(conversation['id'],))

    def set_last_retrieved_conversation(self, id: str, conversation: dict):
        """
        Set the last retrieved conversation
        """
        st.session_state.last_retrieved_conversation = dict(conversation)
        if "id" not in st.session_state.last_retrieved_conversation:
            st.session_state.last_retrieved_conversation["id"] = id

    def get_last_retrieved_conversation(self, id: str):
        """
        Get the last retrieved conversation. If "last_retrieved_conversation"
        entry is found and the id matches, return the buffered conversation.
        Otherwise, retrieve the conversation from the database.

        Args:
            id (str): The conversation ID.

        Returns:
            dict: The conversation dictionary, or None if not found.
        """
        if "last_retrieved_conversation" in st.session_state and \
           id == st.session_state.last_retrieved_conversation["id"]:
            conversation = dict(st.session_state.last_retrieved_conversation)
        else:
            conversation = self.get_conversation(id)
        if conversation:
            self.set_last_retrieved_conversation(id, conversation)
        return conversation

    def show_conversation_debug(self, conversation: dict):
        with st.expander("Detailed Response"):
            st.write(conversation)

    def show_cloud_resource(self, url: str, resource_type: str):
        if resource_type == "image":
            st.image(url)
        elif resource_type == "video":
            st.video(url)
        else:
            st.write(f"Not a video or image: {url}")

    def show_local_resource(self, url: str, resource_type: str):
        if resource_type in ["image", "video"]:
            return self.show_cloud_resource(url, resource_type)
        with open(url, "rb") as url:
            st.download_button(
                label="Download File",
                data=url,
                file_name=os.path.basename(url)
            )

    def verify_and_show_resource(self, url: str, resource_type: str):
        if is_an_url(url):
            self.show_cloud_resource(url, resource_type)
            return
        if not path_exists(url):
            st.write(f"ERROR E-IG-101: file not found: {url}")
        else:
            self.show_local_resource(url, resource_type)

    def show_conversation_content(
        self,
        id: str, container: st.container,
        additional_container: st.container
    ):
        """
        Show the conversation content
        """
        if not id:
            return
        conversation = self.get_last_retrieved_conversation(id)
        if not conversation:
            container.write("ERROR E-600: Conversation not found")
            return
        if conversation.get('refined_prompt'):
            with additional_container.expander(
                 f"Enhanced Prompt for {conversation['type'].capitalize()}"):
                st.write(conversation['refined_prompt'])

        if conversation['type'] == "video":
            if conversation.get('answer'):
                # Check for list type entries, and show them individually
                if isinstance(conversation['answer'], list):
                    with container.container():
                        self.show_conversation_debug(conversation)
                        for url in conversation['answer']:
                            st.write(f"Video URL: {url}")
                            self.verify_and_show_resource(url, "video")
                else:
                    with container.container():
                        self.show_conversation_debug(conversation)
                        st.write(f"Video URL: {conversation['answer']}")
                        self.verify_and_show_resource(
                            conversation['answer'], "video")
            else:
                self.video_generation(
                    result_container=container,
                    question=conversation['question'],
                    previous_response=conversation['ttv_response'])

        elif conversation['type'] == "image":
            if conversation.get('answer'):
                # Check for list type entries, and show them individually
                if isinstance(conversation['answer'], list):
                    with container.container():
                        self.show_conversation_debug(conversation)
                        for url in conversation['answer']:
                            self.verify_and_show_resource(url, "image")
                else:
                    with container.container():
                        self.show_conversation_debug(conversation)
                        self.verify_and_show_resource(
                            conversation['answer'], "image")
            else:
                with container.container():
                    self.show_conversation_debug(conversation)
                    st.write("ERROR: No image found as answer")

        else:
            with container.container():
                self.show_conversation_debug(conversation)
                st.write(conversation['answer'])
                if conversation.get("subtype"):
                    if conversation["subtype"] in [
                        "generate_presentation",
                        "generate_app_presentation"
                    ]:
                        extra_button_text = ""
                        if conversation.get("presentation_file_path"):
                            extra_button_text = " again"
                        st.button(
                            f"Generate Presentation{extra_button_text}",
                            on_click=self.create_pptx,
                            args=(conversation,))
                        if conversation.get("presentation_file_path"):
                            self.verify_and_show_resource(
                                conversation["presentation_file_path"],
                                "other")

    def show_conversation_question(self, id: str):
        if not id:
            return
        conversation = self.get_last_retrieved_conversation(id)
        if not conversation:
            st.session_state.question = "ERROR E-700: Conversation not found"
        else:
            st.session_state.question = conversation['question']
            if conversation.get("form_data"):
                form_session_state_key = \
                    self.get_form_session_state_key(conversation)
                st.session_state[form_session_state_key] = \
                    conversation["form_data"]

    def validate_question(self, question: str, assign_global: bool = True):
        """
        Validate the question
        """
        if not question:
            st.write("Please enter a question / prompt")
            return False
        # Update the user input in the conversation
        if assign_global:
            st.session_state.question = question
        return True

    # Data management

    def format_results(self, results: list):
        return "\n*".join(results)

    def attach_files(self, files):
        """
        Save the files to be attached to the LLM/model call
        """
        if "files_attached" not in st.session_state:
            st.session_state.files_to_attach = []
        if not files:
            return
        for file in files:
            if file:
                st.session_state.files_to_attach.append(file)

    def import_data(self, container: st.container):
        """
        Umport data from a uploaded JSON file into the database
        """

        def process_uploaded_file():
            """
            Process the uploaded file
            """
            uploaded_files = st.session_state.import_data_file
            st.session_state.dm_results = []
            with st.spinner(f"Processing {len(uploaded_files)} files..."):
                for uploaded_file in uploaded_files:
                    uploaded_file_path = uploaded_file.name
                    json_dict = json.loads(uploaded_file.getvalue())
                    db = self.init_db()
                    response = db.import_data(json_dict)
                    if response['error']:
                        item_result = f"File: {uploaded_file_path}" \
                                    f" | ERROR: {response['error_message']}"
                        log_debug(f"IMPORT_DATA | {item_result}", debug=DEBUG)
                        st.session_state.dm_results.append(item_result)
                        continue
                    item_result = f"File: {uploaded_file_path}" \
                                  f" | {response['result']}"
                    st.session_state.dm_results.append(item_result)

        container.file_uploader(
            "Choose a JSON file to perform the import",
            accept_multiple_files=True,
            type="json",
            on_change=process_uploaded_file,
            key="import_data_file",
        )

    def export_data(self, container: st.container):
        """
        Export data from the database and send it to the user as a JSON file
        """
        with st.spinner("Exporting data..."):
            db = self.init_db()
            response = db.export_data()
            if response['error']:
                container.write(f"ERROR {response['error_message']}")
                return
            container.download_button(
                label=f"{response['result']}. Click to download.",
                data=response['json'],
                file_name="data.json",
                mime="application/json",
            )

    def data_management_components(self):
        """
        Show data management components in the side bar
        """
        with st.expander("Data Management"):
            st.write("Import/export data with JSON files")
            sb_col1, sb_col2 = st.columns(2)
            with sb_col1:
                sb_col1.button(
                    "Import Data",
                    key="import_data")
            with sb_col2:
                sb_col2.button(
                    "Export Data",
                    key="export_data")

    # UI

    def get_title(self):
        """
        Returns the title of the app
        """
        return (f"{st.session_state.app_name_version}"
                f" {st.session_state.app_icon}")

    def show_button_of_type(self, button_config: dict, extra_kwargs: dict,
                            container: Any):
        """
        Show a button based on the button_config
        Args:
            button_config (dict): button configuration
                {
                    "text": "Answer Question",
                    "key": "generate_text",
                    "enable_config_name": "GENERATE_TEXT_ENABLED",
                    "type": "checkbox",
                }
        """
        submitted = None
        button_type = button_config.get("type", "button")
        if button_type == "checkbox":
            submitted = container.checkbox(
                button_config["text"],
                key=button_config["key"],
                **extra_kwargs)
        elif button_type == "spacer":
            container.write(button_config.get("text", ""))
        elif button_type == "submit":
            submitted = container.form_submit_button(
                button_config["text"])
        else:
            # Defaults to button
            submitted = container.button(
                button_config["text"],
                key=button_config["key"],
                **extra_kwargs)
        return submitted

    def show_buttons_row(
        self,
        buttons_config: list,
        fill_missing_spaces: bool = False
    ):
        """
        Show buttons based on the buttons_config
        Args:
            buttons_config (listo): list of buttons configurations
                [
                    # Button example with enable config
                    {
                        "text": "Answer Question",
                        "key": "generate_text",
                        "enable_config_name": "TEXT_GENERATION_ENABLED",
                    },
                    # Button example with a function and no enable config
                    {
                        "text": "Enhance prompt",
                        "key": "prompt_enhancement",
                        "on_change": cgsl.prompt_enhancement
                    },

        Returns:
            None
        """
        col = st.columns(len(buttons_config))
        col_index = 0
        submitted = []
        for button in buttons_config:
            extra_kwargs = {}
            for key in ["on_change", "on_click", "args"]:
                if button.get(key, None):
                    extra_kwargs[key] = button[key]
            if button.get("enable_config_name", None):
                with col[col_index]:
                    if self.get_par_value(button["enable_config_name"], True):
                        submitted.append(
                            self.show_button_of_type(
                                button,
                                extra_kwargs,
                                col[col_index]))
                        col_index += 1
                    else:
                        if fill_missing_spaces:
                            st.write("")
                            col_index += 1
            else:
                with col[col_index]:
                    submitted.append(
                        self.show_button_of_type(
                            button,
                            extra_kwargs,
                            col[col_index]))
                    col_index += 1
        return submitted

    def get_buttons_submitted_data(self, buttons_submitted: list,
                                   buttons_data: dict,
                                   submit_button_verification: bool = True):
        """
        Reduce the list of buttons submitted to a single boolean value
        to determine if the form was submitted
        """
        submitted = any(buttons_submitted)

        # log_debug(f"buttons_submitted: {buttons_submitted}", debug=DEBUG)

        buttons_submitted_data = {}
        if submitted:
            # Get the button submitted values
            # and create a dictionary with the form data

            curr_item = 0
            for i in range(len(buttons_data)):
                if not submit_button_verification or \
                   buttons_data[i].get("type") == "submit":
                    if buttons_data[i].get("enable_config_name", None):
                        if self.get_par_value(
                                buttons_data[i]["enable_config_name"], True):
                            # log_debug(f"buttons_data[{i}]: {buttons_data[i]}"
                            #     f" -> {buttons_submitted[curr_item]}",
                            buttons_submitted_data[buttons_data[i]["key"]] = \
                                buttons_submitted[curr_item]
                            curr_item += 1
                    else:
                        # log_debug(f"buttons_data[{i}]: {buttons_data[i]} "
                        #     f"-> {buttons_submitted[curr_item]}",
                        #     debug=DEBUG)
                        buttons_submitted_data[buttons_data[i]["key"]] = \
                            buttons_submitted[curr_item]
                        curr_item += 1
        return buttons_submitted_data

    def get_option_index(self, options: list, value: str):
        """
        Returns the index of the option in the list
        """
        for i, option in enumerate(options):
            if option == value:
                return i
        return 0

    def show_form_fields(self, fields_data: dict, form_data: dict):
        """
        Show the form
        """
        fields_values = {}
        for key in fields_data:
            field = fields_data.get(key)
            if not field.get("enabled", True):
                continue
            value = form_data.get(key, "")
            if field.get("type") == "selectbox":
                field_value = st.selectbox(
                    field.get("title"),
                    field.get("options", []),
                    # key=key,  # If this is set, the value is not assigned
                    help=field.get("help"),
                    index=self.get_option_index(
                        options=field.get("options", []),
                        value=value),
                )
            elif field.get("type") == "radio":
                field_value = st.radio(
                    field.get("title"),
                    field.get("options", []),
                    # key=key,  # If this is set, the value is not assigned
                    help=field.get("help"),
                    index=self.get_option_index(
                        options=field.get("options", []),
                        value=value),
                )
            elif field.get("type") == "text":
                field_value = st.text_input(
                    field.get("title"),
                    value,
                    # key=key,  # If this is set, the value is not assigned
                    help=field.get("help"),
                )
            else:
                field_value = st.text_area(
                    field.get("title"),
                    value,
                    # key=key,  # If this is set, the value is not assigned
                    help=field.get("help"),
                )
            fields_values[key] = field_value
        return fields_values

    def show_form_error(self, message: str):
        """
        Show a form submission error
        """
        show_popup(
            title="The following error(s) were found:",
            message=message,
            msg_type="error")

    def add_buttons_and_return_submitted(self, buttons_config: list):
        """
        Add the buttons to the page, then returns the submitted buttons and the
        buttons configuration
        """
        with st.container():
            submitted = self.show_buttons_row(buttons_config)
            return submitted, buttons_config

    def get_selected_feature(self, form: dict, features_data: dict):
        """
        Returns the selected feature
        """
        log_debug(f"get_selected_feature | form: {form}", debug=DEBUG)
        log_debug(f"get_selected_feature | features_data: {features_data}",
                  debug=DEBUG)
        selected_feature = None
        for key in form.get("buttons_submitted_data"):
            for feature in features_data:
                if form["buttons_submitted_data"].get(key) and \
                        key == feature:
                    selected_feature = feature
                    break
        return selected_feature

    def get_form_name(self, form_config: dict):
        """
        Returns the form session state key
        """
        form_name = form_config.get("name", "application_form")
        return f"{form_name}"

    def get_form_session_state_key(self, form_config: dict):
        """
        Returns the form session state key
        """
        form_name = self.get_form_name(form_config)
        form_session_state_key = form_config.get(
            "form_session_state_key",
            f"{form_name}_data")
        return form_session_state_key

    def show_form(self, form_config: dict):
        """
        Show the configured form
        """
        form_name = self.get_form_name(form_config)
        form_session_state_key = self.get_form_session_state_key(form_config)
        if form_session_state_key not in st.session_state:
            st.session_state[form_session_state_key] = {}
        form_data = st.session_state[form_session_state_key]

        fields_data = form_config.get("fields", {})

        # Clear the form data if it's not the first time the form is shown
        if form_name in st.session_state:
            del st.session_state[form_name]

        with st.form(form_name):
            st.title(form_config.get("title", "Application Form"))

            if form_config.get("subtitle"):
                st.write(form_config.get("subtitle"))

            fields_values = self.show_form_fields(fields_data, form_data)

            if form_config.get("suffix"):
                st.write(form_config.get("suffix"))

            func = form_config.get(
                "buttons_function",
                self.add_buttons_and_return_submitted)
            if form_config.get("buttons_config"):
                buttons_submitted, buttons_data = func(
                    form_config["buttons_config"])
            else:
                buttons_submitted, buttons_data = [], []

        buttons_submitted_data = self.get_buttons_submitted_data(
            buttons_submitted,
            buttons_data)
        if not buttons_submitted_data:
            return None

        st.session_state[form_session_state_key] = dict(fields_values)
        st.session_state[form_session_state_key].update({
            "buttons_submitted_data": buttons_submitted_data
        })
        return st.session_state[form_session_state_key]

    # No-form processing
    def process_no_form_buttons(
        self,
        forms_config_name: str,
        question: str,
        process_form_func: Callable,
        submit_form_func: Callable
    ):
        """
        Process No-Form buttons, like the ones for the
        the ideation-from-prompt feature
        """

        ideation_from_prompt_config = \
            st.session_state.forms_config[forms_config_name]
        ideation_from_prompt_buttons_config = \
            ideation_from_prompt_config.get("buttons_config")
        i = 0
        buttons_submitted = []
        process_form = False
        for button in ideation_from_prompt_buttons_config:
            button_was_clicked = True if st.session_state.get(button["key"]) \
                                else False
            if button_was_clicked:
                process_form = True
            buttons_submitted.append(button_was_clicked)
            i += 1
        data = {
            "buttons_submitted": buttons_submitted,
            "question": question,
        }
        if process_form:
            form = process_form_func(None, "process_form", data)
            if not question:
                self.show_form_error("No question / prompt to process")
            else:
                # Assign here the question to the session state because
                # the question assignment in the process_form_func()
                # when it call the llm is suppressed, to preserve the
                # original question
                st.session_state.question = question
                submit_form_func(
                    form,
                    ideation_from_prompt_config)

    # General functions

    def get_par_value(self, param_name: str, default_value: str = None):
        """
        Returns the parameter value. If the parameter value is a file path,
        it will be read and returned.
        """
        result = self.params.get(param_name, default_value)
        if result and isinstance(result, str) and result.startswith("[") \
           and result.endswith("]"):
            # result = read_file(f"config/{result[1:-1]}")
            file_path = os.path.join(
                os.path.dirname(os.path.abspath(__file__)),
                f"../config/{result[1:-1]}")
            result = read_file(file_path)
        return result

    def get_par_or_env(self, param_name: str, default_value: str = None):
        """
        Returns the parameter value or the environment variable value
        """
        if os.environ.get(param_name):
            return os.environ.get(param_name)
        return self.get_par_value(param_name, default_value)

    def add_js_script(self, source: str):
        """
        Add a JS script to the page
        """
        # Reference:
        # Injecting JS?
        # https://discuss.streamlit.io/t/injecting-js/22651/5?u=carlos9
        # The following snippet could help you solve your cross-origin issue:
        div_id = uuid.uuid4()
        st.markdown(f"""
            <div style="display:none" id="{div_id}">
                <iframe src="javascript: \
                    var script = document.createElement('script'); \
                    script.type = 'text/javascript'; \
                    script.text = {html.escape(repr(source))}; \
                    var div = window.parent.document.getElementById('{div_id}'); \
                    div.appendChild(script); \
                    div.parentElement.parentElement.parentElement.style.display = 'none'; \
                "/>
            </div>
        """, unsafe_allow_html=True)
