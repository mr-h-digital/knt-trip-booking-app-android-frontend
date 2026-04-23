package com.kntransport.app.data

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.LocalTime

// ── User ──────────────────────────────────────────────────────────────────────
enum class UserRole { COMMUTER, DRIVER, ADMIN }

data class User(
    val id        : String,
    val name      : String,
    val email     : String,
    val phone     : String,
    val role      : UserRole = UserRole.COMMUTER,
    val avatarUri : Uri? = null,
)

// ── Trip Booking ──────────────────────────────────────────────────────────────
enum class TripStatus {
    PENDING_QUOTE, QUOTE_SENT, QUOTE_ACCEPTED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
}

data class TripBooking(
    val id           : String,
    val commuterId   : String,
    val commuterName : String,
    val pickupAddress: String,
    val dropAddress  : String,
    val date         : LocalDate,
    val time         : LocalTime,
    val passengers   : Int = 1,
    val notes        : String = "",
    val status       : TripStatus = TripStatus.PENDING_QUOTE,
    val quotedAmount : Double? = null,
    val driverName   : String? = null,
    val vehicleInfo  : String? = null,
    val vehiclePlate : String? = null,
)

// ── Lift Club ─────────────────────────────────────────────────────────────────
enum class LiftClubStatus {
    OPEN, QUOTA_MET, QUOTE_SENT, ACTIVE, COMPLETED, CANCELLED
}

enum class PaymentCycle { MONTHLY, WEEKLY, FORTNIGHTLY }

data class LiftClub(
    val id              : String,
    val creatorId       : String,
    val creatorName     : String,
    val title           : String,
    val pickupArea      : String,
    val dropArea        : String,
    val departureTime   : LocalTime,
    val returnTime      : LocalTime? = null,
    val daysOfWeek      : List<String>,
    val maxPassengers   : Int,
    val subscriberCount : Int = 0,
    val status          : LiftClubStatus = LiftClubStatus.OPEN,
    val quotedAmount    : Double? = null,
    val paymentCycle    : PaymentCycle? = null,
    val driverName      : String? = null,
    val vehicleInfo     : String? = null,
    val description     : String = "",
)

data class LiftClubSubscription(
    val id         : String,
    val liftClubId : String,
    val userId     : String,
    val userName   : String,
    val quoteAccepted: Boolean? = null,
)

// ── Quote ─────────────────────────────────────────────────────────────────────
data class Quote(
    val id           : String,
    val referenceId  : String,
    val referenceType: String,           // "TRIP" or "LIFT_CLUB"
    val amount       : Double,
    val paymentCycle : PaymentCycle? = null,
    val paymentOptions: List<PaymentCycle> = emptyList(),
    val driverNote   : String = "",
    val accepted     : Boolean? = null,
)

// ── Notification ──────────────────────────────────────────────────────────────
enum class NotifType { QUOTE_RECEIVED, TRIP_CONFIRMED, LIFT_CLUB_UPDATE, GENERAL }

data class AppNotification(
    val id            : String,
    val type          : NotifType,
    val title         : String,
    val body          : String,
    val timestamp     : String,
    val read          : Boolean = false,
    val referenceId   : String? = null,
    val referenceType : String? = null,  // "TRIP", "LIFT_CLUB", "QUOTE"
)

// ── Sample data ───────────────────────────────────────────────────────────────
object SampleData {

    var currentUser by mutableStateOf(
        User(
            id    = "u1",
            name  = "Tayla Hendricks",
            email = "tayla@email.com",
            phone = "072 345 6789",
            role  = UserRole.COMMUTER,
        )
    )

    val driverUser = User(
        id    = "d1",
        name  = "Taswill Heynes",
        email = "taswill@ktransport.co.za",
        phone = "+27787784182",
        role  = UserRole.DRIVER,
    )

    val adminUser = User(
        id    = "a1",
        name  = "Admin User",
        email = "admin@ktransport.co.za",
        phone = "+27211234567",
        role  = UserRole.ADMIN,
    )

    val myTrips = listOf(
        TripBooking(
            id = "t1", commuterId = "u1", commuterName = "Tayla Hendricks",
            pickupAddress = "14 Sunrise Ave, Beacon Valley",
            dropAddress   = "Cape Town CBD, Adderley St",
            date = LocalDate.now().plusDays(1),
            time = LocalTime.of(7, 30),
            passengers = 1,
            status = TripStatus.QUOTE_SENT,
            quotedAmount = 180.0,
        ),
        TripBooking(
            id = "t2", commuterId = "u1", commuterName = "Tayla Hendricks",
            pickupAddress = "14 Sunrise Ave, Beacon Valley",
            dropAddress   = "Mitchells Plain Town Centre",
            date = LocalDate.now().minusDays(3),
            time = LocalTime.of(8, 0),
            passengers = 1,
            status = TripStatus.COMPLETED,
            quotedAmount = 95.0,
            driverName   = "Taswill Heynes",
            vehicleInfo  = "Toyota HiAce — White",
            vehiclePlate = "CA 456 789",
        ),
        TripBooking(
            id = "t3", commuterId = "u1", commuterName = "Tayla Hendricks",
            pickupAddress = "14 Sunrise Ave, Beacon Valley",
            dropAddress   = "Bellville Station",
            date = LocalDate.now().plusDays(2),
            time = LocalTime.of(6, 45),
            passengers = 2,
            status = TripStatus.PENDING_QUOTE,
        ),
    )

    val liftClubs = listOf(
        LiftClub(
            id = "lc1", creatorId = "u2", creatorName = "Nadia Adams",
            title = "Beacon Valley → Cape Town CBD",
            pickupArea = "Beacon Valley, Mitchell's Plain",
            dropArea   = "Cape Town CBD",
            departureTime = LocalTime.of(6, 30),
            returnTime    = LocalTime.of(17, 30),
            daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            maxPassengers = 6, subscriberCount = 4,
            status = LiftClubStatus.OPEN,
            description = "Daily commute to the CBD. Relaxed vibe, music allowed. Drop at Adderley or Company's Garden.",
        ),
        LiftClub(
            id = "lc2", creatorId = "u3", creatorName = "Yusuf Daniels",
            title = "Tafelsig → Bellville Station",
            pickupArea = "Tafelsig, Mitchell's Plain",
            dropArea   = "Bellville Station",
            departureTime = LocalTime.of(7, 0),
            daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            maxPassengers = 8, subscriberCount = 8,
            status = LiftClubStatus.QUOTA_MET,
            quotedAmount = 650.0,
            paymentCycle = PaymentCycle.MONTHLY,
            description = "Quota met! Awaiting driver quote confirmation. Shared lift to Bellville for train connections.",
        ),
        LiftClub(
            id = "lc3", creatorId = "u4", creatorName = "Fatima Jacobs",
            title = "Westridge → Athlone",
            pickupArea = "Westridge, Mitchell's Plain",
            dropArea   = "Athlone",
            departureTime = LocalTime.of(6, 0),
            returnTime    = LocalTime.of(18, 0),
            daysOfWeek = listOf("Mon", "Wed", "Fri"),
            maxPassengers = 5, subscriberCount = 2,
            status = LiftClubStatus.OPEN,
            description = "3 days/week commute. Looking for reliable commuters from Westridge area.",
        ),
        LiftClub(
            id = "lc4", creatorId = "u5", creatorName = "Bradley September",
            title = "Lentegeur → Somerset West",
            pickupArea = "Lentegeur, Mitchell's Plain",
            dropArea   = "Somerset West Industrial",
            departureTime = LocalTime.of(5, 30),
            daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            maxPassengers = 10, subscriberCount = 7,
            status = LiftClubStatus.ACTIVE,
            quotedAmount = 480.0,
            paymentCycle = PaymentCycle.FORTNIGHTLY,
            driverName   = "Taswill Heynes",
            vehicleInfo  = "Toyota HiAce — White",
            description = "Active lift club. Fortnightly billing via EFT or cash.",
        ),
    )

    val myLiftClubSubscriptions = listOf("lc1", "lc4")

    val notifications = listOf(
        AppNotification("n1", NotifType.QUOTE_RECEIVED,  "Quote Received",         "Your trip to Cape Town CBD has a quote of R180.00. Tap to review.", "2 min ago"),
        AppNotification("n2", NotifType.LIFT_CLUB_UPDATE,"Lift Club Quota Met",    "Beacon Valley → CBD lift club is full. Driver quote incoming.",    "1 hr ago"),
        AppNotification("n3", NotifType.TRIP_CONFIRMED,  "Trip Confirmed",         "Your trip on Mon 14 Apr is confirmed. Driver: Taswill Heynes.",    "Yesterday"),
        AppNotification("n4", NotifType.GENERAL,         "Welcome to K&T Transport","Book a trip or join a lift club to get started.",                 "2 days ago", read = true),
    )
}
