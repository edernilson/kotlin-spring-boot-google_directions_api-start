package com.edernilson.ctr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CtrBackendApplication

fun main(args: Array<String>) {
    runApplication<CtrBackendApplication>(*args)
}
