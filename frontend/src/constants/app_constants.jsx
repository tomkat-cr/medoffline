import constants from "../configs/frontend/app_constants.json";
import genConstants from "../configs/frontend/general_constants.json";
import * as gs from "genericsuite";

const buildConstant = gs.jsonUtilities.buildConstant;

export const GENDERS = buildConstant(genConstants.GENDERS);
export const BILLING_PLANS = buildConstant(constants.BILLING_PLANS);
export const ERROR_MESSAGES = constants.ERROR_MESSAGES;
export const APP_EMAILS = constants.APP_EMAILS;
export const APP_VALID_URLS = constants.APP_VALID_URLS;

export const USER_TYPES = buildConstant(constants.USER_TYPES);
export const PROPERTY_STATUS = buildConstant(constants.PROPERTY_STATUS);
export const PROPERTY_TYPES = buildConstant(constants.PROPERTY_TYPES);
export const PROPERTY_FOR = buildConstant(constants.PROPERTY_FOR);
export const TRANSACTION_STATUS = buildConstant(constants.TRANSACTION_STATUS);
