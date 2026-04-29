package com.kntransport.app.repository

import com.kntransport.app.network.*

class TripRepository {

    private val api by lazy { ApiClient.service }

    // ── Commuter ──────────────────────────────────────────────────────────────

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

    suspend fun cancelTrip(id: String, reason: String, note: String = "") =
        safeApiCall { api.cancelTrip(id, CancelTripRequest(reason, note)) }

    suspend fun rateTrip(id: String, rating: Int, comment: String = "") =
        safeApiCall { api.rateTrip(id, RateTripRequest(rating, comment)) }

    suspend fun respondToQuote(id: String, accepted: Boolean, paymentCycle: String? = null) =
        safeApiCall { api.respondToQuote(id, QuoteAcceptRequest(accepted, paymentCycle)) }

    // ── Driver ────────────────────────────────────────────────────────────────

    suspend fun getDriverTrips(page: Int = 0) =
        safeApiCall { api.getDriverTrips(page) }

    suspend fun getDriverTrip(id: String) =
        safeApiCall { api.getDriverTrip(id) }

    suspend fun updateTripStatus(id: String, status: String) =
        safeApiCall { api.updateTripStatus(id, UpdateTripStatusRequest(status)) }

    suspend fun cancelDriverTrip(id: String, reason: String, note: String = "") =
        safeApiCall { api.cancelDriverTrip(id, CancelTripRequest(reason, note)) }

    suspend fun getDriverEarnings() =
        safeApiCall { api.getDriverEarnings() }

    suspend fun getAvailableTrips(page: Int = 0) =
        safeApiCall { api.getAvailableTrips(page) }

    suspend fun createDriverQuote(tripId: String, amount: Double, note: String) =
        safeApiCall { api.createDriverQuote(tripId, DriverQuoteRequest(amount, note)) }

    suspend fun editDriverQuote(quoteId: String, amount: Double, note: String) =
        safeApiCall { api.editDriverQuote(quoteId, DriverQuoteRequest(amount, note)) }

    suspend fun cancelDriverQuote(quoteId: String) =
        safeApiCall { api.cancelDriverQuote(quoteId) }

    // ── Live tracking ─────────────────────────────────────────────────────────

    suspend fun getTripLocation(tripId: String) =
        safeApiCall { api.getTripLocation(tripId) }

    suspend fun updateDriverLocation(tripId: String, latitude: Double, longitude: Double) =
        safeApiCall { api.updateDriverLocation(tripId, UpdateDriverLocationRequest(latitude, longitude)) }
}
