/** CreateRecipe
 * This program will take the policy xml document. It transformes it into the
 * data section of an orchard recipe.
 */

import java.text.SimpleDateFormat
import java.security.MessageDigest

import groovy.io.FileType
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

def config = new ConfigSlurper().parse(new File('config.groovy').toURL())
def INPUT_FILE_NAME = config.PolicySlurper.outputFileName
def OUTPUT_FILE_NAME = config.CreateRecipies.outputFileName
def MIMETYPES = config.MimeTypes
def PARTITION_SIZE = config.CreateRecipies.partitionSize

def createIdentifier(String s) {
    MessageDigest digest = MessageDigest.getInstance("MD5")
    digest.update(s.bytes);
    new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
}

boolean isIndex(String content) {
    return content ==~ /.+current.+previous.+/
}

String convertDate(String dateString) {
    if (!(dateString ==~ /\d+\/\d+\/\d+/)) return
    if (dateString.split('/')[2].size() < 4) {
        temp = dateString.split('/')
        if (temp[2] == '201') temp[2] = '10'
        else if (temp[2] == '012') temp[2] = '12'
        temp[2] = '20'+ temp[2]
        if (temp[1] == '085') temp[1] = '08'
        dateString = temp.join('/')
    }
    if (dateString.split('/')[2].toInteger() < 1900) {
        temp = dateString.split('/')
        temp[2] = '1900'
        dateString = temp.join('/')
    }
    calendar = Date.parse('M/d/yyyy zzzz', "$dateString Hawaii Standard Time").toCalendar()
    calendar.setTimeZone TimeZone.getTimeZone('UTC')
    date = calendar.time.format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone('UTC')
    date
}

def policies = new XmlSlurper().parseText(new File(INPUT_FILE_NAME).text)
def policiesPartitioned = policies.policy.collate(PARTITION_SIZE)
policiesPartitioned.eachWithIndex { it, i ->
    def policyPartition = it
    new File(OUTPUT_FILE_NAME + "[${i + 1},${policyPartition.size()}].xml").withWriter { writer ->

        println "=========================="
        println ""
        println "[${i + 1},${policyPartition.size()}].xml"
        println "=========================="
        new MarkupBuilder(writer).Orchard {
            setOmitEmptyAttributes(true)
            setOmitNullAttributes(true)
            Data {
                policyPartition.each {
                    def title = it.@title
                    def identifier = createIdentifier(it.@docNumber.text())
                    def docnumber = it.@docNumber.text()
                    def taxonomies = it.categories.taxonomy
                    def author = it.@author
                    def publishedBy = it.@publishedBy
                    def contentOwner = it.@contentOwner
                    def created = convertDate(it.@created.text())
                    def published = convertDate(it.@firstPublished.text())
                    def modified = convertDate(it.@modified.text())
                    def reviewDate = convertDate(it.@reviewDate.text())
                    def content = ''
                    def documents = []

                    if (it.content.text()) {
                        content += it.content.text().substring(50) - '</content>\n'
                    }
                    if (it.externalContentOutlineFormat.text()) {
                        content += it.externalContentOutlineFormat.text().substring(68) - '</ExternalContentOutlineFormat>'
                    }
                    if (it.outlineFormat.text()) {
                        content += it.outlineFormat.text().substring(53) - '</OutlineFormat>'
                    }
                    if (it.externalContentClinicalMeasure.text()) {
                        content += it.externalContentClinicalMeasure.text().substring(71) - '</ExternalContentClinicalMeasure>'
                    }
                    if (it.externalContentFreeFormText.text()) {
                        content += it.externalContentFreeFormText.text().substring(68) - '</ExternalContentFreeFormText>'
                    }
                    if (it.externalContentConditionCodes.text()) {
                        content += it.externalContentConditionCodes.text().substring(71) - '</ExternalContentConditionCodes>'
                    }
                    if (it.freeFormText.text()) {
                        content += it.freeFormText.text().substring(54) - '</FreeFormText>'
                    }
                    if (it.medicalPolicyComments.text()) {
                        content += it.medicalPolicyComments.text().substring(63) - '</MedicalPolicyComments>'
                    }

                    content = content.replaceAll(~"((href=\".+\\.)(xml)\")", { Object[] m -> m[2] + 'htm"' })

                    if (content =~ /([Vv]iew|[Ss]ee|[Dd]ownload).*current.*([Dd]ocument|[Ff]ile|[Pp]olic(y|ies))[\s\S]*([Vv]iew|[Ss]ee|[Dd]ownload).*previous.*([Dd]ocument|[Ff]ile|[Pp]olic(y|ies))/) {
                        println "$title; $docnumber"
                        it.documents.document.each {
                            def documentFilename = it.@filename.text()
                            def documentIdentifier = createIdentifier(it.text())
                            def documentTitle = it.text()
                            def documentMimeType = MIMETYPES[documentFilename.tokenize('.')[-1]]
                            if (!(documentFilename =~ /(xml|html?)/ || documentFilename =~ /http/)) {
                                println "  $documentFilename"
                                Document(Id:"/Identifier=$documentIdentifier", Status:'Published') {
                                    IdentityPart(Identifier:documentIdentifier)
                                    CommonPart(Owner:'User.UserName=admin')
                                    TitlePart(Title:documentTitle)
                                    MediaPart(FolderPath:'LibraryDocuments', FileName:documentFilename, MimeType:documentMimeType)
                                }
                                documents << documentIdentifier
                            }
                        }
                    }

                    LibraryPolicy(Id:"/Identifier=$identifier", Status:'Published') {
                        CommonPart(Owner:'/User.UserName=admin', CreatedUtc:created, PublishedUtc:published, ModifiedUtc:modified)
                            if (taxonomies[0].category*.@docnumber) {
                                'TaxonomyField.CommunicationArchive'(Terms:"/alias=${taxonomies[0].category*.@docnumber.collect{ it.text().replaceAll('/', '\\\\/')}.join(',/alias=')}")
                            }
                            if (taxonomies[1].category*.@docnumber) {
                                'TaxonomyField.LibraryCategory'(Terms:"/alias=${taxonomies[1].category*.@docnumber.collect{ it.text().replaceAll('/', '\\\\/')}.join(',/alias=')}")
                            }
                        'TextField.Author'(Text:author)
                        'TextField.PublishedBy'(Text:publishedBy)
                        'TextField.ContentOwner'(Text:contentOwner)
                        'TextField.Docnumber'(Text:docnumber)
                        'LibraryPolicy.NextReviewDate'(Value:reviewDate)
                        if (documents) {
                            'MediaLibraryPickerField.CurrentDocument'(ContentItems:"/Identifier=${documents.remove(0)}")
                            if (documents) {
                                'MediaLibraryPickerField.PreviousDocument'(ContentItems:"/Identifier=${documents.join(',/Identifier=')}")
                            }
                        }
                        IdentityPart(Identifier:identifier)
                        BodyPart(Text:content)
                        TitlePart(Title:title)
                    }
                }
            }
        }
    }
}
