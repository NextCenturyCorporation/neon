/*global module:false*/
module.exports = function (grunt) {

    var outputFile = grunt.option('outfile') || 'build/<%= pkg.name %>.js'

    function src(file) {
        return 'src/main/javascript/' + file;
    }

    function lib(dir) {
        return 'src/main/js-lib/' + dir + '/**/*.js';
    }

    function aperture(){
        return 'src/main/js-lib/aperture/1.0/aperture.js';
    }

    function apertureLib(file){
        return 'src/main/js-lib/aperture/1.0/lib/' + file;
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
                src: [src('mapwidget.js')],
                dest: 'build/js-temp/<%= pkg.name %>.js'
            },
            dist: {
                src: [lib('d3'), lib('jquery'),
                    apertureLib('raphael.js'),
                    apertureLib('json2-min.js'),
                    apertureLib('proj4js.js'),
                    apertureLib('OpenLayers-textures.js'),
                    aperture(),
                    src('aperturemapconfig.js'),
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
        }
    });

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');

    // hint after concatenation since the concatenated version is also hinted
    grunt.registerTask('default', ['concat', 'jshint']);

};
