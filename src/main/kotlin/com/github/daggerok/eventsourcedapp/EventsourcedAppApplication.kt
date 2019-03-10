package com.github.daggerok.eventsourcedapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventsourcedAppApplication

fun main(args: Array<String>) {
  runApplication<EventsourcedAppApplication>(*args)
}
