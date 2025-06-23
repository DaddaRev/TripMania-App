package com.example.travelapplicationv5

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import java.util.Locale

object UtilityMaps {

    fun getLatLngFromLocationName(
        context: Context,
        locationName: String,
        locale: Locale = Locale.getDefault()
    ): Pair<Double, Double>? {
        val geocoder = Geocoder(context, locale)
        return try {
            val results = geocoder.getFromLocationName(locationName, 1)
            if (!results.isNullOrEmpty()) {
                val firstResult = results[0]
                Pair(firstResult.latitude, firstResult.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun openTripItinerary(context: Context, stops: List<Stop>) {
        val validStops = stops.filter { it.latitude != null && it.longitude != null }

        if (validStops.size < 2) {
            Toast.makeText(context, "At least two stops with coordinates are required", Toast.LENGTH_SHORT).show()
            return
        }

        val uriBuilder = StringBuilder("https://www.google.com/maps/dir/")

        validStops.forEach { stop ->
            uriBuilder.append("${stop.latitude},${stop.longitude}/")
        }

        val uri = Uri.parse(uriBuilder.toString())
        val intent = Intent(Intent.ACTION_VIEW, uri)
        // intent.setPackage("com.google.android.apps.maps")

        try {
            context.startActivity(intent)
        } catch (ex: Exception) {
            Toast.makeText(context, "Impossible showing maps at the moment", Toast.LENGTH_SHORT).show()
        }

        /*
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Impossible showing maps at the moment", Toast.LENGTH_SHORT).show()
        }
        */
    }
}