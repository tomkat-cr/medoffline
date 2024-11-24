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
                    <b>MedOffline</b> is an offline AI Assistant that provides basic first aid guidance in communities with limited (or no) Internet access. It offers:
                    <ul>
                        <li>First aid guides.</li>
                        <li>Preliminary diagnostics based on symptoms.</li>
                        <li>Information on basic medications (dosages, side effects).</li>
                        <li>Guidance on when and where to seek medical assistance.</li>
                        <li>Educational materials on preventing common illnesses.</li>
                    </ul>
                </p>
                <p>
                    2024-11-24 | The FynBots
                </p>
                {children}
            </>
        </GsAboutBody>
    )
}
