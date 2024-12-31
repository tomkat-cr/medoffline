import React from 'react';

import * as gsAi from "genericsuite-ai";

import { HomePage } from '../HomePage/HomePage.jsx';
import { AboutBody } from '../About/About.jsx';
import { UserProfileEditor } from "../UsersMenu/UserProfile.jsx";
import { Users_EditorData } from "../SuperAdminOptions/Users.jsx";

const componentMap = {
    "AboutBody": AboutBody,
    "HomePage": HomePage,
    "UserProfileEditor": UserProfileEditor,
    "Users_EditorData": Users_EditorData,
    "UserProfileEditor": UserProfileEditor,
    "Users_EditorData": Users_EditorData,
};

export const App = () => {
    return (
        <gsAi.App
            appLogo="MedOffLine.circled.logo.500.png"
            componentMap={componentMap}
        />
    );
}