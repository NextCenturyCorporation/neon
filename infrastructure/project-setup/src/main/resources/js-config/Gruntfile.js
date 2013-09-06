/*global module:false*/
module.exports = function (grunt) {

    var outputFile = grunt.option('outfile') || 'build/<%= pkg.name %>.js'

    function src(file) {
        return 'src/main/javascript/' + file;
    }

    function lib(file) {
        return 'src/main/js-lib/' + file;
    }

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
		// code from this project with no dependencies
                nodeps: {		
                    src: [src('**/*')],
                    dest: 'build/js-temp/<%= pkg.name %>.js'
                },
		// include library files here to concatenate all files to one js file
                dist: {
                    src: ['<%= concat.nodeps.dest %>'],
                    dest: outputFile
                }
            },
            jshint: {
                options: {
                    'jshintrc': '.jshintrc'
                },
                // check both the preconcat and concatenated files
                files: [].concat('<%= concat.nodeps.src %>').concat(['<%= concat.nodeps.dest %>'])
            },
            jasmine: {
                unit: {
                    src: outputFile,
                    options: {
                        specs: 'src/test/javascript/spec/**/*.spec.js',
                        vendor: '../js-test-support/lib/**/*.js',
                        helpers: '../js-test-support/helpers/**/*.js',
                        '--web-security': false,
                        '--local-to-remote-url-access': true,
                        '--ignore-ssl-errors': true
                    }
                }
            }
        }
    )
    ;

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-jasmine');

    grunt.registerTask('default', ['concat', 'jshint', 'jasmine:unit']);

};
