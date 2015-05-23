def config = new ConfigSlurper().parse(new File('config.groovy').toURL())
INPUT_FOLDER_NAME = config.CategorySlurper.inputDirName
INPUT_FILE_NAME = config.CategorySlurper.outputFileName

def dir = new File(INPUT_FOLDER_NAME)
def allCategoriesDocnumbersByDir = []

dir.eachFileRecurse() { file ->
    def category = new XmlSlurper().parseText(file.text)
    allCategoriesDocnumbersByDir << category.pp_docnumber.text()
}

println "Number of categories in dir: ${allCategoriesDocnumbersByDir.size()}"
println "Number of unique categories in dir: ${allCategoriesDocnumbersByDir.unique().size()}"
println ''

def categoriesByXML = new XmlSlurper().parseText(new File(INPUT_FILE_NAME).text)
def allCategoriesByXML = categoriesByXML.depthFirst().findAll { it.name() == 'category' }
def allCategoriesDocnumbersByXML = allCategoriesByXML*.@docnumber*.text()

println "Number of categories in XML: ${allCategoriesByXML.size()}"
println "Number of unique categories in XML: ${allCategoriesByXML*.@docnumber.unique().size()}"
println ''

println 'Categories in dir that appear more than once:'
allCategoriesDocnumbersByDir.each {
    if(Collections.frequency(allCategoriesDocnumbersByDir, it) > 1)
        println it
}
println ''

println 'Categories in XML that appear more than once:'
allCategoriesDocnumbersByXML.each {
    if(Collections.frequency(allCategoriesDocnumbersByXML, it) > 1)
        println it
}
println ''

def categoriesNotFound = allCategoriesDocnumbersByDir - allCategoriesDocnumbersByXML

println "Number of categories in dir but not in XML: ${categoriesNotFound.size()}"
println ''

println 'Docnumbers of categories in dir that are not in XML:'
categoriesNotFound.each {
    println it
}
