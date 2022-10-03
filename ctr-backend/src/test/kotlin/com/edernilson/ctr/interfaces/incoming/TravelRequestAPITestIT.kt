package com.edernilson.ctr.interfaces.incoming

import com.edernilson.ctr.infrastructure.loadFileContents
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.restassured.RestAssured
import io.restassured.RestAssured.basic
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles

import org.hamcrest.Matchers.equalTo as equalToHamcrest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = WireMockConfiguration.DYNAMIC_PORT)
@ActiveProfiles("test")
class TravelRequestAPITestIT {

    private val MOTORISTA_ADDRESS: String = "Rua General Piragibe, 485, Parquelândia, Fortaleza - CE"
    private val ROUTE_START: String = "Avenida Bezerra de Menezes, 1465, São Gerardo, Fortaleza - CE"
    private val ROUTE_END: String = "Avenida Jovita Feitosa, 1095, Parquelândia, Fortaleza - CE"

    @Autowired
    lateinit var server: WireMockServer

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        RestAssured.baseURI = "https://localhost:$port"
        RestAssured.useRelaxedHTTPSValidation()
        RestAssured.authentication = basic("admin","password")
    }

    fun setupServer() {
        var stubFor = server.stubFor(
            get(urlPathEqualTo("/maps/api/directions/json"))
                .withQueryParam("origin", equalTo(MOTORISTA_ADDRESS))
                .withQueryParam("destination", equalTo(ROUTE_START))
                .withQueryParam("key", equalTo("APIKEY"))
                .willReturn(okJson(loadFileContents("/responses/gmaps/sample_response.json")))
        )
    }

    @Test
    fun testFindNearbyTravelRequests() {
        setupServer()
        val passengerId =
            given()
                .contentType(ContentType.JSON)
                .body(loadFileContents("/requests/passengers_api/create_new_passenger.json"))
                .post("/passengers")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalToHamcrest("Eder Nilson"))
                .extract()
                .body()
                .jsonPath().getString("id")

        val data = mapOf<String, String>(
            "passengerId" to passengerId
        )

        val travelRequestId =
            given()
                .contentType(ContentType.JSON)
                .body(loadFileContents("/requests/travel_requests_api/create_new_request.json", data))
                .post("/travelRequest")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("origin", equalToHamcrest(ROUTE_START))
                .body("destination", equalToHamcrest(ROUTE_END))
                .body("status", equalToHamcrest("CREATED"))
                .body("_links.passenger.title", equalToHamcrest("Eder Nilson"))
                .extract()
                .jsonPath().getInt("id")

        given()
            .get("/travelRequest/nearby?currentAddress=$MOTORISTA_ADDRESS")
            .then()
            .statusCode(200)
            .body("[0].id", equalToHamcrest(travelRequestId))
            .body("[0].origin", equalToHamcrest(ROUTE_START))
            .body("[0].destination", equalToHamcrest(ROUTE_END))
            .body("[0].status", equalToHamcrest("CREATED"))
    }

}