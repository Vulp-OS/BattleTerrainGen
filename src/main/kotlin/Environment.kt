package org.rockwood

import kotlin.properties.Delegates

//<editor-fold desc="Min/Max allowed values for reasonable environmental data">
const val MAX_AVG_TEMP = 36         // Maximum allowed average annual temperature in Celsius
const val MIN_AVG_TEMP = -65        // Minimum allowed average annual temperature in Celsius
const val MAX_AVG_TEMP_MONTH = 55   // Maximum allowed average monthly temperature in Celsius
const val MIN_AVG_TEMP_MONTH = -80  // Minimum allowed average monthly temperature in Celsius
const val MIN_AVG_PRECIP = 0        // Minimum allowed average annual precipitation in Millimeters
const val MAX_AVG_PRECIP = 0        // Maximum allowed average annual precipitation in Millimeters
const val MIN_AVG_PRECIP_MONTH = 0  // Minimum allowed average monthly precipitation in Millimeters
const val MAX_AVG_PRECIP_MONTH = 15000  // Maximum allowed average monthly precipitation in Millimeters
//</editor-fold>

// Environmental classifications are based on the Köppen climate classification
// NOTE: I have not matched these classifications 1:1
class Environment() {
    enum class PrimaryEnvs {
        Tropical,
        Dry,
        Temperate,
        Continental,
        Polar
    }
    enum class TropicalSubEnvs {
        Rainforest,
        Monsoon,
        Savanna
    }
    enum class DrySubEnvs {
        Desert,
        Steppe
    }
    enum class PolarSubEnvs {
        Tundra,
        EternalFrost
    }
    enum class DrySeasons {
        NoDrySeason,    // Average precipitation doesn't change much throughout the seasons, but not close to 0
        DryWinter,
        DrySummer,
        AlwaysDry       // Very low average precipitation
    }

    // Declaration and Initialization of Properties
    var primaryEnv: PrimaryEnvs = PrimaryEnvs.Tropical
    private var _tropicalSubEnv: TropicalSubEnvs? = TropicalSubEnvs.Rainforest
    private var _drySubEnv: DrySubEnvs? = null
    private var _polarSubEnv: PolarSubEnvs? = null
    var tropicalSubEnv: TropicalSubEnvs?
        get() = _tropicalSubEnv
        set(env) {
            primaryEnv = PrimaryEnvs.Tropical
            _tropicalSubEnv = env
            _drySubEnv = null
            _polarSubEnv = null
        }
    var drySubEnv: DrySubEnvs?
        get() = _drySubEnv
        set(env) {
            primaryEnv = PrimaryEnvs.Dry
            _tropicalSubEnv = null
            _drySubEnv = env
            _polarSubEnv = null
        }
    var polarSubEnv: PolarSubEnvs?
        get() = _polarSubEnv
        set(env) {
            primaryEnv = PrimaryEnvs.Polar
            _tropicalSubEnv = null
            _drySubEnv = null
            _polarSubEnv = env
        }
    var drySeason: DrySeasons = DrySeasons.NoDrySeason

    // Configure these properties to dynamically update our Climate designation when changed
    // These are set to standard starting values that intentionally don't match the Env values
    //  configured above. This allows us to confirm that the init function is working and that
    //  the setClimate() function is working as expected.
    var avgTemp:Int by Delegates.observable(11) { _, _, _ ->
        setClimate()
    }        // Average Annual Temp in Celsius
    var avgTempCM:Int by Delegates.observable(-1) { _, _, _ ->
        setClimate()
    }       // Average Temp for Coldest Month
    var avgTempWM:Int by Delegates.observable(26) { _, _, _ ->
        setClimate()
    }      // Average Temp for Warmest Month
    var avgPrecip:Int by Delegates.observable(461) { _, _, _ ->
        setClimate()
    }     // Average Annual Precipitation in mm
    var avgPrecipDM:Int by Delegates.observable(8) { _, _, _ ->
        setClimate()
    }    // Average Precipitation for Driest Month in mm

    init {
        setClimate()
    }

    override fun toString(): String {
        return listOfNotNull(primaryEnv, tropicalSubEnv, drySubEnv, polarSubEnv, drySeason).joinToString(" ")
    }

    private fun setClimate() {      // Sets Climate variables based off of user-configured variables
        // Evaluate Primary Environment Designation
        // Determine if Tropical
        if (avgTempCM >= 18 && avgPrecip >= 1000) {
            primaryEnv = PrimaryEnvs.Tropical
        }
        // Determine if Dry
        if (avgPrecip < ((avgTemp * 20)+280)) {
            primaryEnv = PrimaryEnvs.Dry
        }
        // Determine if Polar
        if (avgTempWM < 10) {
            primaryEnv = PrimaryEnvs.Polar
        }
        // Determine if Temperate
        if (avgTempCM in 1..17 && avgTempWM > 10 && (avgPrecip > (avgTemp * 20)+280)) {
            primaryEnv = PrimaryEnvs.Temperate
        }
        // Determine if Continental
        if (avgTempCM < 0 && avgTempWM > 10 && (avgPrecip > (avgTemp * 20)+280)) {
            primaryEnv = PrimaryEnvs.Continental
        }

        // Evaluate Sub-Environment
        when (primaryEnv) {
            PrimaryEnvs.Tropical -> {
                tropicalSubEnv = when {
                    avgPrecipDM >= 60 -> TropicalSubEnvs.Rainforest
                    avgPrecipDM >= 100-(avgPrecip/25) && avgPrecipDM < 60 -> TropicalSubEnvs.Monsoon
                    else -> TropicalSubEnvs.Savanna
                }
            }

            PrimaryEnvs.Dry -> {
                drySubEnv = if (
                    (drySeason == DrySeasons.DryWinter && avgPrecip <= ((avgTemp*20)+280)/2) ||
                    (drySeason == DrySeasons.AlwaysDry || drySeason == DrySeasons.NoDrySeason) && avgPrecip <= ((avgTemp*20)+140)/2 ||
                    (drySeason == DrySeasons.DrySummer && avgPrecip <= (avgTemp*20)/2)) {
                    DrySubEnvs.Desert
                } else
                    DrySubEnvs.Steppe

                if (drySeason == DrySeasons.NoDrySeason) {
                    drySeason = DrySeasons.AlwaysDry
                }
            }

            PrimaryEnvs.Polar -> {
                polarSubEnv = if (avgTempWM > 0)
                    PolarSubEnvs.Tundra
                else
                    PolarSubEnvs.EternalFrost
            }
            else -> {
                _tropicalSubEnv = null
                _drySubEnv = null
                _polarSubEnv = null
            }
        }
    }
}