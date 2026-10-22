package com.example.data.remote

import com.example.data.model.LatLng
import kotlin.math.*

object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6371000.0

    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun distanceMeters(p1: LatLng, p2: LatLng): Double {
        return distanceMeters(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
    }

    /**
     * Minimum distance in meters from a point to a polyline
     */
    fun minDistanceToPolyline(point: LatLng, polyline: List<LatLng>): Double {
        if (polyline.isEmpty()) return Double.MAX_VALUE
        if (polyline.size == 1) return distanceMeters(point, polyline[0])

        var minDistance = Double.MAX_VALUE
        for (i in 0 until polyline.size - 1) {
            val dist = distanceToSegment(point, polyline[i], polyline[i + 1])
            if (dist < minDistance) {
                minDistance = dist
            }
        }
        return minDistance
    }

    private fun distanceToSegment(p: LatLng, a: LatLng, b: LatLng): Double {
        val latP = p.latitude
        val lonP = p.longitude
        val latA = a.latitude
        val lonA = a.longitude
        val latB = b.latitude
        val lonB = b.longitude

        val dx = lonB - lonA
        val dy = latB - latA
        if (dx == 0.0 && dy == 0.0) {
            return distanceMeters(p, a)
        }

        val t = max(0.0, min(1.0, ((lonP - lonA) * dx + (latP - latA) * dy) / (dx * dx + dy * dy)))
        val projLat = latA + t * dy
        val projLon = lonA + t * dx
        return distanceMeters(latP, lonP, projLat, projLon)
    }
}
