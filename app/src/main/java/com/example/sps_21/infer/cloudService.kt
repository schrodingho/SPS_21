package com.example.sps_21.infer

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.aiplatform.v1.EndpointName
import com.google.cloud.aiplatform.v1.PredictRequest
import com.google.cloud.aiplatform.v1.PredictionServiceClient
import com.google.cloud.aiplatform.v1.PredictionServiceSettings
import com.google.cloud.aiplatform.v1.Value
import java.io.File
import java.nio.file.Path


class cloudService {
    private val location: String = "us-central1"
    private val apiEndpoint: String = "us-central1-aiplatform.googleapis.com"

    fun createService(project: String, endpointId: String, instances: Any): Any {
        val credentials = GoogleCredentials.getApplicationDefault()
        val client = PredictionServiceClient.create(
            PredictionServiceSettings.newBuilder()
                .setCredentialsProvider { credentials }
                .setEndpoint(apiEndpoint)
                .build()
        )

        var instancesList: Iterable<com.google.protobuf.Value> = listOf()
        val parameters : com.google.protobuf.Value = com.google.protobuf.Value.getDefaultInstance();

        val endPointName: EndpointName = EndpointName.newBuilder()
            .setProject(project)
            .setLocation(location)
            .setEndpoint(endpointId)
            .build()
//        val endpointName = "projects/$project/locations/$location/endpoints/$endpointId"
        val response = client.predict(
            PredictRequest.newBuilder()
                .setEndpoint(endPointName.toString())
                .addAllInstances(instancesList)
                .setParameters(parameters)
                .build()
        )
        return response
    }

    fun runService(credentialJsonPath: String, pcmData: File) {
//        val credentialJSON = File(curContext.cacheDir.absolutePath, spectrumName)
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialJsonPath)
        val pcmStringify = String(pcmData.readBytes(), Charsets.UTF_8)
        val preds = createService(
            project = "635622715090",
            endpointId = "2616340694851125248",
            instances = ""
        )

        Log.v("preds", "$preds")

    }
}