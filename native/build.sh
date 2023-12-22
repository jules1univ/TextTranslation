#!/bin/bash


JDK_PATH=$(dirname $(dirname $(readlink -f $(which java))))
CC="g++"

OUTPUT_DIR="../"

$CC -shared -fPIC -I"$JDK_PATH/include" -I"$JDK_PATH/include/linux" -std=c++17 -O3 -march=native -o $OUTPUT_DIR/linker.so main_translate_link_NativeLinker.cpp linker.cpp