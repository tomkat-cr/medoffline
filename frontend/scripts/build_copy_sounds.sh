#!/bin/bash
# File: scripts/build_copy_sounds.sh
# 2024-03-16 | CR
#
echo "Copying sounds to build/static/media directory..."
echo "from: $(pwd)"
echo ""

mkdir -p ./build/static/media

if [ -d ./src/lib/sounds ]; then
    cp ./src/lib/sounds/* ./build/static/media
fi
if [ -d ./src/lib/_sounds ]; then
    cp ./src/lib/_sounds/* ./build/static/media
fi
if [ -d ./node_modules/genericsuite/src/lib/sounds ]; then
    cp ./node_modules/genericsuite/src/lib/sounds/* ./build/static/media
fi
if [ -d ./node_modules/genericsuite-ai/src/lib/sounds ]; then
    cp ./node_modules/genericsuite-ai/src/lib/sounds/* ./build/static/media
fi
if [ -d ./src/sounds ]; then
    cp ./src/sounds/* ./build/static/media
fi
if [ -d ./src/_sounds ]; then
    cp ./src/_sounds/* ./build/static/media
fi

echo ""
echo "Done!"
