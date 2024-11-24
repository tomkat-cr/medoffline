#/bin/bash
# scripts/link_configs.sh
# 2024-11-17 | CR
#
REPO_BASEDIR="`pwd`"
rm -rf ${REPO_BASEDIR}/frontend/src/configs
rm -rf ${REPO_BASEDIR}/backend/app/config_dbdef
ln -s ${REPO_BASEDIR}/gs_config ${REPO_BASEDIR}/frontend/src/configs
ln -s ${REPO_BASEDIR}/gs_config ${REPO_BASEDIR}/backend/app/config_dbdef
