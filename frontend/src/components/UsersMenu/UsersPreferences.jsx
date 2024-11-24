import React from 'react';

import * as gs from "genericsuite";
import users_preferences from "../../configs/frontend/users_preferences.json";
import {
    PROPERTY_TYPES,
    PROPERTY_FOR,
} from '../../constants/app_constants';

const GenericCrudEditor = gs.genericEditorRfcService.GenericCrudEditor;
const GetFormData = gs.genericEditorRfcService.GetFormData;
// const console_debug_log = gs.loggingService.console_debug_log;


export function UsersPreferences_EditorData() {
    // console_debug_log("UsersPreferences_EditorData");
    const registry = {
        "PROPERTY_TYPES": PROPERTY_TYPES,
        "PROPERTY_FOR": PROPERTY_FOR,
    }
    return GetFormData(users_preferences, registry, false);
}

export function UsersPreferences() {
    return {
        editorConfig: UsersPreferences_EditorData(),
        component: UsersPreferencesComponent
    };
}

export const UsersPreferencesComponent = ({parentData}) => (
    <GenericCrudEditor
        editorConfig={UsersPreferences_EditorData()}
        parentData={parentData}
    />
)
