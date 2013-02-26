@Grapes([
    @Grab ('mysql:mysql-connector-java:5.1.6'),
    @GrabConfig(systemClassLoader = true)
])
import groovy.sql.Sql
import com.mysql.jdbc.Driver
import groovy.xml.MarkupBuilder


interface Converter {

    String convert(String line)
}

class HeadingConverter implements Converter {

    static def headings = []
    
    static {
      6.downto(1) { n ->
        headings << ~/^(={$n})(.*?)(={$n})$/
      }
    }
    
    String convert(String line) {
    
      for (heading in headings) {
        def result = line =~ heading
        if (result.find()) {
          return line.replaceAll(heading, '!' * result[0][1].size() + "\$2")
        }
      }
      return line
    }

}

class TableConverter implements Converter {

    String convert(String line) {
        line = line.startsWith('|=') ? line.replace('|=', '|!') : line
        line = line.startsWith('|') ? line + '|' : line
    }
}

class XToTiddlyWikiConverter {
    
    Converter[] converters = [new HeadingConverter(), new TableConverter()]

    String convert(String line) {
        for (converter in converters) {
            line = converter.convert(line)
        }
        return line
    }
}


def url = 'jdbc:mysql://localhost:3306/personal'
def userName = 'root'
def password = 'P799_dom'
def driver = 'com.mysql.jdbc.Driver'
def sql = groovy.sql.Sql.newInstance(url, userName, password, driver)

assert sql, 'Unable to configure db connection'

def timestampFormat = 'yyyyMMddHHmm'

def processContent = { converter, content ->

    content.readLines().collect { line ->
        converter.convert(line)
    }.join('\n')
}

def converter = new XToTiddlyWikiConverter()
def writer = new FileWriter("/home/ian/Documents/TiddlyWiki/xwiki.html")
def builder = new MarkupBuilder(writer)
writer << "<!DOCTYPE html>"
builder.html {
    body {
        div(id: "storeArea") {
            sql.eachRow("select xwd_fullname, xwd_name, xwd_title, xwd_date, xwd_creation_date, xwd_content_update_date, xwd_author, xwd_content from xwikidoc where xwd_author like 'XWiki.ibriscoe'") { doc ->
                div(title: doc.xwd_fullname, creator:"Ian", modifier:"Ian", created: doc.xwd_creation_date.format(timestampFormat), modified: doc.xwd_content_update_date.format(timestampFormat)) {
                    pre {
                        mkp.yield processContent(converter, doc.xwd_content)
                    }
                }
            }
        }
    }
}
