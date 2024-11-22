import React from 'react';

import * as gsAi from "genericsuite-ai";

import { HomePage } from '../HomePage/HomePage.jsx';
import { AboutBody } from '../About/About.jsx';
import { Properties_EditorData } from "../UsersMenu/Properties.jsx";
import { Transactions_EditorData } from "../UsersMenu/Transactions.jsx";
import { UserProfileEditor } from "../UsersMenu/UserProfile.jsx";
import { Users_EditorData } from "../SuperAdminOptions/Users.jsx";

const componentMap = {
    "AboutBody": AboutBody,
    "HomePage": HomePage,
    "Properties_EditorData": Properties_EditorData,
    "Transactions_EditorData": Transactions_EditorData,
    "UserProfileEditor": UserProfileEditor,
    "Users_EditorData": Users_EditorData,
};

export const App = () => {
    return (
        <gsAi.App
            appLogo="MedOffline-logo-circled-500.png"
            componentMap={componentMap}
        />
    );
}