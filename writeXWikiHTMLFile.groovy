@Grapes([
    @Grab ('mysql:mysql-connector-java:5.1.6'),
    @GrabConfig(systemClassLoader = true)
])
import groovy.sql.Sql
import com.mysql.jdbc.Driver
import groovy.xml.MarkupBuilder

def url = 'jdbc:mysql://localhost:3306/personal'
def userName = 'root'
def password = 'password'
def driver = 'com.mysql.jdbc.Driver'
def sql = groovy.sql.Sql.newInstance(url, userName, password, driver)

assert sql, 'Unable to configure db connection'

def creator = "Ian"
def timestampFormat = 'yyyyMMddHHmm'

def writer = new FileWriter("/home/ian/Documents/TiddlyWiki/xwiki.html")
def builder = new MarkupBuilder(writer)
writer << "<!DOCTYPE html>\n"
builder.html {
    body {
        div(id: "storeArea") {
            sql.eachRow("select xwd_fullname, xwd_name, xwd_title, xwd_date, xwd_creation_date, xwd_content_update_date, xwd_author, xwd_content from xwikidoc where xwd_author like 'XWiki.ibriscoe'") { doc ->
                div(title: doc.xwd_fullname, creator: creator, modifier: creator, 
                        created: doc.xwd_creation_date.format(timestampFormat), 
                        modified: doc.xwd_content_update_date.format(timestampFormat)) {
                    pre {
                        mkp.yield doc.xwd_content
                    }
                }
            }
        }
    }
}
