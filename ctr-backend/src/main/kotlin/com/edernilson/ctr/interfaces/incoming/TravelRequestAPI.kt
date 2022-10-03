package com.edernilson.ctr.interfaces.incoming

import com.edernilson.ctr.domain.TravelRequestInput
import com.edernilson.ctr.domain.TravelRequestOutput
import com.edernilson.ctr.interfaces.incoming.mapping.TravelRequestMapper
import com.edernilson.ctr.domain.TravelService
import com.edernilson.ctr.interfaces.incoming.errorhandling.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.hateoas.EntityModel
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Service
@RestController
@RequestMapping(path = ["/travelRequest"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TravelRequestAPIImpl(val travelService: TravelService, val mapper: TravelRequestMapper): TravelRequestAPI {

    @PostMapping
    override fun makeTravelRequest(@Valid @RequestBody travelRequestInput: TravelRequestInput): EntityModel<TravelRequestOutput> {
        val travelRequest = travelService.saveTravelRequest(mapper.map(travelRequestInput))
        val output = mapper.map(travelRequest)

        return mapper.buildOutputModel(travelRequest, output)
    }

    @GetMapping("/nearby")
    override fun listNearbyRequests(@RequestParam currentAddress: String): List<EntityModel<TravelRequestOutput>> {
        val requests = travelService.listNearbyTravelRequests(currentAddress)
        return mapper.buildOutputModel(requests)
    }
}

@Tag(name = "TravelRequest API", description = "Processa solicitações de viagens.")
interface TravelRequestAPI {

    @Operation(
        description = "Cria uma nova solicitação de viagem",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso a solicitação tenha sido registrada na base"),
            ApiResponse(
                responseCode = "404", description = "Caso o motorista não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun makeTravelRequest(@Valid @RequestBody travelRequestInput: TravelRequestInput): EntityModel<TravelRequestOutput>

    @Operation(
        description = "Retorna a lista de viagens próximas da localização do motorista",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso a solicitação tenha sido executada com sucesso"),
        ],
    )
    fun listNearbyRequests(@RequestParam currentAddress: String): List<EntityModel<TravelRequestOutput>>
}