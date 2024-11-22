import React from 'react'

import * as gs from "genericsuite";

const GsAboutBody = gs.AboutBody;
const console_debug_log = gs.loggingService.console_debug_log;

const debug = false;

export const AboutBody = ({ children }) => {
    if (debug) console_debug_log('>>>> MedOffline AboutBody <<<<');
    return (
        <GsAboutBody>
            <>
                <p>
                    <b>MedOffline</b> is a cutting-edge application designed to revolutionize the real estate market by simplifying the process for both buyers and sellers. Utilizing an advanced AI  Assistant technology, the app offers a conversational interface where users can enter their real estate preferences and requirements.
                </p>
                <p>
                    2024-11-15 | Carlos J. Ramirez
                </p>
                {children}
            </>
        </GsAboutBody>
    )
}
