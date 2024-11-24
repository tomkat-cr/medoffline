import React from 'react';

import * as gs from "genericsuite";
import users_profile from "../../configs/frontend/users_profile.json";
import {
    GENDERS,
    USER_TYPES,
} from '../../constants/app_constants';
import { UsersPreferences } from '../UsersMenu/UsersPreferences';
// import { UsersDbPostWrite } from '../SuperAdminOptions/Users';

// const authenticationService = gs.authenticationService.authenticationService;
const useUser = gs.UserContext.useUser;
const GenericSinglePageEditor = gs.genericEditorSinglepage.GenericSinglePageEditor;
const GetFormData = gs.genericEditorRfcService.GetFormData;
// const console_debug_log = gs.loggingService.console_debug_log;
const BILLING_PLANS = gs.appConstants.BILLING_PLANS;
const LANGUAGES = gs.generalConstants.LANGUAGES;
const TRUE_FALSE = gs.generalConstants.TRUE_FALSE;

const UsersConfig = gs.UsersConfig;
// const UsersDbPostWrite = gs.UsersDbPostWrite;
const UsersDbListPreRead = gs.UsersDbListPreRead;
const UsersDbPreWrite = gs.UsersDbPreWrite;
const UsersValidations = gs.UsersValidations;
const UsersPasswordValidations = gs.UsersPasswordValidations;

export function UsersProfile_EditorData() {
    const registry = {
        // User's Profile
        "GENDERS": GENDERS,
        "LANGUAGES": LANGUAGES, 
        "TRUE_FALSE": TRUE_FALSE,
        "BILLING_PLANS": BILLING_PLANS,
        "USER_TYPES": USER_TYPES,
        "UsersPreferences": UsersPreferences,
        "UsersDbPostWrite": UsersDbPostWrite,
        "UsersConfig": UsersConfig,
        "UserProfileEditor": UserProfileEditor,
        "UsersDbListPreRead": UsersDbListPreRead,
        "UsersDbPreWrite": UsersDbPreWrite,
        "UsersValidations": UsersValidations,
        "UsersPasswordValidations": UsersPasswordValidations,
    }
    return GetFormData(users_profile, registry, 'UserProfileEditor');
}

export const UserProfileEditor = (props) => {
    // const { currentUserValue } = authenticationService;
    const { currentUser } = useUser();
    return (
        <>
            <GenericSinglePageEditor
                // id={currentUserValue.id}
                id={currentUser.id}
                editorConfig={UsersProfile_EditorData()}
            />
        </>
    );
}
