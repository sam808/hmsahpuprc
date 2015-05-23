/** CategorySlurper
 * This script creates a xml category tree for the policies.
 *
 * This script traverses the directory specified in inputDirName and writes an
 * xml file with the base-name specified in outputFileName.
 * taxonomies is an array of the taxonomies that will be created. It contains
 * arrays, each holding the roots of their taxonomies.
 */

@Grab(group='org.apache.commons', module='commons-io', version='1.3.2')
import org.apache.commons.io.FilenameUtils
import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder

def config = new ConfigSlurper().parse(new File('config.groovy').toURL())
INPUT_DIR_NAME = config.CategorySlurper.inputDirName
TAXONOMIES = config.CategorySlurper.taxonomies
OUTPUT_FILE_NAME = config.CategorySlurper.outputFileName

/*
 * For each taxonomy create a xml file, take the root file names and add their
 * categories under <categories\/>
 */
TAXONOMIES.eachWithIndex { it, i ->
    def fileNames = it
    new File(OUTPUT_FILE_NAME + "[${i}].xml").withWriter { w ->
        xml = new MarkupBuilder(w)
        xml.categories() {
            addCategoryToTree(INPUT_DIR_NAME, fileNames)
        }
    }
}

/**
 * Takes a name of an xml file (replaces the extension with '.xml') and returns
 * the GPathResult
 */
GPathResult parseXML(dir, fileNameHTM) {
    def fileNameXML = FilenameUtils.getBaseName(fileNameHTM) + '.xml'
    def file = new File(dir, fileNameXML)
    def category = new XmlSlurper().parseText(file.text)
    return category
}

def getHierachyInstructions(criteria) {
    criteria.depthFirst().findAll {
        it.name().matches(~/HierarchyCriteria\d/) && it.children()[1].text() && it.children()[2].text()
    }
}

def getAttributeInstructions(criteria) {
    criteria.'*'.findAll {
        it.name().matches(~/AttributeCriteria/)
    }
}

/*
 * This is a depth first traversal of the categories.
 * For each file their xml content gets parsed, after that their subcategories
 * are pulled and their own <category>-tag is created. In the tag the
 * instructions (pp levels) are stored. Then their subcategories get added.
 */
def addCategoryToTree(dir, fileNames) {

    fileNames.unique().each() {
        def category
        // Try to parse the file or disregard.
        try {
            category = parseXML(dir, it)
        } catch(e) {
            System.err.println "failed to open ${it}"
            return
        }

        // Links to subcategories are in tables ...
        def subCategories = category.body.content.table.depthFirst().findAll {
            (it.name().toLowerCase() == 'a' && it.@href.text().trim())
        }

        // or in unordered lists.
        subCategories += category.body.ul.depthFirst().findAll {
            (it.name().toLowerCase() == 'a' && it.@href.text().trim())
        }

        // Write out xml <category>
        xml.category(title:category.pp_title.text(), docnumber:category.pp_docnumber) {
            // <instructions>
            instructions {
                getHierachyInstructions(category.body.Instructions.CriteriaList).each {
                    'hierarchy-instruction'(a:it.children()[0].text().trim(), op:it.children()[1].text().trim(), b:it.children()[2].text().trim())
                }
                getAttributeInstructions(category.body.Instructions.CriteriaList).each {
                    'attribute-instruction'(a:it.Attribute.text().trim(), op:it.Comparison.text().trim(), b:it.Value.text().trim())
                }
            }
            // <category/> ...
            addCategoryToTree(dir, subCategories*.@href*.text())
        }

    }

}
