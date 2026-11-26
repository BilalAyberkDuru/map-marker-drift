import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}

// Kişisel konumlar git'e girmeyen local.places.properties dosyasından okunur (README'deki kural).
val localPlaces = Properties().apply {
    val f = rootProject.file("local.places.properties")
    if (f.exists()) f.reader(Charsets.UTF_8).use { load(it) }
}

fun placeField(key: String): String {
    val v = localPlaces.getProperty(key, "").trim().replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$v\""
}

android {
    namespace = "com.example.markerdrift"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.markerdrift"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        listOf("A", "B").forEach { p ->
            listOf("NAME", "LAT", "LNG").forEach { k ->
                buildConfigField("String", "PLACE_${p}_$k", placeField("PLACE_${p}_$k"))
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
