package kz.samat.patientservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class PatientServiceApplication

fun main(args: Array<String>) {
    runApplication<PatientServiceApplication>(*args)
}
