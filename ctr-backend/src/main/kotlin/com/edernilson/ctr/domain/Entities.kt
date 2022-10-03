package com.edernilson.ctr.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "drivers")
@Schema(description = "Representa um motorista dentro da plataforma")
data class Driver(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @get:Schema(description = "Nome do motorista")
    @get:Size(min = 5, max = 255)
    val name: String,
    @get:Schema(description = "Data de nascimento do motorista")
    val birthDate: LocalDate
)

@Entity
@Table(name = "passengers")
@Schema(description = "Representa um passageiro dentro da plataforma")
data class Passenger(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @get:Schema(description = "Nome do passageiro")
    val name: String
)

@Entity
@Table(name = "travel_requests")
data class TravelRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    val passenger: Passenger,
    val origin: String,
    val destination: String,
    val status: TravelRequestStatus = TravelRequestStatus.CREATED,
    val creationDate: LocalDateTime = LocalDateTime.now()
)

@Schema(description = "Representa uma solicitação de viagem dentro da plataforma")
data class TravelRequestInput(
    @get:NotNull
    @get:Schema(description = "Código de identificação do passageiro")
    val passengerId: Long?,
    @get:NotEmpty
    @get:Schema(description = "Localização de partida da viagem")
    val origin: String?,
    @get:NotEmpty
    @get:Schema(description = "Localização do destino da viagem")
    val destination: String?
)

enum class TravelRequestStatus {
    CREATED, ACCEPTED, REFUSED
}

@Schema(description = "Representa a saída de uma solicitação de viagem dentro da plataforma")
data class TravelRequestOutput(
    val id: Long,
    val origin: String,
    val destination: String,
    val status: TravelRequestStatus,
    val creationDate: LocalDateTime
)

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    val username: String,
    val password: String,
    val enabled: Boolean = true,

    @ElementCollection
    val roles: MutableList<String>
)