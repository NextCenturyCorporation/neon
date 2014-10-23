#Neon Overview
Neon is a software platform designed to help you, as a developer, to integrate your disparate visualization widgets with your data stores. The **Data Access API** makes it easy for widgets to query NoSQL databases directly from JavaScript or RESTful endpoints, while still letting the server do the heavy lifting. The **Interaction API** provides capabilities for inter-widget communication, which easily links your widgets together. Neon does not provide any user interface components. Instead, Neon shines under-the-hood by removing the pressure from developers to figure out *how* to make different components work together and allowing them to focus more on the fun stuff, like creating valuable data exploration applications and workflows.

## View an example application
To see an example of what's possible with Neon, check out our demo server running an [example application](http://54.88.152.130:8080/neon-examples/angular-demo/app/) built with Neon. Read more about how to use it and how it interacts with Neon [here][demo-guide].

## Build and run the example
Building the example application requires Java JDK 1.7 or 1.8, node.js, and MongoDB. To install everything you need on Ubuntu 14.04, run

    sudo apt-get install git openjdk-7-jdk mongodb unzip npm nodejs-legacy

On Fedora 20, run

    sudo yum install git unzip mongodb mongodb-server java-1.7.0-openjdk-devel npm
    sudo systemctl start mongod

1. With the prerequisites out of the way, get Neon and give it a basic configuration:

        git clone https://github.com/NextCenturyCorporation/neon.git
        cd neon/
        cp gradle.properties.sample gradle.properties

2. Then import the example data into a MongoDB collection:

        cd examples/angular-demo/data/
        unzip earthquakes.zip
        mongoimport --db test --collection earthquakes --type csv --headerline --stopOnError --file earthquakes.csv
        mongo test --eval "db.earthquakes.find().forEach(function(doc){doc.time = new ISODate(doc.time);db.earthquakes.save(doc)});"
        cd ../../..

3. Finally, build and run the Neon server. This will launch a test web server on port 8080:

        ./gradlew jettyRun
    Point your web browser at [http://localhost:8080/neon/examples][demo] and select one of the single page example files to view a simple query, filter, or database listing.  If they display the earthquake data listed above, you have successfully configured your development environment.
    
    To see a more substantial Neon demonstration, check out the [Neon Geo Temporal Dashboard][neon-gtd].  Originally part of this project, it has been spun off to a sister project and is meant to be deployed as a separate web application alongside core Neon.
    
[demo]: http://localhost:8080/neon/examples
[neon-gtd]: https://github.com/NextCenturyCorporation/neon-gtd

##Documentation

**[Neon Wiki][1]** - Visit the Neon wiki for more information on what Neon can do for you.

**[Build Instructions][2]** - Includes instructions for building the Neon WAR file from source code and lists Neon's external dependencies.

**[Deploying Neon][3]** - Includes instructions for deploying the Neon application to a web server (e.g., Jetty and Tomcat).

**[Developer Quick Start Guide][4]** - A quick tour of how to develop apps that use Neon.

## Additional Information

Email: neon-support@nextcentury.com

Website: [http://neonframework.org][5]

Copyright 2014 Next Century Corporation

[1]: https://github.com/NextCenturyCorporation/neon/wiki
[2]: https://github.com/NextCenturyCorporation/neon/wiki/Build-Instructions
[3]: https://github.com/NextCenturyCorporation/neon/wiki/Deploying-Neon
[4]: https://github.com/NextCenturyCorporation/neon/wiki/Developer-Quick-Start-Guide
[5]: http://neonframework.org
