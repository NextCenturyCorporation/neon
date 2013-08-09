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
                nodeps: {
                    src: src('table.js'),
                    dest: 'build/js-temp/<%= pkg.name %>.js'
                },
                dist: {
                    src: [lib('jquery/jquery-1.7.min.js'),
                        lib('jquery/jquery.event.drag-2.2.js'),
                        lib('jquery/jquery-ui-1.8.16.custom.min.js'),
                        lib('slickgrid/slick.core.js'),
                        lib('slickgrid/slick.grid.js'),
                        lib('slickgrid/slick.dataview.js'),
                        lib('slickgrid/plugins/slick.autotooltips.js'),
                        lib('mergesort/merge-sort.js'),
                        lib('bootstrap/bootstrap.min.js'),
                        'build/jsdependencies/**/*.js',
                        '<%= concat.nodeps.dest %>'],
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
                    options: {
                        specs: 'src/test/javascript/spec/**/*.spec.js',
                        vendor: '../js-test-support/lib//**/*.js',
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

    // hint after concatenation since the concatenated version is also hinted
    grunt.registerTask('default', ['concat', 'jshint', 'jasmine:unit']);

}
;
