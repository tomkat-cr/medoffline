import React from 'react';

import * as gs from "genericsuite";

import {
    GENDERS,
    BILLING_PLANS,
    USER_TYPES,
} from '../../constants/app_constants';
import { UsersPreferences } from '../UsersMenu/UsersPreferences';

import users from "../../configs/frontend/users.json";

const GenericCrudEditor = gs.genericEditorRfcService.GenericCrudEditor;
const genericFuncArrayDefaultValue = gs.genericEditorRfcSpecificFunc.genericFuncArrayDefaultValue;
const GenericSelectGenerator = gs.genericEditorRfcSelector.GenericSelectGenerator;
const GenericSelectDataPopulator = gs.genericEditorRfcSelector.GenericSelectDataPopulator;
const GetFormData = gs.genericEditorRfcService.GetFormData;
const dbApiService = gs.dbService.dbApiService;
const console_debug_log = require("genericsuite").loggingService.console_debug_log;
const processDateToTimestamp = require("genericsuite").dateTimestamp.processDateToTimestamp;

const UsersValidations = require("genericsuite").UsersValidations;
const UsersDbListPreRead = require("genericsuite").UsersDbListPreRead;
const UsersPasswordValidations = require("genericsuite").UsersPasswordValidations;
const UsersDbPreWrite = require("genericsuite").UsersDbPreWrite;

const UsersConfig = require("genericsuite").UsersConfig;

const ACTION_CREATE = gs.generalConstants.ACTION_CREATE;
const ACTION_UPDATE = gs.generalConstants.ACTION_UPDATE;
const TRUE_FALSE = gs.generalConstants.TRUE_FALSE;
const LANGUAGES = gs.generalConstants.LANGUAGES;

const debug = true;

export function Users_EditorData(calleeName='Users_EditorData') {
    const registry = {
        "GENDERS": GENDERS,
        "LANGUAGES": LANGUAGES, 
        "TRUE_FALSE": TRUE_FALSE,
        "BILLING_PLANS": BILLING_PLANS,
        "USER_TYPES": USER_TYPES,
        "UsersPreferences": UsersPreferences,
        // "UsersDbPostWrite": UsersDbPostWrite,
        "UsersConfig": UsersConfig,
        "Users": Users,
        "UsersDbListPreRead": UsersDbListPreRead,
        "UsersDbPreWrite": UsersDbPreWrite,
        "UsersValidations": UsersValidations,
        "UsersPasswordValidations": UsersPasswordValidations,
    }
    return GetFormData(users, registry, calleeName);
}

export const Users = () => (
    <GenericCrudEditor editorConfig={Users_EditorData()} />
)

export const UsersSelect = (props) => {
    // const userTypeFilter = {'user_type': ["buyer", "both"]}
    const userTypeFilter = {'user_type': "buyer"}
    return (
        <GenericSelectGenerator
            filter={typeof props.filter == 'undefined' ? null : props.filter}
            show_description={typeof props.show_description == 'undefined' ? false : props.show_description}
            description_fields={["firstname", "lastname"]}
            editorConfig={Users_EditorData()}
            dbFilter={userTypeFilter}
        />
    )
}

export const UsersDataPopulator = () => (
    <GenericSelectDataPopulator editorConfig={Users_EditorData()} />
)

/*
 * System Admin
 */

// export const UsersValidations = (data, editor, action) => { ... }
// export const UsersDbListPreRead = (data, editor, action) => { ... }
// export const UsersPasswordValidations = (data, editor, action) => { ... }
// export const UsersDbPreWrite = (data, editor, action) => { ... }

/*
 * User's Profile
 */

// export const UsersDbPostWrite = (data, editor, action) => {
//     // Add an updated entry in user_history with current user's data
//     return new Promise((resolve, reject) => {
//         let resp = genericFuncArrayDefaultValue(data);
//         const parentId = data[editor.primaryKeyName];
//         if (debug) {
//             console_debug_log('UsersDbPostWrite - parentId: ' + String(parentId) + ' | data:', data);
//         }
//         switch(action) {
//             case ACTION_CREATE:
//             case ACTION_UPDATE:
//                 const db = new dbApiService({ url: 'users_user_history' });
//                 const itemToSave = {
//                     user_id: parentId,
//                     user_history: {
//                         date: processDateToTimestamp(new Date().toISOString()),
//                         goal_code: data['goal_code'],
//                         goals: data['goals'],
//                         weight: data['weight'],
//                         weight_unit: data['weight_unit'],
//                     }
//                 }
//                 if (debug) {
//                     console_debug_log("UsersDbPostWrite - itemToSave:", itemToSave);
//                 }
//                 db.createRow(itemToSave).then(
//                     _ => {
//                         // To refresh parent component and show the new calorie total
//                         resp['otherData']['refresh'] = true;
//                         if (debug) {
//                             console_debug_log(`UsersDbPostWrite | resp:`, resp);
//                         }
//                         resolve(resp);
//                     },
//                     error => {
//                         console_debug_log(`[UDPW-020] UsersDbPostWrite | error:`, error);
//                         resp.error = true;
//                         resp.errorMsg = error;
//                         reject(resp)
//                     }
//                 );
//                 break;
//             default:
//                 resolve(resp);
//         }
//     });
// }
