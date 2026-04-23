package com.kntransport.app.repository

import com.kntransport.app.network.*

class LiftClubRepository {

    private val api by lazy { ApiClient.service }

    suspend fun getLiftClubs(page: Int = 0) =
        safeApiCall { api.getLiftClubs(page) }

    suspend fun getLiftClub(id: String) =
        safeApiCall { api.getLiftClub(id) }

    suspend fun createLiftClub(
        title        : String,
        pickupArea   : String,
        dropArea     : String,
        departureTime: String,
        returnTime   : String?,
        daysOfWeek   : List<String>,
        maxPassengers: Int,
        description  : String,
    ) = safeApiCall {
        api.createLiftClub(
            CreateLiftClubRequest(
                title, pickupArea, dropArea, departureTime,
                returnTime.takeIf { !it.isNullOrBlank() },
                daysOfWeek, maxPassengers, description,
            )
        )
    }

    suspend fun subscribe(id: String) =
        safeApiCall { api.subscribeToLiftClub(id) }
}
