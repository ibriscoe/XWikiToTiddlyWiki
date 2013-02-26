@Grapes([
    @Grab ('mysql:mysql-connector-java:5.1.6'),
    @GrabConfig(systemClassLoader = true)
])
import groovy.sql.Sql
import com.mysql.jdbc.Driver

def url = 'jdbc:mysql://localhost:3306/personal'
def userName = 'root'
def password = 'password'
def driver = 'com.mysql.jdbc.Driver'
def sql = groovy.sql.Sql.newInstance(url, userName, password, driver)

assert sql, 'Unable to configure db connection'

def query = '''
select
    xwd_fullname, 
    xwd_creation_date, 
    xwd_content_update_date, 
    xwd_author, 
    xwd_content 
from 
    xwikidoc 
where 
    xwd_author like "XWiki.ibriscoe"'''

sql.eachRow(query) { doc ->
    new File("/home/ian/XWikiFiles/${doc.xwd_fullname}.xwiki").withWriter { writer ->
        writer << doc.xwd_content
    }
}
