# Experimental design wizard

## Setup instructions

1. Follow the instructions for setting up Liferay and openBIS as well as the QBiC-specific data model found on our portal. (coming soon)
2. Clone Projectwizard from the git repository: ‘git clone https://github.com/qbicsoftware/projectwizard projectwizard’
3. Adjust the properties defined in the file ‘portlet.properties’ (this is what links the portlet to the qbic-ext.properties file)
4. For deployment a web application archive (.war) file has to be created. Navigate to the ‘WebContent’ folder of the Projectwizard project and type ‘jar cvf projectwizard.war .’
5. Copy projectwizard.war to the deploy folder of Liferay, e.g. ‘cp projectwizard /home/tomcat-liferay/liferay-portal-6.2-ce-ga4/deploy/‘
6. Add Projectwizard as a new application in your Liferay instance through the web interface.
