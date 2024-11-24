import React from 'react';

import * as gs from "genericsuite";
import * as gsAi from "genericsuite-ai";

import transactions from "../../configs/frontend/transactions.json";
import {
    TRANSACTION_STATUS,
} from '../../constants/app_constants.jsx';
import { UsersSelect, UsersDataPopulator } from '../SuperAdminOptions/Users.jsx';
import { PropertiesDataPopulator, PropertiesSelect } from './Properties.jsx';

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


export function Transactions_EditorData() {
    const registry = {
        "Transactions": Transactions, 
        // "TransactionsDataPopulator": TransactionsDataPopulator, 
        // "TransactionsValidations": TransactionsValidations, 
        "TRANSACTION_STATUS": TRANSACTION_STATUS,
        "UsersSelect": UsersSelect,
        "UsersDataPopulator": UsersDataPopulator,
        "PropertiesSelect": PropertiesSelect,
        "PropertiesDataPopulator": PropertiesDataPopulator,
    }
    return GetFormData(transactions, registry, 'Transactions_EditorData');
}

export const Transactions = () => (
    <GenericCrudEditor editorConfig={Transactions_EditorData()} />
)

export const TransactionsSelect = (props) => {
    const { currentUser } = useUser();
    const userIdFilter = {'user_id': currentUser.id}
    console_debug_log("*** TransactionsSelect *** | userIdFilter:");
    console_debug_log(userIdFilter);
    return (
        <GenericSelectGenerator
            filter={typeof props.filter == 'undefined' ? null : props.filter}
            show_description={typeof props.show_description == 'undefined' ? false : props.show_description}
            editorConfig={Transactions_EditorData()}
            dbFilter={userIdFilter}
        />
    )
}

export const TransactionsDataPopulator = () => {
    const { currentUser } = useUser();
    const userIdFilter = {'user_id': currentUser.id}
    console_debug_log("*** TransactionsDataPopulator *** | userIdFilter:");
    console_debug_log(userIdFilter);
    return (
        <GenericSelectDataPopulator
            editorConfig={Transactions_EditorData()}
            dbFilter={userIdFilter}
        />
    );
}

export const TransactionsValidations = (data, editor, action) => {
    // transactions pre-deletion validations
    return new Promise((resolve, reject) => {
        let resp = genericFuncArrayDefaultValue(data);
        switch(action) {
            default:
                resolve(resp);
        }
    });
}
