import React, { useState, useEffect } from 'react';

import * as gs from "genericsuite";

// const authenticationService = gs.authenticationService.authenticationService;
const useUser = gs.UserContext.useUser;
const console_debug_log = gs.loggingService.console_debug_log;

const debug = false;

export const HomePage = ({ children }) => {
    if (debug) console_debug_log('>>>> MedOffline HomePage <<<<');
    const { currentUser } = useUser();
    return (
        <gs.HomePage>
            <>
                {currentUser &&
                    (<p>Hi {currentUser.firstName}! Welcome to MedOffline</p>)
                }
            </>
            {children}
        </gs.HomePage>
    );
}
