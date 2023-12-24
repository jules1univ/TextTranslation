#include "main_translate_link_NativeLinker.h"

JNIEXPORT void JNICALL Java_main_translate_link_NativeLinker_create(JNIEnv* env, jobject obj, jstring defaultRootPath, jstring outRootPath, jstring outLinkPath, jobject words)
{	
	jclass jHashMap = env->GetObjectClass(words);
	jmethodID jHashMapkeySetMethod = env->GetMethodID(jHashMap, "keySet", "()Ljava/util/Set;");

	jobject jkeySet = env->CallObjectMethod(words, jHashMapkeySetMethod);
	if (jkeySet == nullptr) return;

	jclass jSet = env->FindClass("java/util/Set");
	jmethodID jSetToArrayMethod = env->GetMethodID(jSet, "toArray", "()[Ljava/lang/Object;");

	jobjectArray keysArray = (jobjectArray)env->CallObjectMethod(jkeySet, jSetToArrayMethod);
	if (keysArray == nullptr) return;


	jsize length = env->GetArrayLength(keysArray);
	std::unordered_set<std::string> keys(length);

	for (jsize i = 0; i < length; ++i)
	{
		jstring key = (jstring)env->GetObjectArrayElement(keysArray, i);
		keys.insert(env->GetStringUTFChars(key, nullptr));
		env->DeleteLocalRef(key); 
	}
	env->DeleteLocalRef(keysArray);

		CreateLinkTree(
		env->GetStringUTFChars(defaultRootPath, nullptr),
		env->GetStringUTFChars(outRootPath, nullptr),
		env->GetStringUTFChars(outLinkPath, nullptr),
		keys);
	
}
