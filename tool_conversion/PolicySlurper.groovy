/** PolicySlurper
 * This program will take the policy xml documents of the export. It will take
 * all information and print it in a easy to read format before it will
 * be transformed into the data section of an orchard recipe.
 */

import java.text.SimpleDateFormat

import groovy.io.FileType
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

def config = new ConfigSlurper().parse(new File('config.groovy').toURL())
def INPUT_FOLDER_NAME = config.PolicySlurper.inputFolderName
def OUTPUT_FILE_NAME = config.PolicySlurper.outputFileName
def CATEGORIES_FILE_NAME = config.CategorySlurper.outputFileName
def TAXONOMIES = config.CategorySlurper.taxonomies

def writer = new StringWriter()
def xml = new MarkupBuilder(writer)

def allCategories = []
TAXONOMIES.eachWithIndex { it, i ->
    def categoriesXML = new XmlSlurper().parseText(new File(CATEGORIES_FILE_NAME + "[${i}].xml").text)
    allCategories[i] = categoriesXML.depthFirst().findAll { it.name() == 'category' }
}

def getParentAlias(it) {
    ret = ''
    if (it.parent().@docnumber.text()) {
        ret += getParentAlias(it.parent()) + '/'
    }
    ret += it.@docnumber
    ret
}

xml.policies() {
    setOmitEmptyAttributes(true)
    setOmitNullAttributes(true)
    dir = new File(INPUT_FOLDER_NAME)
    dir.eachFileRecurse(FileType.FILES) { file ->
        def page = new XmlSlurper().parseText(file.text)
        def docs = page.depthFirst().findAll {
            (it.name().toLowerCase() == 'a' && it.@href.text().trim())
        }

        policy(
            title:page.pp_title,
            docNumber:page.pp_docnumber,
            author: page.pp_author,
            contentOwner: page.pp_content_owner,
            publishedBy: page.pp_publishedby,
            created: page.body.revisions.datecreated,
            firstPublished: page.body.revisions.firstpublished,
            modified: page.body.revisions.datelastupdated,
            reviewDate: page.pp_nextreviewdate
        ) {
            levels {
                page.body.meta.pp_level_1.each {
                    level1(it)
                }
                page.body.meta.pp_level_2.each {
                    level2(it)
                }
                page.body.meta.pp_level_3.each {
                    level3(it)
                }
                page.body.meta.pp_level_4.each {
                    level4(it)
                }
                page.body.meta.pp_level_5.each {
                    level5(it)
                }
                page.body.meta.pp_level_6.each {
                    level6(it)
                }
            }
            categories {
                allCategories.each { cats ->
                    taxonomy {
                        cats.each {
                            def inCategory = true && !(it.@docnumber in ['pharmacies', 'facilities_and_durables', 'zar_index','MED-INDEX','MM_Preventive_Services','PQSR_INDEX','HQSR_INDEX','zav_IN.PHARM-FORMULARY',])

                            it.instructions.'hierarchy-instruction'.each {

                                // println "${it.@a} ${it.@op} ${it.@b}"

                                if (it.@op == 'EQUALS') {
                                    switch (it.@a) {
                                        case 'pp_level_1':
                                            inCategory = (inCategory && it.@b in page.body.meta.pp_level_1)
                                            break
                                        case 'pp_level_2':
                                            inCategory = (inCategory && it.@b in page.body.meta.pp_level_2)
                                            break
                                        case 'pp_level_3':
                                            inCategory = (inCategory && it.@b in page.body.meta.pp_level_3)
                                            break
                                        case 'pp_level_4':
                                            inCategory = (inCategory && it.@b in page.body.meta.pp_level_4)
                                            break
                                        case 'pp_level_5':
                                            inCategory = (inCategory && it.@b in page.body.meta.pp_level_5)
                                            break
                                        case 'pp_level_6':
                                            inCategory = (inCategory && it.@b in page.body.meta.pp_level_6)
                                            break
                                        default:
                                            inCategory = inCategory
                                    }
                                } else if (it.@op == 'NOT EQUALS') {
                                    switch (it.@a) {
                                        case 'pp_level_1':
                                            inCategory = (inCategory && !(it.@b in page.body.meta.pp_level_1))
                                            break
                                        case 'pp_level_2':
                                            inCategory = (inCategory && !(it.@b in page.body.meta.pp_level_2))
                                            break
                                        case 'pp_level_3':
                                            inCategory = (inCategory && !(it.@b in page.body.meta.pp_level_3))
                                            break
                                        case 'pp_level_4':
                                            inCategory = (inCategory && !(it.@b in page.body.meta.pp_level_4))
                                            break
                                        case 'pp_level_5':
                                            inCategory = (inCategory && !(it.@b in page.body.meta.pp_level_5))
                                            break
                                        case 'pp_level_6':
                                            inCategory = (inCategory && !(it.@b in page.body.meta.pp_level_6))
                                            break
                                        default:
                                            inCategory = inCategory
                                    }
                                }
                            }


                            it.instructions.'attribute-instruction'.each {

                                //println "${it.@a} ${it.@op} ${it.@b}"

                                if (it.@a == 'Document Number') {
                                    switch (it.@op) {
                                        case 'CONTAINS':
                                            inCategory = (inCategory && page.pp_docnumber.text().contains(it.@b.text()))
                                            break
                                        case 'NOT CONTAINS':
                                            inCategory = (inCategory && !(page.pp_docnumber.text().contains(it.@b.text())))
                                            break
                                        case 'EQUALS':
                                            inCategory = (inCategory && (page.pp_docnumber.text() == it.@b.text()))
                                            break
                                        case 'NOT EQUALS':
                                            inCategory = (inCategory && (page.pp_docnumber.text() != it.@b.text()))
                                            break
                                        case 'BEGINS WITH':
                                            inCategory = (inCategory && (page.pp_docnumber.text().startsWith(it.@b.text())))
                                            break
                                        case 'NOT BEGINS WITH':
                                            inCategory = (inCategory && !(page.pp_docnumber.text().startsWith(it.@b.text())))
                                            break
                                        default:
                                            println it.@op.text()
                                            inCategory = inCategory
                                    }
                                } else if (it.@a == 'Document Name') {
                                    switch (it.@op) {
                                        case 'CONTAINS':
                                            inCategory = (inCategory && page.pp_title.text().contains(it.@b.text()))
                                            break
                                        case 'NOT CONTAINS':
                                            inCategory = (inCategory && !(page.pp_title.text().contains(it.@b.text())))
                                            break
                                        default:
                                            println it.@op.text()
                                            inCategory = inCategory
                                    }
                                } else if (it.@a == 'Published Date') {
                                    policyDateText = page.body.revisions.firstpublished.text()
                                    if (!policyDateText || policyDateText == 'nulldate') {
                                        inCategory = false
                                    } else {
                                        policyDate = new SimpleDateFormat('mm/dd/yyyy').parse(policyDateText)
                                        categoryDate = new SimpleDateFormat('mm/dd/yyyy').parse(it.@b.text())
                                        switch (it.@op) {
                                            case 'GREATER THAN OR EQUAL TO':
                                                inCategory = inCategory && (policyDate >= categoryDate)
                                                break
                                            case 'LESS THAN OR EQUAL TO':
                                                inCategory = inCategory && (policyDate <= categoryDate)
                                                break
                                            default:
                                                println it.@op.text()
                                                inCategory = inCategory
                                        }
                                    }
                                }
                            }

                            if (inCategory) {
                                category(title: it.@title, docnumber: getParentAlias(it))
                            }
                        }
                    }
                }
            }
            if (page.body.content.text()) {
                content(XmlUtil.serialize(page.body.content))
            }
            if (page.body.ExternalContentOutlineFormat.text()) {
                externalContentOutlineFormat(XmlUtil.serialize(page.body.ExternalContentOutlineFormat))
            }
            if (page.body.OutlineFormat.text()) {
                outlineFormat(XmlUtil.serialize(page.body.OutlineFormat))
            }
            if (page.body.ExternalContentClinicalMeasure.text()) {
                externalContentClinicalMeasure(XmlUtil.serialize(page.body.ExternalContentClinicalMeasure))
            }
            if (page.body.ExternalContentFreeFormText.text()) {
                externalContentFreeFormText(XmlUtil.serialize(page.body.ExternalContentFreeFormText))
            }
            if (page.body.ExternalContentConditionCodes.text()) {
                externalContentConditionCodes(XmlUtil.serialize(page.body.ExternalContentConditionCodes))
            }
            if (page.body.FreeFormText.text()) {
                freeFormText(XmlUtil.serialize(page.body.FreeFormText))
            }
            if (page.body.MedicalPolicyComments.text()) {
                medicalPolicyComments(XmlUtil.serialize(page.body.MedicalPolicyComments))
            }


            if (docs) {
                documents() {
                    docs.each {
                        document(it, filename:it.@href)
                    }
                }
            }
        }
    }
}

def outputFileWriter = new FileWriter(new File(OUTPUT_FILE_NAME))
outputFileWriter.write(writer.toString())
outputFileWriter.close()
