package com.anjelikabog.spring_unmarshalling_xml_save_to_db

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import marshalling_xml.Persons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.file.dsl.Files
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.ErrorMessage
import java.io.File
import javax.sql.DataSource

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
        private val xmlMapper: XmlMapper
) {
    private val input = File("src/main/resources/input")
    private val errors = File("src/main/resources/errors")
    private val archive = File("src/main/resources/archive")
    @Autowired
    private var dataSource: DataSource? = null

    @Autowired
    private var jdbcTemplate: JdbcTemplate? = null

    fun setDataSource(dataSource: DataSource?) {
        this.dataSource = dataSource
        jdbcTemplate = JdbcTemplate(dataSource!!)
    }

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

            val insertPersons = "insert into persons(fullname, birthday) values (?,?)"
            val insertHobby = "insert into hobby(complexity, hobby_name) values (?,?)"
            val insertHobbies = "insert into hobbies values (?,?)"

            println(person)
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