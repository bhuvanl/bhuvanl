#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <nativeRedis.h>
#include <hiredis.h>
// generated by javah via maven-native-plugin

JNIEXPORT void JNICALL Java_redis_benchmark_hiredis_NativeRedis_helloRedis
(JNIEnv * env, jobject obj)
{
	puts("Hello from Redis!!!");
}

JNIEXPORT jlong JNICALL Java_redis_benchmark_hiredis_NativeRedis_connect(
		JNIEnv * env, jobject obj, jstring host, jint jport) {
	const char *ip = env->GetStringUTFChars(host, NULL);
	const int port = (int) jport;
	redisContext *connection = redisConnect(ip, port);
	env->ReleaseStringUTFChars(host, ip);
	return (long) connection;
}

JNIEXPORT void JNICALL Java_redis_benchmark_hiredis_NativeRedis_close
(JNIEnv * env, jobject obj,jlong jconnection)
{
	redisContext *connection = (redisContext*)jconnection;
	redisFree(connection);
}

JNIEXPORT jobjectArray JNICALL Java_redis_benchmark_hiredis_NativeRedis_command(
		JNIEnv * env, jobject obj, jlong jconnection, jstring commandString) {
	redisContext *connection = (redisContext*) jconnection;
	const char *command = env->GetStringUTFChars(commandString, NULL);
	redisReply *reply = (redisReply*) redisCommand(connection, command);
	env->ReleaseStringUTFChars(commandString, command);
	jobjectArray elements = env->NewObjectArray(reply->elements,
			env->FindClass("java/lang/String"), env->NewStringUTF(""));
	for (int i = 0; i < reply->elements; i++) {
		env->SetObjectArrayElement(elements, i,
				env->NewStringUTF(reply->element[i]->str));
	}
	return elements;
}
