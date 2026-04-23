package com.kntransport.app.network

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    @SerializedName("email")    val email    : String,
    @SerializedName("password") val password : String,
)

data class RegisterRequest(
    @SerializedName("name")     val name     : String,
    @SerializedName("email")    val email    : String,
    @SerializedName("phone")    val phone    : String,
    @SerializedName("password") val password : String,
)

data class AuthResponse(
    @SerializedName("token") val token : String,
    @SerializedName("role")  val role  : String,
    @SerializedName("user")  val user  : UserDto,
)

// ── User ──────────────────────────────────────────────────────────────────────

data class UserDto(
    @SerializedName("id")                    val id                    : String,
    @SerializedName("name")                  val name                  : String,
    @SerializedName("email")                 val email                 : String,
    @SerializedName("phone")                 val phone                 : String,
    @SerializedName("role")                  val role                  : String,
    @SerializedName("avatarUrl")             val avatarUrl             : String? = null,
    @SerializedName("currentVehicleId")      val currentVehicleId      : String? = null,
    @SerializedName("currentVehicleMake")    val currentVehicleMake    : String? = null,
    @SerializedName("currentVehicleModel")   val currentVehicleModel   : String? = null,
    @SerializedName("currentVehicleColour")  val currentVehicleColour  : String? = null,
    @SerializedName("currentVehiclePlate")   val currentVehiclePlate   : String? = null,
    @SerializedName("currentVehicleType")    val currentVehicleType    : String? = null,
    @SerializedName("currentVehiclePhotoUrl")val currentVehiclePhotoUrl: String? = null,
)

data class UpdateProfileRequest(
    @SerializedName("name")  val name  : String,
    @SerializedName("email") val email : String,
    @SerializedName("phone") val phone : String,
)

// ── Trip ──────────────────────────────────────────────────────────────────────

data class TripBookingDto(
    @SerializedName("id")             val id             : String,
    @SerializedName("pickupAddress")  val pickupAddress  : String,
    @SerializedName("dropAddress")    val dropAddress    : String,
    @SerializedName("date")           val date           : String,
    @SerializedName("time")           val time           : String,
    @SerializedName("passengers")     val passengers     : Int,
    @SerializedName("notes")          val notes          : String = "",
    @SerializedName("status")         val status         : String,
    @SerializedName("quotedAmount")   val quotedAmount   : Double? = null,
    @SerializedName("commuterName")   val commuterName   : String? = null,
    @SerializedName("commuterPhone")  val commuterPhone  : String? = null,
    @SerializedName("driverName")     val driverName     : String? = null,
    @SerializedName("driverId")       val driverId       : String? = null,
    @SerializedName("vehicleId")      val vehicleId      : String? = null,
    @SerializedName("vehicleMake")    val vehicleMake    : String? = null,
    @SerializedName("vehicleModel")   val vehicleModel   : String? = null,
    @SerializedName("vehicleColour")  val vehicleColour  : String? = null,
    @SerializedName("vehicleType")    val vehicleType    : String? = null,
    @SerializedName("vehiclePhotoUrl")val vehiclePhotoUrl: String? = null,
    @SerializedName("vehicleInfo")    val vehicleInfo    : String? = null,
    @SerializedName("vehiclePlate")   val vehiclePlate   : String? = null,
    @SerializedName("rating")         val rating         : Int? = null,
)

data class CreateTripRequest(
    @SerializedName("pickupAddress") val pickupAddress : String,
    @SerializedName("dropAddress")   val dropAddress   : String,
    @SerializedName("date")          val date          : String,
    @SerializedName("time")          val time          : String,
    @SerializedName("passengers")    val passengers    : Int,
    @SerializedName("notes")         val notes         : String = "",
)

// ── Lift Club ─────────────────────────────────────────────────────────────────

data class LiftClubDto(
    @SerializedName("id")               val id               : String,
    @SerializedName("title")            val title            : String,
    @SerializedName("pickupArea")       val pickupArea       : String,
    @SerializedName("dropArea")         val dropArea         : String,
    @SerializedName("departureTime")    val departureTime    : String,
    @SerializedName("daysOfWeek")       val daysOfWeek       : List<String>,
    @SerializedName("maxPassengers")    val maxPassengers    : Int,
    @SerializedName("subscriberCount")  val subscriberCount  : Long = 0L,
    @SerializedName("status")           val status           : String,
    @SerializedName("quotedAmount")     val quotedAmount     : Double? = null,
    @SerializedName("paymentCycle")     val paymentCycle     : String? = null,
    @SerializedName("description")      val description      : String = "",
)

data class CreateLiftClubRequest(
    @SerializedName("title")         val title         : String,
    @SerializedName("pickupArea")    val pickupArea    : String,
    @SerializedName("dropArea")      val dropArea      : String,
    @SerializedName("departureTime") val departureTime : String,
    @SerializedName("returnTime")    val returnTime    : String? = null,
    @SerializedName("daysOfWeek")    val daysOfWeek    : List<String>,
    @SerializedName("maxPassengers") val maxPassengers : Int,
    @SerializedName("description")   val description   : String = "",
)

// ── Quote ─────────────────────────────────────────────────────────────────────

data class QuoteDto(
    @SerializedName("id")            val id            : String,
    @SerializedName("referenceId")   val referenceId   : String,
    @SerializedName("referenceType") val referenceType : String,
    @SerializedName("amount")        val amount        : Double,
    @SerializedName("paymentCycle")  val paymentCycle  : String? = null,
    @SerializedName("driverNote")    val driverNote    : String = "",
)

data class QuoteAcceptRequest(
    @SerializedName("accepted")      val accepted      : Boolean,
    @SerializedName("paymentCycle")  val paymentCycle  : String? = null,
)

// ── Notification ──────────────────────────────────────────────────────────────

data class NotificationDto(
    @SerializedName("id")            val id            : String,
    @SerializedName("type")          val type          : String,
    @SerializedName("title")         val title         : String,
    @SerializedName("body")          val body          : String,
    @SerializedName("timestamp")     val timestamp     : String,
    @SerializedName("read")          val read          : Boolean = false,
    @SerializedName("referenceId")   val referenceId   : String? = null,
    @SerializedName("referenceType") val referenceType : String? = null,
)

// ── Vehicle ───────────────────────────────────────────────────────────────────

data class VehicleDto(
    @SerializedName("id")                 val id                 : String,
    @SerializedName("make")               val make               : String,
    @SerializedName("model")              val model              : String,
    @SerializedName("colour")             val colour             : String,
    @SerializedName("plate")              val plate              : String,
    @SerializedName("year")               val year               : Int,
    @SerializedName("vehicleType")        val vehicleType        : String,
    @SerializedName("photoUrl")           val photoUrl           : String? = null,
    @SerializedName("active")             val active             : Boolean = true,
    @SerializedName("notes")              val notes              : String = "",
    @SerializedName("assignedDriverId")   val assignedDriverId   : String? = null,
    @SerializedName("assignedDriverName") val assignedDriverName : String? = null,
)

data class CreateVehicleRequest(
    @SerializedName("make")        val make        : String,
    @SerializedName("model")       val model       : String,
    @SerializedName("colour")      val colour      : String,
    @SerializedName("plate")       val plate       : String,
    @SerializedName("year")        val year        : Int,
    @SerializedName("vehicleType") val vehicleType : String = "MINIBUS",
    @SerializedName("photoUrl")    val photoUrl    : String? = null,
    @SerializedName("notes")       val notes       : String = "",
)

data class AssignVehicleRequest(
    @SerializedName("vehicleId") val vehicleId : String?,
)

data class UpdateTripStatusRequest(
    @SerializedName("status") val status : String,
)

data class DriverEarningsDto(
    @SerializedName("totalEarnings")          val totalEarnings          : Double,
    @SerializedName("completedTrips")         val completedTrips         : Long,
    @SerializedName("confirmedTrips")         val confirmedTrips         : Long,
    @SerializedName("inProgressTrips")        val inProgressTrips        : Long,
    @SerializedName("averageEarningsPerTrip") val averageEarningsPerTrip : Double,
)

// ── Generic wrapper ───────────────────────────────────────────────────────────

data class ApiError(
    @SerializedName("message") val message : String,
    @SerializedName("status")  val status  : Int = 0,
)

data class PagedResponse<T>(
    @SerializedName("content")          val content          : List<T>,
    @SerializedName("totalElements")    val totalElements    : Long,
    @SerializedName("totalPages")       val totalPages       : Int,
    @SerializedName("number")           val number           : Int,
    @SerializedName("last")             val last             : Boolean,
)
