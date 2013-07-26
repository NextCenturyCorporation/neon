## Overview
The neon framework provides a SQL-like query language that can be used by javascript frameworks to execute queries
  against different types of databases. The queries are executed on the server, and neon translates its query language
  into database specific queries.

Neon also provides an interaction framework that allows different visualizations (widgets) to communicate with one
   another. For example, selecting a point on a map widget might select that point in a table.


## Building neon

Neon contains a mix of groovy and javascript code and uses different build tools for the different languages:

* gradle (or gradlew) can be used to build the groovy code
* npm/nodejs/grunt are used to build the javascript code
* the gradle build files provide wrapper tasks for executing grunt tasks

### setup instructions for npm/nodejs

* install npm and nodejs
* from the top level neon directory, run the gradle task installGruntDeps. this will install the necessary javascript
build tools. this only needs to be re-run when the package.json file changes.

### Test tasks

Several tasks exist for running tests:

* test - runs groovy unit tests
* integrationTest - runs groovy integration tests (requires mongodb)
* acceptanceTest - runs end to end acceptance tests (requires mongodb)
* gruntjs runs all javascript verification tasks - unit test, jshint, etc


note: all projects share a common javascript configuration, so the package.json file is dynamically generated from the package.json.template in the top level neon folder