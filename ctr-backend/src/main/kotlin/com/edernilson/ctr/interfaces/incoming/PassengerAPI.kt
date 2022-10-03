package com.edernilson.ctr.interfaces.incoming

import com.edernilson.ctr.domain.Passenger
import com.edernilson.ctr.domain.PassengerRepository
import com.edernilson.ctr.interfaces.incoming.errorhandling.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.annotation.security.RolesAllowed

@Service
@RestController
@RequestMapping(path = ["/passengers"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PassengerAPIImpl(val passengerRepository: PassengerRepository): PassengerAPI {
    @GetMapping
    override fun listPassengers() = passengerRepository.findAll()

    @GetMapping("/{id}")
    override fun findPassenger(@PathVariable("id") id: Long) =
        passengerRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @RolesAllowed("ROLE_ADMIN")
    @PostMapping
    override fun createPassenger(@RequestBody passenger: Passenger) =
        passengerRepository.save(passenger)

    @PutMapping("/{id}")
    override fun fullUpdatePassenger(@PathVariable("id") id: Long, @RequestBody passenger: Passenger): Passenger {
        val newPassenger = findPassenger(id).copy(
            name = passenger.name
        )
        return passengerRepository.save(newPassenger)
    }

    @PatchMapping("/{id}")
    override fun incrementalUpdatePassenger(@PathVariable("id") id: Long, @RequestBody passenger: PatchPassenger): Passenger {
        val foundPassenger = findPassenger(id)
        val newPassenger = foundPassenger.copy(
            name = passenger.name ?: foundPassenger.name
        )
        return passengerRepository.save(newPassenger)
    }

    @DeleteMapping("/{id}")
    override fun deletePassenger(@PathVariable("id") id: Long) = passengerRepository.delete(findPassenger(id))
}

@Tag(name = "Passenger Transfer", description = "Representa o objeto enviado para a atualizado parcial de passageiros")
data class PatchPassenger(
    val name: String?
)

@Tag(name = "Passenger API", description = "Manipula dados de passageiros.")
interface PassengerAPI {
    @Operation(description = "Lista todos os passageiros disponíveis")
    fun listPassengers(): List<Passenger>

    @Operation(
        description = "Localiza um passageiro específico",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o passageiro tenha sido encontrado na base"),
            ApiResponse(
                responseCode = "404", description = "Caso o passageiro não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun findPassenger(@PathVariable("id") id: Long): Passenger

    @Operation(
        description = "Cria um novo passageiro",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o passageiro tenha sido incluido na base"),
        ],
    )
    fun createPassenger(@RequestBody passenger: Passenger): Passenger

    @Operation(
        description = "Atualiza um passageiro específico enviando todos os campos",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o passageiro tenha sido atualizado com sucesso"),
            ApiResponse(
                responseCode = "404", description = "Caso o passageiro não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun fullUpdatePassenger(@PathVariable("id") id: Long, @RequestBody passenger: Passenger): Passenger

    @Operation(
        description = "Atualiza um passageiro específico enviando alguns campos",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o passageiro tenha sido atualizado com sucesso"),
            ApiResponse(
                responseCode = "404", description = "Caso o passageiro não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun incrementalUpdatePassenger(@PathVariable("id") id: Long, @RequestBody passenger: PatchPassenger): Passenger

    @Operation(
        description = "Exclui um passageiro específico",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o passageiro tenha sido excluido com sucesso"),
            ApiResponse(
                responseCode = "404", description = "Caso o passageiro não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun deletePassenger(@PathVariable("id") id: Long)
}