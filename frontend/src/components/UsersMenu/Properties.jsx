import React from 'react';

import * as gs from "genericsuite";
import * as gsAi from "genericsuite-ai";

import properties from "../../configs/frontend/properties.json";
import {
    PROPERTY_STATUS,
    PROPERTY_TYPES,
    PROPERTY_FOR,
} from '../../constants/app_constants.jsx';

// const dbApiService = gs.dbService.dbApiService;
// const authenticationService = gs.authenticationService.authenticationService;
const useUser = gs.UserContext.useUser;
const GenericCrudEditor = gs.genericEditorRfcService.GenericCrudEditor;
const GetFormData = gs.genericEditorRfcService.GetFormData;
const GenericSelectGenerator = gs.genericEditorRfcSelector.GenericSelectGenerator;
const GenericSelectDataPopulator = gs.genericEditorRfcSelector.GenericSelectDataPopulator;
const genericFuncArrayDefaultValue = gs.genericEditorRfcSpecificFunc.genericFuncArrayDefaultValue;
const console_debug_log = gs.loggingService.console_debug_log;
// const ACTION_DELETE = gs.generalConstants.ACTION_DELETE;


export function Properties_EditorData() {
    const registry = {
        "Properties": Properties, 
        "PropertiesDataPopulator": PropertiesDataPopulator, 
        "PropertiesValidations": PropertiesValidations, 
        "PROPERTY_STATUS": PROPERTY_STATUS,
        "PROPERTY_TYPES": PROPERTY_TYPES,
        "PROPERTY_FOR": PROPERTY_FOR,
    }
    return GetFormData(properties, registry, 'Properties_EditorData');
}

export const Properties = () => (
    <GenericCrudEditor editorConfig={Properties_EditorData()} />
)

export const PropertiesSelect = (props) => {
    const { currentUser } = useUser();
    const userIdFilter = {'user_id': currentUser.id}
    console_debug_log("*** PropertiesSelect *** | userIdFilter:");
    console_debug_log(userIdFilter);
    return (
        <GenericSelectGenerator
            filter={typeof props.filter == 'undefined' ? null : props.filter}
            show_description={typeof props.show_description == 'undefined' ? false : props.show_description}
            description_fields={["address"]}
            editorConfig={Properties_EditorData()}
            dbFilter={userIdFilter}
        />
    )
}

export const PropertiesDataPopulator = () => {
    // const { currentUser } = useUser();
    // const userIdFilter = {'user_id': currentUser.id}
    // console_debug_log("*** PropertiesDataPopulator *** | userIdFilter:");
    // console_debug_log(userIdFilter);
    return (
        <GenericSelectDataPopulator
            editorConfig={Properties_EditorData()}
            // dbFilter={userIdFilter}
        />
    );
}

export const PropertiesValidations = (data, editor, action) => {
    // Properties pre-deletion validations
    return new Promise((resolve, reject) => {
        let resp = genericFuncArrayDefaultValue(data);
        switch(action) {
            default:
                resolve(resp);
        }
    });
}
