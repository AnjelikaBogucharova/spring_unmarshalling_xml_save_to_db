package marshalling_xml

import com.fasterxml.jackson.annotation.JacksonAnnotation
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.dataformat.xml.annotation.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@JacksonXmlRootElement(localName = "Persons")
data class Persons(
        @field: JacksonXmlElementWrapper(useWrapping = false)
        @field: JacksonXmlProperty(localName = "Person")
        var person: List<Person>? = null
) {
    override fun toString(): String {
        return """
Persons: $person
""".trimMargin()
    }
}


data class Person(
        var name: String? = null,
        //@field: JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Europe/Moscow")
        var birthday: Date? = null,
        var hobbies: Hobbies? = null
)
{
    override fun toString(): String {
        val sf = SimpleDateFormat("dd.MM.yyyy")
        return """

name: $name
birthday: ${sf.format(birthday)} 
hobbies: $hobbies 
""".trimIndent()
    }
}

data class Hobbies(
        @field: JacksonXmlElementWrapper(useWrapping = false)
        var hobby: List<Hobby>? = null
){
    override fun toString()="""
$hobby
""".trimIndent()
}

data class Hobby(
        var complexity: Int? = null,
        var hobby_name: String? = null
) {
    override fun toString()="""

Hobby:
complexity: $complexity
hobby_name: $hobby_name  
""".trimIndent()
}