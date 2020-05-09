package com.anjelikabog.spring_unmarshalling_xml_save_to_db

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import marshalling_xml.Persons
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.MessageChannels
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.file.dsl.Files
import org.springframework.messaging.MessageHeaders
import java.io.File

@SpringBootApplication
class MainSpringUnmarshallingXmlSaveToDbApplication

fun main(args: Array<String>) {
    runApplication<MainSpringUnmarshallingXmlSaveToDbApplication>(*args)
}



