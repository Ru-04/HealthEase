package com.xyz.healthease

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase

object MongoDBHelper {

    // MongoDB connection string (replace with your MongoDB credentials and IP)
    private const val CONNECTION_STRING =
        "mongodb://admin:passwordd@127.0.0.1:27017/healthease?authSource=admin" // Update this

    // MongoClient object to manage the connection
    private var mongoClient: MongoClient? = null

    // Function to connect to MongoDB and return the database instance
    fun connect(): MongoDatabase {
        // If mongoClient is null, initialize the connection
        if (mongoClient == null) {
            val settings = MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(CONNECTION_STRING))
                .build()
            mongoClient = MongoClients.create(settings)  // Create the client using the connection string
        }
        // Return the specific database instance (replace 'your_database_name' with your actual DB name)
        return mongoClient!!.getDatabase("healthease")
    }

    // Function to disconnect from MongoDB
    fun disconnect() {
        mongoClient?.close() // Close the connection when done
    }
}
