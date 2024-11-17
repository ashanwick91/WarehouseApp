package com.example.warehouseapp.util

import android.content.Context
import android.widget.ImageView
import com.example.warehouseapp.R
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.BufferedReader
import java.io.InputStreamReader

fun readBaseUrl(context: Context): String {
    val inputStream = context.assets.open("base_url.txt")
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.readLine()
}

fun loadImageFromFirebase(storagePath: String, imageView: ImageView) {
    // Reference to the Firebase Storage location
    val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)

    // Get the download URL
    storageRef.downloadUrl.addOnSuccessListener { uri ->
        // Load the image using Picasso
        Picasso.get()
            .load(uri)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .into(imageView);

    }.addOnFailureListener {
        // Handle any errors
        imageView.setImageResource(R.drawable.error) // Optional: error image
    }
}