
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

def heading1 = '= Heading 1 ='
def heading2 = '== Heading 2 =='
def heading3 = '=== Heading 3 ==='
def heading4 = '==== Heading 4 ===='
def heading5 = '===== Heading 5 ====='
def heading6 = '====== Heading 6 ======'

XToTiddlyWikiConverter converter = new XToTiddlyWikiConverter()

assert converter.convert(heading1) == '! Heading 1 '
assert converter.convert(heading2) == '!! Heading 2 '
assert converter.convert(heading3) == '!!! Heading 3 '
assert converter.convert(heading4) == '!!!! Heading 4 '
assert converter.convert(heading5) == '!!!!! Heading 5 '
assert converter.convert(heading6) == '!!!!!! Heading 6 '

def tableHeading = '|= Heading |= Heading'
def tableRow = '| Row | Row'

assert converter.convert(tableHeading) == '|! Heading |! Heading|'
assert converter.convert(tableHeading) == '|! Heading |! Heading|'
