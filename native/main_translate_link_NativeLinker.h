#ifndef _Included_main_translate_link_NativeLinker
#define _Included_main_translate_link_NativeLinker

#include "stdafx.h"
#include "linker.h"

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

    /*
     * Class:     main_translate_link_NativeLinker
     * Method:    create
     * Signature: (Ljava/lang/String;Ljava/util/HashMap;)V
     */
    JNIEXPORT void JNICALL Java_main_translate_link_NativeLinker_create(JNIEnv *env, jobject obj, jstring defaultRootPath, jstring outRootPath, jstring outLinkPath, jobject words);

#ifdef __cplusplus
}
#endif

#endif // _Included_main_translate_link_NativeLinker
