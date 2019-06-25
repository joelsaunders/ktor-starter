package com.thebookofjoel

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.auth.principal
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.mindrot.jbcrypt.BCrypt
import service.UserService
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


class SimpleJWT(secret: String) {
    private val validityInMs = 36_000_00 * 1
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create()
        .withClaim("name", name)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + validityInMs)
}


data class LoginRegister(val email: String, val password: String)


@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val simpleJwt = SimpleJWT(environment.config.property("jwt.secret").getString())
    val userService = UserService()

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    DatabaseFactory.init()
    
    routing {
        post("/login") {
            val post = call.receive<LoginRegister>()

            val user = userService.getUserByEmail(post.email)
            if (user == null || !BCrypt.checkpw(post.password, user.password)) {
                error("Invalid Credentials")
            }

            call.respond(mapOf("token" to simpleJwt.sign(user.email)))
        }
        authenticate {
            get("/user" ) {
                call.respond(userService.getAllUsers())
                val principal = call.principal<UserIdPrincipal>() ?: error("No principal decoded")
                val userEmail = principal.name
                val user = userService.getUserByEmail(userEmail)?: error("user not found")
                if (!user.active) {
                    error("user not active")
                }
                call.respond(userService.getAllUsers())
            }
        }
    }

}

