/**
 * This is the configuration file there are a total of 4 scripts that have to
 * be run in order to create the recipies for Orchard.  The order is:
 *
 * CategorySlurper -> CreateTaxonomyImport
 *                \
 *                 -> PolicySlurper -> CreateRecepies
 */

/*
 * This script traverses the directory specified in inputDirName and writes an
 * xml file with the base-name specified in outputFileName.
 * taxonomies is an array of the taxonomies that will be created. It contains
 * arrays, each holding the roots of their taxonomy files.
 */
CategorySlurper {
    inputDirName = 'res/exportRemoved'
    outputFileName = 'res/categories'
    taxonomies = [
        [
            'zar_index.xml',
        ],
        [
            'zav_IN.MED-INDEX.xml',
            'zav_IN.CH-INDEX.xml',
            'facilities_and_durables.xml',
                //'zav_IN.FH-INDEX.xml',
                //'zav_IN.DM-INDEX.xml',
            'pharmacies.xml',
                //'zav_IN.MP-Med_Pharm.xml',
                //'zav_IN.RX-Pharmacies_-_Drug_Plans.xml',
            'zav_IN.RT-INDEX.xml',
            'zav_IN.VS-INDEX.xml',
            'zav_IN.QU-index.xml',
        ],
        //[
            //'zav_IN.PHARM-FORMULARY.xml',
            //'zav_IN.HC-INDEX.xml',
            //'zav_IN.Medicare-INDEX.xml',
        //],
    ]
}
CreateTaxonomyImport {
    outputFileName = 'res/Orchard_Taxonomy'
}
PolicySlurper {
    inputFolderName = "res/export"
    outputFileName = "res/policies.xml"
}
CreateRecipe {
    outputFileName = "res/recipe.xml"
}
CreateRecipies {
    outputFileName = "res/recipe"
    partitionSize = 100
}
MimeTypes {
    pdf = 'application/pdf'
    xml = 'application/xml'
    txt = 'text/plain'
    xls = 'application/vnd.ms-excel'
    xlsx = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    rtf = 'application/rtf'
    ppt = 'application/vnd.ms-powerpoint'
    doc = 'application/msword'
    docx = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
}
