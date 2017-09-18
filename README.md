# qWizard

## Installation instructions

1. Follow the instructions for setting up Liferay and openBIS as well as the QBiC-specific data model found on our portal (http://qbic.life/portal/web/qbic/software)
2. Clone qWizard from the git repository: 'git clone https://github.com/qbicsoftware/projectwizard projectwizard'
3. Adjust the properties defined in the file 'portlet.properties' (this is what links the portlet to the qbic-ext.properties file)
4. For deployment a web application archive (.war) file has to be created. Navigate to the 'WebContent' folder of the qWizard project and type 'jar cvf projectwizard.war'
5. Copy the generated projectwizard.war to the deploy folder of your Liferay installation 'cp projectwizard /home/to/liferay/deploy/'
6. Add qWizard as a new application in your Liferay instance through the web interface
