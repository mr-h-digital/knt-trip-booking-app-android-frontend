package com.kntransport.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    KntScaffold(title = "Privacy Policy", onBack = onBack) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LegalLastUpdated("1 May 2026")
            LegalBody(
                "K&T Transport (\"K&T\", \"we\", \"us\") is committed to protecting your personal " +
                "information in accordance with the Protection of Personal Information Act, 2013 " +
                "(\"POPIA\"). This Privacy Policy explains what information we collect, why we " +
                "collect it, and how we use and protect it."
            )
            LegalSection("1. Information We Collect") {
                LegalSubSection("1.1  Information you provide to us") {
                    LegalBullet("Full name")
                    LegalBullet("Email address")
                    LegalBullet("South African phone number")
                    LegalBullet("Password (stored in hashed form — never readable by us)")
                    LegalBullet("Profile photo (optional)")
                }
                LegalSubSection("1.2  Information generated when you use the App") {
                    LegalBullet("Trip pickup and drop-off addresses")
                    LegalBullet("Trip dates, times, and passenger counts")
                    LegalBullet("GPS location (drivers only, while a trip is in progress and location sharing is enabled)")
                    LegalBullet("Trip history and status updates")
                    LegalBullet("Quotes submitted or received")
                    LegalBullet("Ratings and reviews")
                }
                LegalSubSection("1.3  Vehicle information (drivers only)") {
                    LegalBullet("Vehicle make, model, colour, registration plate")
                    LegalBullet("Vehicle photo (optional)")
                }
            }
            LegalSection("2. Why We Collect This Information") {
                LegalBody("We use your personal information to:")
                LegalBullet("Create and manage your account.")
                LegalBullet("Match commuters with available drivers.")
                LegalBullet("Facilitate trip booking, quoting, and confirmation.")
                LegalBullet("Display live driver location to commuters during an active trip.")
                LegalBullet("Send you notifications about your bookings.")
                LegalBullet("Improve the App and resolve technical issues.")
                LegalBullet("Comply with applicable legal obligations.")
            }
            LegalSection("3. Legal Basis for Processing (POPIA)") {
                LegalBody(
                    "We process your personal information on the following grounds as permitted " +
                    "by POPIA:"
                )
                LegalBullet("Performance of a contract — to provide the transport booking service you requested.")
                LegalBullet("Consent — for optional features such as profile photos.")
                LegalBullet("Legitimate interest — to maintain security, prevent fraud, and improve the platform.")
                LegalBullet("Legal obligation — where required by South African law.")
            }
            LegalSection("4. GPS Location") {
                LegalBody(
                    "GPS location data is collected from drivers only, and only while a trip is " +
                    "marked as In Progress and the driver has explicitly enabled location sharing " +
                    "via the toggle in the app. Location data is used solely to display the " +
                    "driver's position on the commuter's map and is not stored permanently."
                )
                LegalBody(
                    "We do not collect location data from commuters."
                )
            }
            LegalSection("5. How We Store Your Information") {
                LegalBody(
                    "Your data is stored on secure servers hosted by Railway (cloud infrastructure " +
                    "provider). Profile photos are stored on Cloudflare R2 object storage. " +
                    "All data is transmitted over encrypted HTTPS connections."
                )
                LegalBody(
                    "Passwords are hashed using BCrypt before storage and are never stored or " +
                    "transmitted in plain text."
                )
            }
            LegalSection("6. How Long We Keep Your Information") {
                LegalBody(
                    "We retain your personal information for as long as your account is active. " +
                    "Trip and transaction records are retained for a minimum of 5 years for " +
                    "legal and accounting purposes. You may request deletion of your account " +
                    "and associated personal data at any time (see Section 8)."
                )
            }
            LegalSection("7. Sharing Your Information") {
                LegalBody("We do not sell your personal information to third parties.")
                LegalBody("We may share limited information:")
                LegalBullet(
                    "Between commuters and drivers — your name, phone number, and trip details " +
                    "are shared with the matched driver/commuter to facilitate the trip."
                )
                LegalBullet(
                    "With service providers — cloud hosting and storage providers who process " +
                    "data on our behalf under appropriate data processing agreements."
                )
                LegalBullet(
                    "Where required by law — if compelled by a court order or regulatory authority."
                )
            }
            LegalSection("8. Your Rights Under POPIA") {
                LegalBody("You have the right to:")
                LegalBullet("Access the personal information we hold about you.")
                LegalBullet("Request correction of inaccurate or incomplete information.")
                LegalBullet("Request deletion of your personal information (subject to legal retention requirements).")
                LegalBullet("Object to the processing of your personal information.")
                LegalBullet("Lodge a complaint with the Information Regulator of South Africa.")
                LegalBody(
                    "To exercise any of these rights, contact us at info@mrhdigital.co.za. " +
                    "We will respond within 30 days."
                )
            }
            LegalSection("9. Information Regulator") {
                LegalBody(
                    "If you believe we have not handled your personal information in accordance " +
                    "with POPIA, you may contact the Information Regulator of South Africa:"
                )
                LegalBody("Website: www.inforegulator.org.za")
                LegalBody("Email: inforeg@justice.gov.za")
            }
            LegalSection("10. Changes to This Policy") {
                LegalBody(
                    "We may update this Privacy Policy from time to time. We will notify you of " +
                    "any material changes through the App. Continued use of the App after changes " +
                    "are published constitutes acceptance of the updated policy."
                )
            }
            LegalSection("11. Contact Us") {
                LegalBody("For any privacy-related questions or requests:")
                LegalBody("Email: info@mrhdigital.co.za")
                LegalBody("WhatsApp: +27 78 778 4182")
                LegalBody("K&T Transport, Beacon Valley, Mitchell's Plain, Cape Town")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
