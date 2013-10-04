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
                src: [src('map.js')],
                dest: 'build/js-temp/<%= pkg.name %>.js'
            },
            dist: {
                src: [lib('openlayers'), lib('heatmap'), 'build/dependencies/**/*.js', '<%= concat.nodeps.dest %>'],
                dest: outputFile
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

    // hint after concatenation since the concatenated version is also hinted
    grunt.registerTask('default', ['concat', 'jshint']);

};
