package tilbud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TilbudApplication

fun main(args: Array<String>) {
    runApplication<TilbudApplication>(*args)
}
