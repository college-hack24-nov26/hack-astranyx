package com.sktech.wastetrack.util

object Constants {
    const val DEFAULT_FACTORY_ID = "ambad-midc-pilot-001"
    const val DEFAULT_FACTORY_NAME = "Ambad MIDC Pilot"

    const val SYNC_INTERVAL_MINUTES = 15L
    const val SYNC_BATCH_SIZE = 50
    const val MAX_RETRY_COUNT = 5

    const val BIN_WARNING_THRESHOLD = 60f
    const val BIN_CRITICAL_THRESHOLD = 85f
    const val BIN_DISPATCH_THRESHOLD = 90f

    const val ANOMALY_SCORE_THRESHOLD = 0.7f
    const val WEIGHT_VOLUME_SIGMA = 2.0f

    const val ML_CONFIDENCE_THRESHOLD = 0.6f

    const val QR_EXPIRY_MINUTES = 30L

    const val AUCTION_DURATION_HOURS = 24L

    const val PREFERENCES_NAME = "wastetrack_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_FACTORY_ID = "factory_id"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_LANGUAGE = "language"

}
