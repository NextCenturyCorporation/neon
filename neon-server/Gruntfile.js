/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*global module:false*/
module.exports = function(grunt) {
    var outputFile = (grunt.option('outfile-base') || 'build/<%= pkg.name %>') + '.js';
    var nodepOutputFile = (grunt.option('outfile-base') || 'build/<%= pkg.name %>') + '-nodeps.js';
    var nodepMinOutputFile = (grunt.option('outfile-base') || 'build/<%= pkg.name %>') + '-nodeps.min.js';

    function src(file) {
        return 'src/main/javascript/' + file;
    }

    function lib(dir) {
        return 'src/main/js-lib/' + dir + '/**/*.js';
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
                'src/js-test-support/eventBusTestUtils.js',
                'build/acceptanceTestSupport/testConfig.js'],
            '--web-security': false,
            '--local-to-remote-url-access': true,
            '--ignore-ssl-errors': true
        };
    }

    // order dependent files, so exclude them from the concatenation. they will be included in the correct order
    var neonDeps = ['intro.js', 'util/loggerUtils.js', 'util/owfUtils.js', 'eventing/owf/owfEventBus.js'];
    var neonSrcs = [src('license-header.txt')].concat(neonDeps.map(function(file) {
        return src(file);
    })).concat(grunt.file.expand(src('**/*.js'), neonDeps.map(function(file) {
        return '!' + src(file);
    })));

    grunt.initConfig({
        // Metadata.
        pkg: grunt.file.readJSON('package.json'),
        banner: '/*!  <%= pkg.title || pkg.name %> | Copyright 2013 <%= pkg.author %> | https://raw.githubusercontent.com/NextCenturyCorporation/neon/master/LICENSE.txt */' + grunt.util.linefeed + grunt.util.linefeed,

        // Task configuration.
        uglify: {
            options: {
                banner: '<%= banner %>',
                mangle: false
            },
            nodeps: {
                files: [{
                    src: neonSrcs,
                    dest: nodepMinOutputFile
                }]
            }
        },

        concat: {
            nodeps: {
                options: {
                    banner: '<%= banner %>',
                    stripBanners: true
                },
                src: neonSrcs,
                dest: nodepOutputFile
            },
            neon: {
                src: [lib('lodash'), lib('uuid'), lib('postal'), lib('jquery'), lib('log4javascript'), 'build/dependencies/**/*.js', nodepMinOutputFile],
                dest: outputFile
            }
        },

        jshint: {
            options: {
                jshintrc: '../.jshintrc',
                reporter: "jslint",
                reporterOutput: "reports/jslint.xml"
            },
            source: {
                options: {
                    reporterOutput: "reports/source-jslint.xml"
                },
                // check both the preconcat and concatenated files
                files: {
                    src: [nodepOutputFile].concat(neonSrcs)
                }
            },
            tests: {
                options: {
                    reporterOutput: "reports/tests-jslint.xml"
                },
                files: {
                    src: ['src/acceptanceTest/javascript/spec/**/*.spec.js', 'src/test/javascript/spec/**/*.spec.js']
                }
            }
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
        },

        jscs: {
            options: {
                config: ".jscsrc",
                force: true,
                reporterOutput: 'reports/jscs.xml',
                reporter: 'checkstyle'
            },
            source: {
                options: {
                    reporterOutput: 'reports/source-jscs.xml'
                },
                files: {
                    src: ['Gruntfile.js', 'src/main/javascript/**/*.js']
                }
            },
            tests: {
                options: {
                    reporterOutput: 'reports/tests-jscs.xml'
                },
                files: {
                    src: ['src/acceptanceTest/javascript/spec/**/*.spec.js', 'src/test/javascript/spec/**/*.spec.js']
                }
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-jshint');
    grunt.loadNpmTasks('grunt-contrib-jasmine');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-jscs');

    // hint after concatenation since the concatenated version is also hinted
    grunt.registerTask('default', ['uglify', 'concat', 'jscs', 'jshint', 'jasmine:unit']);
};
