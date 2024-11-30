"""
MedOffLine App
"""
from dotenv import load_dotenv

import streamlit as st

from lib.codegen_streamlit_lib import StreamlitLib
from lib.codegen_utilities import get_app_config
# from lib.codegen_utilities import log_debug

DEBUG = True

APK_DONWLOAD_URL = "https://www.carlosjramirez.com/downloads/medoffline.apk"


def load_config(lang: str = None):
    """
    Load the configuration
    """
    if not lang:
        lang = "es"
    config_file = f"./public/config/app_config_{lang}.json"
    app_config = get_app_config(config_file)
    return StreamlitLib(app_config)


if "lang" not in st.session_state:
    st.session_state.lang = "es"
cgsl = load_config(st.session_state.lang)


# UI elements


def add_language_button():
    """
    Languaje change button
    """
    st.button(
        "English" if st.session_state.lang == "es" else "Español",
        key="change_lang"
    )


def add_title(page_name: str = None):
    """
    Add the title section to the page
    """
    # Emoji shortcodes
    # https://streamlit-emoji-shortcodes-streamlit-app-gwckff.streamlit.app/

    cols = st.columns(3, vertical_alignment="center")
    with cols[0]:
        st.title(cgsl.get_title())
    with cols[1]:
        add_language_button()
    if page_name:
        st.subheader(page_name)
    st.divider()


def get_app_description():
    """
    Add ppp description
    """
    app_desc = cgsl.get_par_value("APP_DESCRIPTION")
    app_desc = app_desc.replace(
        "{app_name}",
        f"**{st.session_state.app_name}**")
    # log_debug(f"App description: {app_desc}", debug=DEBUG)
    return app_desc


def add_sidebar():
    """
    Add the sidebar to the page
    """
    with st.sidebar:
        app_desc = get_app_description()
        st.sidebar.write(app_desc)
        # Data management
        cgsl.data_management_components()
        data_management_container = st.empty()
        # Show the conversations in the side bar
        cgsl.show_conversations()
    return data_management_container


def add_homepage_button():
    """
    Add the homepage button to the page
    """
    st.divider()
    st.button(
        cgsl.get_par_value("HOME_BUTTON_TEXT"),
        on_click=cgsl.set_query_param,
        args=("page", "home"))


def add_main_content():
    with st.container():
        cols = st.columns(2)
        with cols[0]:
            st.image("./public/assets/MedOffLine_cover_image_2.png")
        with cols[1]:
            app_desc = get_app_description()
            app_features = cgsl.get_par_value("APP_FEATURES")
            st.header(cgsl.get_title())
            st.write(app_desc)
            st.write(app_features)
            # App instructions
            app_instructions = cgsl.get_par_value("APP_INSTRUCTIONS")
            st.write(app_instructions)
            button_cols = st.columns(3)
            with button_cols[0]:
                # Button to download a file from "./apk/app.apk"
                st.link_button(
                    cgsl.get_par_value("DOWNLOAD_BUTTON_TEXT"),
                    APK_DONWLOAD_URL)
            button_cols = st.columns(3)
            with button_cols[0]:
                # Source Code page
                st.button(
                    cgsl.get_par_value("SOURCE_CODE_BUTTON_TEXT"),
                    on_click=cgsl.set_query_param,
                    args=("page", "source_code"))
            with button_cols[1]:
                # Presentations page
                st.button(
                    cgsl.get_par_value("PRESENTATIONS_BUTTON_TEXT"),
                    on_click=cgsl.set_query_param,
                    args=("page", "presentations"))
            with button_cols[2]:
                # Team page
                st.button(
                    cgsl.get_par_value("TEAM_BUTTON_TEXT"),
                    on_click=cgsl.set_query_param,
                    args=("page", "team"))

            # Logo
            st.image("./assets/MedOffLine.circled.logo.500.png", width=250)


def add_check_buttons_pushed():
    """
    Check buttons pushed
    """
    if st.session_state.change_lang:
        st.session_state.lang = "en" if st.session_state.lang == "es" else "es"
        st.rerun()


def add_footer():
    """
    Add the footer to the page
    """
    st.caption(f"© 2024 {st.session_state.maker_name}. All rights reserved.")


# Pages


def homepage():
    # Main content

    # Title
    # add_title()

    # Sidebar
    # data_management_container = add_sidebar()
    # add_sidebar()

    # Main content
    add_main_content()

    # Check buttons pushed
    add_check_buttons_pushed()

    # Footer
    with st.container():
        add_footer()


def source_code_page():
    # Main content

    # Title
    add_title(cgsl.get_par_value("SOURCE_CODE_BUTTON_TEXT"))

    # Content

    # Source Code: https://github.com/tomkat-cr/medoffline
    st.link_button(
        cgsl.get_par_value("SOURCE_CODE_URL_TEXT"),
        "https://github.com/tomkat-cr/medoffline")

    # Models on Kaggle:
    st.link_button(
        cgsl.get_par_value("KAGGLE_MODELS_URL_TEXT"),
        "https://www.kaggle.com/models/tomkatcr/llama3.2_3b_pte")

    # Back to homepage
    add_homepage_button()

    # Check buttons pushed
    add_check_buttons_pushed()

    # Footer
    with st.container():
        add_footer()


def presentations_page():
    # Main content

    # Title
    add_title(cgsl.get_par_value("PRESENTATIONS_BUTTON_TEXT"))

    # Content

    # Video Presentation (español): https://youtu.be/R-rAKMbKVus
    st.link_button(
        cgsl.get_par_value("PRESENTATION_VIDEO_URL_TEXT"),
        cgsl.get_par_value("PRESENTATION_VIDEO_YOUTUBE_URL"))

    # Presentation Document (PDF): https://storage.googleapis.com/lablab-static-eu/presentations/submissions/cm3xdpe8l00113b713e90gmb5/cm3xdpe8l00113b713e90gmb5-1732561102890_zq1alr0cwl.pdf
    st.link_button(
        cgsl.get_par_value("PRESENTATION_DOCUMENT_URL_TEXT"),
        cgsl.get_par_value("PRESENTATION_DOCUMENT_STORAGE_URL_TEXT"))

    # Project description: https://www.linkedin.com/pulse/medoffline-empoderando-comunidades-con-orientaci%C3%B3n-m%C3%A9dica-ramirez-spd4e
    st.link_button(
        cgsl.get_par_value("PROJECT_DESCRIPTION_URL_TEXT"),
        cgsl.get_par_value("PROJECT_DESCRIPTION_STORAGE_URL_TEXT"))

    st.write(cgsl.get_par_value("HACKATHON_DESCRIPTION"))
    st.link_button(
        cgsl.get_par_value("HACKATHON_SUBMISSION_PAGE_BUTTON_TEXT"),
        cgsl.get_par_value("HACKATHON_SUBMISSION_PAGE_BUTTON_URL"))

    # Back to homepage
    add_homepage_button()

    # Check buttons pushed
    add_check_buttons_pushed()

    # Footer
    with st.container():
        add_footer()


def team_page():
    # Main content

    # Title
    add_title(cgsl.get_par_value("TEAM_BUTTON_TEXT"))

    # Content

    # Team description
    st.write(cgsl.get_par_value("TEAM_DESCRIPTION"))

    # Team page
    st.link_button(
        cgsl.get_par_value("TEAM_PAGE_BUTTON_TEXT"),
        cgsl.get_par_value("TEAM_PAGE_BUTTON_URL"))

    # Meet the team
    st.subheader(cgsl.get_par_value("MEET_THE_TEAM_TEXT"))

    # Team members
    team_members = cgsl.get_par_value("TEAM_MEMBERS")
    cols = st.columns(2)
    curr_col = 0
    for member in team_members:
        if curr_col == 2:
            curr_col = 0
            cols = st.columns(2)
        with cols[curr_col]:
            curr_col += 1
            st.write(f"**{member['NAME']}**")
            st.write(member['ROLE_NAME'])
            st.write(member['ROLE_DESCRIPTION'])
            st.link_button("Linkedin", member['LINKEDIN'])

    # Team photo
    st.image("./assets/The.FynBots.2024-11-25.IMG_0749.jpg", width=900)

    # Back to homepage
    add_homepage_button()

    # Check buttons pushed
    add_check_buttons_pushed()

    # Footer
    with st.container():
        add_footer()


def invalid_page():
    # Main content

    # Title
    add_title(cgsl.get_par_value("INVALID_PAGE_TITLE"))

    # Content

    # Invallid page message
    st.write(cgsl.get_par_value("INVALID_PAGE_TEXT"))

    # Back to homepage
    add_homepage_button()

    # Check buttons pushed
    add_check_buttons_pushed()

    # Footer
    with st.container():
        add_footer()


# Main


# Main function to render pages
def main():
    load_dotenv()

    st.session_state.app_name = cgsl.get_par_or_env("APP_NAME")
    st.session_state.app_version = cgsl.get_par_or_env("APP_VERSION")
    st.session_state.app_name_version = \
        f"{st.session_state.app_name} v{st.session_state.app_version}"
    st.session_state.maker_name = cgsl.get_par_or_env("MAKER_MAME")
    st.session_state.app_icon = cgsl.get_par_or_env("APP_ICON", ":sparkles:")

    # Streamlit app code
    st.set_page_config(
        page_title=st.session_state.app_name_version,
        page_icon=st.session_state.app_icon,
        layout="wide",
        initial_sidebar_state="auto",
    )

    # Query params to handle navigation
    page = st.query_params.get("page", cgsl.get_par_value("DEFAULT_PAGE"))

    # Page navigation logic
    if not page or page == "home":
        homepage()
    elif page == "source_code":
        source_code_page()
    elif page == "presentations":
        presentations_page()
    elif page == "team":
        team_page()
    else:
        # Invalid page
        invalid_page()


if __name__ == "__main__":
    main()
