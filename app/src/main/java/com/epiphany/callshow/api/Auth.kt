package com.epiphany.callshow.api

import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory

/**
 * Shared class used by every sample. Contains methods for authorizing a user and caching credentials.
 */
object Auth {

    /**
     * Define a global instance of the HTTP transport.
     */
    val HTTP_TRANSPORT: HttpTransport = NetHttpTransport()

    /**
     * Define a global instance of the JSON factory.
     */
    val JSON_FACTORY: JsonFactory = JacksonFactory()
}