"""
VitexBrain App
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


def add_title():
    """
    Add the title section to the page
    """

    # Emoji shortcodes
    # https://streamlit-emoji-shortcodes-streamlit-app-gwckff.streamlit.app/
    pass


def get_app_description():
    """
    Add the sidebar to the page
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
            button_cols = st.columns(2)
            with button_cols[0]:
                # Button to download a file from "./apk/app.apk"
                st.link_button(
                    "Descargar APK" if st.session_state.lang == "es"
                    else "Download APK",
                    APK_DONWLOAD_URL)
                # Languaje change
            with button_cols[1]:
                st.button(
                    "English" if st.session_state.lang == "es" else "Español",
                    key="change_lang"
                )

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


def page_1():
    # Main content

    # Title
    add_title()

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
        page_1()


if __name__ == "__main__":
    main()
