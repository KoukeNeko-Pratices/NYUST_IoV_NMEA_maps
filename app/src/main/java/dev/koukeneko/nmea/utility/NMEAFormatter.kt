package dev.koukeneko.nmea.utility

import dev.koukeneko.nmea.data.Location

class NMEAFormatter constructor(
    val nmea: String
) {
    private var latitude: String = ""
    private var longitude: String = ""
    private val nmeaArray = nmea.split(",")

    fun getLatLong(): Location? {
        if (nmeaArray[0] == "\$GNGGA") {

            latitude = if (nmeaArray[3] == "S") {
                (nmeaArray[2].toInt() * -1).toString()
            } else {
                nmeaArray[2]
            }

            longitude = if (nmeaArray[5] == "W") {
                //convert longitude to negative by parsing to Int and then back to String
                (nmeaArray[4].toInt() * -1).toString()
            } else {
                nmeaArray[4]
            }

            latitude =
                latitude.substring(0, 2) + '.' + ((latitude.substring(2).replace(Regex("\\."), "")
                    .toInt() / 60).toString()).replace(".", "")
            longitude =
                longitude.substring(0, 3) + '.' + ((longitude.substring(3).replace(Regex("\\."), "")
                    .toInt() / 60).toString()).replace(".", "")

        }
//        else if (nmeaArray[0] == "\$GNGSA") //多星聯合定位
//        {
//
//        }
        //check if latitude and longitude are empty
        if (latitude == "" || longitude == "") {
            return null
        }
        return Location(latitude.toDouble(), longitude.toDouble())

    }


}