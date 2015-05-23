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
def OUTPUT_FILE_NAME = config.CreateRecipe.outputFileName
def MIMETYPES = config.MimeTypes

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
    calendar = Date.parse('M/d/yyyy zzzz', "$dateString Hawaii Standard Time").toCalendar()
    calendar.setTimeZone TimeZone.getTimeZone('UTC')
    date = calendar.time.format "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone('UTC')
    date
}

def policies = new XmlSlurper().parseText(new File(INPUT_FILE_NAME).text)

new File(OUTPUT_FILE_NAME).withWriter { writer ->
    new MarkupBuilder(writer).Orchard {
        setOmitEmptyAttributes(true)
        setOmitNullAttributes(true)
        Data {
            policies.policy.each {

                def title = it.@title
                def identifier = createIdentifier(it.@docNumber.text())
                def categories = it.categories.category
                def author = it.@author
                def publishedBy = it.@publishedBy
                def contentOwner = it.@contentOwner
                def created = convertDate(it.@created.text())
                def published = convertDate(it.@firstPublished.text())
                def modified = convertDate(it.@modified.text())
                def reviewDate = convertDate(it.@reviewDate.text())
                def content
                def documents = []

                if (it.content.text()) {
                    content = it.content.text().substring(50) - '</content>\n'
                }

                it.documents.document.each {
                    def documentIdentifier = createIdentifier(it.text())
                    def documentTitle = it.text()
                    def documentFilename = it.@filename.text()
                    def documentMimeType = MIMETYPES[documentFilename.tokenize('.')[-1]]
                    Document(Id:"/Identifier=$documentIdentifier", Status:'Published') {
                        IdentityPart(Identifier:documentIdentifier)
                        CommonPart(Owner:'User.UserName=admin')
                        TitlePart(Title:documentTitle)
                        MediaPart(FolderPath:'LibraryDocuments', FileName:documentFilename, MimeType:documentMimeType)
                    }
                    documents << documentIdentifier
                }

                LibraryPolicy(Id:"/Identifier=$identifier", Status:'Published') {
                    CommonPart(Owner:'/User.UserName=admin', CreatedUtc:created, PublishedUtc:published, ModifiedUtc:modified)
                    if (categories) {
                        'TaxonomyField.LibraryCategory'(Terms:"/alias=${categories*.@docnumber.collect{ it.text().replaceAll('/', '\\\\/')}.join(',/alias=')}")
                    } else {
                        'TaxonomyField.LibraryCategory'(Terms:'/alias=uncategorized')
                    }
                    'TextField.Author'(Text:author)
                    'TextField.PublishedBy'(Text:publishedBy)
                    'TextField.ContentOwner'(Text:contentOwner)
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
