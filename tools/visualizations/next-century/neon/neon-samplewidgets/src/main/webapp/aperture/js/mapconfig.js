aperture.config.provide({
    'aperture.map': {
        'defaultMapConfig': {

            /*
             * Map wide options which are required for proper use of
             * the tile set below.
             */
            'options': {
                'projection': 'EPSG:900913',
                'displayProjection': 'EPSG:900913',
                'units': 'm',
                'numZoomLevels': 12,
                'maxExtent': [
                    -20037500,
                    -20037500,
                    20037500,
                    20037500
                ]
            },

            /* The example maps use a Tile Map Service (TMS), which when
             * registered with the correct settings here in the client
             * requires no server side code. Tile images are simply requested
             * by predictable url paths which resolve to files on the server.
             */
            'baseLayer': {

                /*
                 * The example map tile set was produced with TileMill,
                 * a free and highly recommended open source tool for
                 * producing map tiles, provided by MapBox.
                 * http://www.mapbox.com.
                 *
                 * Requires specific map-wide options, see above.
                 */
                'tms': {
                    'name': 'Base Map',
                    'url': 'http://aperture.oculusinfo.com/map/',
                    'options': {
                        'layername': 'world-dark',
                        'osm': 0,
                        'type': 'png',
                        serverResolutions: [156543.0339, 78271.51695, 39135.758475, 19567.8792375, 9783.93961875, 4891.96980938, 2445.98490469, 1222.99245234], // ,611.496226172,305.748113086,152.874056543,76.4370282715,38.2185141357,19.1092570679,9.55462853394,4.77731426697,2.38865713348,1.19432856674,0.597164283371
                        resolutions: [156543.0339, 78271.51695, 39135.758475, 19567.8792375, 9783.93961875, 4891.96980938, 2445.98490469, 1222.99245234] //,611.496226172,305.748113086,152.874056543,76.4370282715
                    }
                }
            }
        }
    },
    'aperture.palette' : {
        'color' : {
            'bad'  : '#FF3333',
            'good' : '#66CCC9',
            'selected' : '#FF0000',
            'highlighted' : '#FFCC00'
        },

        'colors' : {
            'series.10' : ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#8c564b', '#e377c2', '#7f7f7f', '#bcbd22', '#17becf']
        }

    }
});