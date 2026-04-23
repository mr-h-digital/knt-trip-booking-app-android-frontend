package com.kntransport.app.repository

import com.kntransport.app.network.*

class TripRepository {

    private val api by lazy { ApiClient.service }

    suspend fun getMyTrips(page: Int = 0) =
        safeApiCall { api.getMyTrips(page) }

    suspend fun getTrip(id: String) =
        safeApiCall { api.getTrip(id) }

    suspend fun createTrip(
        pickup    : String,
        dropoff   : String,
        date      : String,
        time      : String,
        passengers: Int,
        notes     : String = "",
    ) = safeApiCall {
        api.createTrip(CreateTripRequest(pickup, dropoff, date, time, passengers, notes))
    }
}
