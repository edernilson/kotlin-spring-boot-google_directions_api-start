package com.edernilson.ctr.interfaces.outcoming

import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GMapsService(
    @Value("\${GOOGLE_API_KEY}")
    val googleAppKey: String,
    @Value("\${GOOGLE_HOST:https://maps.googleapis.com}")
    val googleMapsHost: String
) {

    val GMAPS_TEMPLATE: String =
        "$googleMapsHost/maps/api/directions/json?origin={origin}&destination={destination}&key={key}"

    fun getDistanceBetweenAddresses(addressOne: String, addressTwo: String): Int {
        val template = RestTemplate()
        val jsonResult = template.getForObject(GMAPS_TEMPLATE, String::class.java, addressOne, addressTwo, googleAppKey)
//        println("Retorno Directions: \n"+jsonResult)
        val rawResults: JSONArray = JsonPath.parse(jsonResult).read("\$..legs[*].duration.value")
        return rawResults.map { it as Int }.minOrNull() ?: Int.MAX_VALUE
    }
}