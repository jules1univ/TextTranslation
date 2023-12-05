#!/bin/bash

JDK_PATH=""
CC="g++"

OUTPUT_DIR="../"

$CC -shared -fPIC -I"$JDK_PATH/include" -I"$JDK_PATH/include/linux" -std=c++17 -o $OUTPUT_DIR/linker.so main_translate_link_NativeLinker.cpp linker.cpp