/*global module:false*/
module.exports = function (grunt) {

    var outputFile = grunt.option('outfile') || 'build/<%= pkg.name %>.js'

    function src(file) {
        return 'src/main/javascript/' + file;
    }

    function createTestOptions(specs) {
        return {
            specs: specs,
            timeout: 60000,
            vendor: '../js-test-support/lib/**/*.js',
            helpers: [
                '../js-test-support/helpers/**/*.js',
                'src/js-test-support/mockNamespaces.js',
                'src/js-test-support/ajaxMockUtils.js',
                'src/js-test-support/owfEventingMock.js',
                'build/acceptanceTestSupport/ports.js'],
            '--web-security': false,
            '--local-to-remote-url-access': true,
            '--ignore-ssl-errors': true
        };
    }

    // order dependent files, so exclude them from the concatenation. they will be included in the correct order
    var concatExcludes = ['intro.js', 'util/loggerUtils.js'];


    grunt.initConfig({
            // Metadata.
            pkg: grunt.file.readJSON('package.json'),
            banner: '/*! <%= pkg.title || pkg.name %> */',
            // Task configuration.
            concat: {
                options: {
                    banner: '<%= banner %>',
                    stripBanners: true
                },
                nodeps: {
                    src: [].concat([src('intro.js'), src('util/loggerUtils.js')]).concat(grunt.file.expand(src('**/*.js'), concatExcludes.map(function (file) {
                        return '!' + src(file);
                    }))),
                    dest: 'build/js-temp/<%= pkg.name %>.js'
                },
                dist: {
                    src: ['src/main/js-lib/**/*.js', 'build/dependencies/**/*.js', '<%= concat.nodeps.dest %>'],
                    dest: outputFile
                }
            },
            jshint: {
                options: {
                    'jshintrc': '../.jshintrc'
                },
                // check both the preconcat and concatenated files
                files: [].concat('<%= concat.nodeps.src %>').concat(['<%= concat.nodeps.dest %>'])
            },
            jasmine: {
                unit: {
                    src: outputFile,
                    options: createTestOptions('src/test/javascript/spec/**/*.spec.js')
                },
                acceptance: {
                    src: outputFile,
                    options: createTestOptions('src/acceptanceTest/javascript/spec/**/*.spec.js')
                }

            }
        }
    );

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-jasmine');

    // hint after concatenation since the concatenated version is also hinted
    grunt.registerTask('default', ['concat', 'jshint', 'jasmine:unit']);




};
