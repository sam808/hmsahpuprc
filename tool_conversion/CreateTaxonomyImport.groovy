/** CreateTaxonomyImport
 * uses the output file of CategorySlurper to construct txt files ready for
 * import into Orchard.
 */

def config = new ConfigSlurper().parse(new File('config.groovy').toURL())
TAXONOMIES = config.CategorySlurper.taxonomies
INPUT_FILE_NAME = config.CategorySlurper.outputFileName
OUTPUT_FILE_NAME = config.CreateTaxonomyImport.outputFileName

/*
 * All this does is a translation of the xml category files to txt files
 * created with CategorySlurper.groovy
 */
TAXONOMIES.eachWithIndex { it, i ->
    def categoriesXML = new XmlSlurper().parseText(new File(INPUT_FILE_NAME + "[${i}].xml").text)
    def filePrinter = new File(OUTPUT_FILE_NAME + "[${i}].txt").newPrintWriter()

    def categoryLevelPrinter
    categoryLevelPrinter = { categories, indent, prefix ->
        categories.each {
            indent.times { filePrinter.print '\t'}
            filePrinter.println "${ it.@title };${ prefix }${ it.@docnumber }"
            categoryLevelPrinter(it.category, indent + 1, prefix + it.@docnumber + '/')
        }
    }

    categoryLevelPrinter(categoriesXML.category, 0, '')
    filePrinter.close()
}
