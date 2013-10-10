/*global module:false*/
module.exports = function (grunt) {

    var outputFile = grunt.option('outfile') || 'build/<%= pkg.name %>.js'

    function src(file) {
        return 'src/main/javascript/' + file;
    }

    function lib(dir) {
        return 'src/main/js-lib/' + dir + '/**/*.js';
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
            nodeps: {
                src: [src('mapcore.js')],
                dest: 'build/js-temp/<%= pkg.name %>.js'
            },
            dist: {
                src: [lib('jquery'), lib('d3'), lib('openlayers'), lib('heatmap'), 'build/dependencies/**/*.js', '<%= concat.nodeps.dest %>'],
                dest: outputFile
            }
        },
        jasmine: {
            unit: {
                src: outputFile,
                options: {
                    specs: 'src/test/javascript/spec/**/*.spec.js',
                    vendor: '../../js-test-support/lib/**/*.js',
                    helpers: '../../js-test-support/helpers/**/*.js',
                    '--web-security': false,
                    '--local-to-remote-url-access': true,
                    '--ignore-ssl-errors': true
                }
            }
        },
        jshint: {
            options: {
                'jshintrc': '../../.jshintrc'
            },
            // check both the preconcat and concatenated files
            files: [].concat('<%= concat.nodeps.src %>').concat(['<%= concat.nodeps.dest %>'])
        }
    });

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-jasmine');

    // hint after concatenation since the concatenated version is also hinted
    grunt.registerTask('default', ['concat', 'jshint', 'jasmine:unit']);

};
