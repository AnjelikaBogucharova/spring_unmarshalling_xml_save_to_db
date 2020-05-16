package com.anjelikabog.spring_unmarshalling_xml_save_to_db

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.anjelikabog.spring_unmarshalling_xml_save_to_db.xml.Persons
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.file.dsl.Files
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.ErrorMessage
import java.io.File
import java.sql.Date
import java.sql.ResultSet
import java.sql.Statement


@Configuration
class ChanelConfiguration {
    @Bean
    fun xml() = MessageChannels.direct().get()

    @Bean
    fun errors() = MessageChannels.direct().get()
}

@Configuration
class FileConfiguration(
        private val channels: ChanelConfiguration,
        private val xmlMapper: XmlMapper,
        var jdbcTemplate: JdbcTemplate
) {
    private val input = File("src/main/resources/input")
    private val errors = File("src/main/resources/errors")
    private val archive = File("src/main/resources/archive")


    @Bean
    fun filesFlow() = integrationFlow(
            Files.inboundAdapter(this.input)
                    .autoCreateDirectory(true),
            {
                poller { it.fixedDelay(500).maxMessagesPerPoll(1) }
            }
    ) {
        filter<File> { it.isFile }
        enrichHeaders(mapOf(
                "errorChannel" to "errors"
        ))

        handle { file: File, _: MessageHeaders ->
            val person = xmlMapper.readValue(file, Persons::class.java)
            var rsPer: ResultSet? = null
            var rsHob: ResultSet
            val keyHolderHobby = GeneratedKeyHolder()
            val keyHolderPerson = GeneratedKeyHolder()

            for (per in person.person!!) {

                var insertPersons = jdbcTemplate.update ({ connection ->
                    val ps = connection.prepareStatement("insert into persons(fullname, birthday) values (?,?)",
                            Statement.RETURN_GENERATED_KEYS)
                    ps.setString(1, per.name)
                    ps.setDate(2, per.birthday?.time?.let { Date(it) })
                    ps
                }, keyHolderPerson)

                for (hobbs in per.hobbies?.hobby!!) {

                    var insertHobby = jdbcTemplate.update ({ connection ->
                        val ps = connection.prepareStatement("insert into hobby(complexity,hobby_name) values (?,?)",
                                Statement.RETURN_GENERATED_KEYS)
                        ps.setInt(1, hobbs.complexity as Int)
                        ps.setString(2, hobbs.hobby_name)
                        ps
                    }, keyHolderHobby)


                    var insertHobbies = jdbcTemplate.update { connection ->
                        val ps = connection.prepareStatement("insert into hobbies values (?,?)")
                        ps.setLong(1, keyHolderPerson.keys?.get("id") as Long)
                        ps.setLong(2, keyHolderHobby.keys?.get("id") as Long)
                        ps
                    }

                }
            }

            file
        }
        channel("xml")
    }

    @Bean
    fun archiveFlow() = integrationFlow(channels.xml()) {
        handle(Files.outboundAdapter(archive)
                .deleteSourceFiles(true)
                .autoCreateDirectory(true)
        )
    }

    @Bean
    fun errorsFlow() = integrationFlow(channels.errors()) {
        transform<ErrorMessage> {
            it.originalMessage?.payload as File
        }
        handle(Files.outboundAdapter(errors)
                .deleteSourceFiles(true)
                .autoCreateDirectory(true)
        )
    }
}