# Run Migration of HMSA Policies

1. Install Groovy. http://www.groovy-lang.org/
2. Run the Groovy scripts:
    1. groovy CategorySlurper.groovy
    2. groovy CreateTaxonomyImport.groovy
    3. groovy PolicySlurper.groovy
    4. groovy CreateRecipies.groovy

# Scripts

CategorySlurper -> CreateTaxonomyImport
                \
                 -> PolicySlurper -> CreateRecepies

## CategorySlurper
This script creates a xml category tree for the policies.

This script traverses the directory specified in inputDirName and writes an
xml file with the base-name specified in outputFileName.
taxonomies is an array of the taxonomies that will be created. It contains
arrays, each holding the roots of their taxonomies.

## CreateTaxonomyImport
Uses the output file of CategorySlurper to construct txt files ready for
import into Orchard.

## PolicySlurper
This script takes the policy xml documents of the export. It takes
all information and print it in a easy to read format before the new xml
is transformed into the data section of an orchard recipe with the next script.

## CreateRecipe
This script takes the policy xml document. It transformes it into the
data section of an orchard recipe.

# Configuration config.groovy
Here the scripts can be configured. I.e. input file names, taxonomies, partition sizes.