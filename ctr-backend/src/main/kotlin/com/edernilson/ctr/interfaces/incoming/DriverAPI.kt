package com.edernilson.ctr.interfaces.incoming

import com.edernilson.ctr.domain.Driver
import com.edernilson.ctr.domain.DriverRepository
import com.edernilson.ctr.interfaces.incoming.errorhandling.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
@RestController
@RequestMapping(path = ["/drivers"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DriverAPIImpl(val driverRepository: DriverRepository) : DriverAPI {
    companion object {
        private const val PAGE_SIZE: Int = 10
    }

    @GetMapping
    override fun listDrivers(@RequestParam(name = "page", defaultValue = "0") page: Int) : Drivers {
        val driverPage = driverRepository.findAll(PageRequest.of(page, PAGE_SIZE))
        val drivers = driverPage.content.map { EntityModel.of(it) }
        val lastPageLink = linkTo<DriverAPIImpl> {
            listDrivers(driverPage.totalPages - 1) }
            .withRel("lastPage")
        return Drivers(drivers, listOf(lastPageLink))
    }

    @GetMapping("/{id}")
    override fun findDriver(@Parameter(description = "ID do motorista a ser localizado") @PathVariable("id") id: Long) =
        driverRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND)
        }

    @PostMapping
    override fun createDriver(@RequestBody driver: Driver): Driver {
        driver.id = null
        return driverRepository.save(driver)
    }

    @PutMapping("/{id}")
    override fun fullUpdateDriver(@PathVariable("id") id: Long, @RequestBody driver: Driver): Driver {
        val foundedDriver = findDriver(id)
        val copyDriver = foundedDriver.copy(
            birthDate = driver.birthDate,
            name = driver.name
        )
        return driverRepository.save(copyDriver)
    }

    @PatchMapping("/{id}")
    override fun incrementalUpdateDriver(@PathVariable("id") id: Long, @RequestBody driver: PatchDriver): Driver {
        val foundedDriver = findDriver(id)
        val copyDriver = foundedDriver.copy(
            birthDate = driver.birthDate ?: foundedDriver.birthDate,
            name = driver.name ?: foundedDriver.name
        )
        return driverRepository.save(copyDriver)
    }

    @DeleteMapping("/{id}")
    override fun deleteDriver(@PathVariable("id") id: Long) = driverRepository.delete(findDriver(id))
}

open class Drivers(
    val drivers: List<EntityModel<Driver>>,
    val links: List<Link> = emptyList()
)

@Tag(name = "Driver Transfer", description = "Representa o objeto enviado para a atualizado parcial de motoristas")
data class PatchDriver(
    val name: String?,
    val birthDate: LocalDate?
)

@Tag(name = "Driver API", description = "Manipula dados de motoristas.")
interface DriverAPI {
    @Operation(description = "Lista todos os motoristas disponíveis")
    fun listDrivers(@RequestParam(name = "page", defaultValue = "0") page: Int): Drivers

    @Operation(
        description = "Localiza um motorista específico",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o motorista tenha sido encontrado na base"),
            ApiResponse(
                responseCode = "404", description = "Caso o motorista não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun findDriver(@Parameter(description = "ID do motorista a ser localizado") id: Long): Driver

    @Operation(
        description = "Cria um novo motorista",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o motorista tenha sido incluido na base"),
        ],
    )
    fun createDriver(driver: Driver): Driver

    @Operation(
        description = "Atualiza um motorista específico enviando todos os campos",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o motorista tenha sido atualizado com sucesso"),
            ApiResponse(
                responseCode = "404", description = "Caso o motorista não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun fullUpdateDriver(id: Long, driver: Driver): Driver

    @Operation(
        description = "Atualiza um motorista específico enviando alguns campos",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o motorista tenha sido atualizado com sucesso"),
            ApiResponse(
                responseCode = "404", description = "Caso o motorista não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun incrementalUpdateDriver(id: Long, driver: PatchDriver): Driver

    @Operation(
        description = "Exclui um motorista específico",
        responses = [
            ApiResponse(responseCode = "200", description = "Caso o motorista tenha sido excluido com sucesso"),
            ApiResponse(
                responseCode = "404", description = "Caso o motorista não tenha sido encontrado",
                content = [Content(
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ],
    )
    fun deleteDriver(@PathVariable("id") id: Long)
}