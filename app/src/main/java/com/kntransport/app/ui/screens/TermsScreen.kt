package com.kntransport.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun TermsScreen(onBack: () -> Unit) {
    KntScaffold(title = "Terms of Service", onBack = onBack) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LegalLastUpdated("1 May 2026")
            LegalSection("1. About K&T Transport") {
                LegalBody(
                    "K&T Transport (\"K&T\", \"we\", \"us\") is a private transport booking platform " +
                    "operating in Beacon Valley and Mitchell's Plain, Cape Town, South Africa. " +
                    "These Terms of Service (\"Terms\") govern your use of the K&T Transport mobile " +
                    "application (\"App\") and all associated services."
                )
                LegalBody(
                    "By creating an account or using the App you confirm that you have read, " +
                    "understood, and agree to be bound by these Terms. If you do not agree, " +
                    "please do not use the App."
                )
            }
            LegalSection("2. Eligibility") {
                LegalBody("You must be at least 18 years of age to create an account and use the App.")
                LegalBody(
                    "By registering you warrant that the information you provide is accurate, " +
                    "complete, and kept up to date."
                )
            }
            LegalSection("3. The Service") {
                LegalBody(
                    "The App allows commuters to request transport (\"Trip\") and allows drivers " +
                    "to submit quotes for those trips. K&T Transport acts as an intermediary platform " +
                    "facilitating the connection between commuters and drivers."
                )
                LegalBody(
                    "A booking is only confirmed once the commuter has accepted a driver's quote " +
                    "and the driver has been notified. K&T Transport does not guarantee the " +
                    "availability of drivers at any given time."
                )
            }
            LegalSection("4. Bookings and Cancellations") {
                LegalBody("4.1  Trip requests must be submitted at least 2 hours before the desired pickup time.")
                LegalBody("4.2  Trip requests cannot be made for dates in the past.")
                LegalBody(
                    "4.3  Commuters may cancel a confirmed booking at any time before the trip " +
                    "starts. Repeated cancellations may result in account suspension."
                )
                LegalBody(
                    "4.4  Drivers may cancel a confirmed booking only in exceptional circumstances " +
                    "(vehicle breakdown, medical emergency). Repeated driver cancellations may result " +
                    "in removal from the platform."
                )
                LegalBody(
                    "4.5  Trip requests that have not been quoted and confirmed by the travel date " +
                    "will be automatically cancelled by the system."
                )
            }
            LegalSection("5. Pricing and Quotes") {
                LegalBody(
                    "Drivers submit their own quotes for each trip. The quoted price is the " +
                    "final price agreed between the commuter and the driver. K&T Transport does " +
                    "not set or guarantee any specific price."
                )
                LegalBody(
                    "Payment arrangements are made directly between the commuter and driver. " +
                    "K&T Transport is not responsible for any payment disputes."
                )
            }
            LegalSection("6. Conduct") {
                LegalBody("Users of the App agree to:")
                LegalBullet("Treat all other users (drivers and commuters) with respect and dignity.")
                LegalBullet("Not engage in fraudulent, abusive, or unlawful conduct.")
                LegalBullet("Not use the App for any purpose other than legitimate transport bookings.")
                LegalBullet("Comply with all applicable South African laws and road traffic regulations.")
                LegalBody(
                    "K&T Transport reserves the right to suspend or permanently remove any user " +
                    "who violates these conduct standards."
                )
            }
            LegalSection("7. Driver Responsibilities") {
                LegalBody("Drivers registered on the App warrant that they:")
                LegalBullet("Hold a valid South African driver's licence appropriate for the vehicle operated.")
                LegalBullet("Operate a roadworthy and adequately insured vehicle at all times.")
                LegalBullet("Comply with all applicable transport regulations and by-laws.")
                LegalBullet("Maintain professional conduct toward commuters at all times.")
                LegalBody(
                    "Drivers operate as independent contractors and are solely responsible for " +
                    "their own tax obligations, insurance, and compliance with labour law."
                )
            }
            LegalSection("8. Liability") {
                LegalBody(
                    "K&T Transport provides a platform service only. We are not a transport " +
                    "operator and are not a party to the transport contract between commuter and driver."
                )
                LegalBody(
                    "To the maximum extent permitted by South African law, K&T Transport shall " +
                    "not be liable for any loss, injury, damage, or claim arising from or related " +
                    "to the transport services provided by drivers through the platform."
                )
                LegalBody(
                    "K&T Transport's total liability to any user, for any reason, shall not " +
                    "exceed the amount paid by that user to K&T Transport in the preceding " +
                    "30 days, or R500, whichever is greater."
                )
            }
            LegalSection("9. Intellectual Property") {
                LegalBody(
                    "All content, design, trademarks, and software in the App are the property " +
                    "of K&T Transport or its licensors. You may not copy, reproduce, or distribute " +
                    "any part of the App without our prior written consent."
                )
            }
            LegalSection("10. Amendments") {
                LegalBody(
                    "We may update these Terms from time to time. Continued use of the App after " +
                    "changes are published constitutes acceptance of the revised Terms. We will " +
                    "notify users of material changes via the App."
                )
            }
            LegalSection("11. Governing Law") {
                LegalBody(
                    "These Terms are governed by the laws of the Republic of South Africa. " +
                    "Any disputes shall be subject to the jurisdiction of the South African courts."
                )
            }
            LegalSection("12. Contact") {
                LegalBody("For questions about these Terms, contact us at:")
                LegalBody("Email: info@mrhdigital.co.za")
                LegalBody("WhatsApp: +27 78 778 4182")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
